#!/usr/bin/env python3
"""Generate Derby SQL import scripts from legacy CRM exports.

By default input files are searched in ../txt data, relative to this script:
- clients.txt: semicolon-separated customer rows
- tutte_le_note.txt: concatenated XML note documents delimited by FILE/END FILE markers
"""
from __future__ import annotations

import re
import uuid
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Iterable

SCRIPT_DIR = Path(__file__).resolve().parent


def find_project_root() -> Path:
    for candidate in (SCRIPT_DIR, *SCRIPT_DIR.parents):
        if (candidate / "pom.xml").is_file():
            return candidate
    return SCRIPT_DIR.parent


ROOT = find_project_root()
DEFAULT_IMPORT_DIR = (SCRIPT_DIR / "../txt data").resolve()
LEGACY_IMPORT_DIR = ROOT / "src/main/resources/importa"
IMPORT_DIR = DEFAULT_IMPORT_DIR if DEFAULT_IMPORT_DIR.is_dir() else LEGACY_IMPORT_DIR
OUT_DIR = ROOT / "src/main/resources/importa/generated"
CLIENTS_FILE = IMPORT_DIR / "clients.txt"
NOTES_FILE = IMPORT_DIR / "tutte_le_note.txt"

SPECIAL_NULLS = {"", "?", "??", "???", "BLANK", "NULL", "NULLO"}
NS = uuid.UUID("8a05d4bc-97cc-4df0-bf06-000000000000")

FIELDS = [
    "ragione_sociale", "persona_riferimento", "email_referente", "telefono", "paese", "regione", "citta",
    "indirizzo", "numero_civico", "provincia", "cap", "interessamento", "tipo_cliente", "partita_iva",
    "codice_fiscale", "titolare", "email_generica", "email_certificata", "sito_web", "note_id", "operatore",
    "volte_contattati", "ultima_chiamata", "prossima_chiamata", "coinvolgimento", "acquisizione", "checkpoint",
    "telefono2", "cellulare",
]

@dataclass
class Call:
    note_id: str
    number: str | None
    data: str | None
    operatore: str | None
    previous_interest: str | None
    new_interest: str | None
    checkpoint: str | None
    durata: str | None
    messaggio: str | None
    text: str

@dataclass
class Client:
    rownum: int
    raw: dict[str, str | None]
    id: str = field(init=False)
    calls: list[Call] = field(default_factory=list)

    def __post_init__(self) -> None:
        natural = self.raw.get("note_id") or f"row-{self.rownum}-{self.raw.get('ragione_sociale') or ''}"
        self.id = uid("cliente", natural)


def clean(value: str | None) -> str | None:
    if value is None:
        return None
    value = value.strip()
    if value.upper() in SPECIAL_NULLS:
        return None
    return value


def sql(value: str | None) -> str:
    value = clean(value)
    if value is None:
        return "NULL"
    return "'" + value.replace("'", "''") + "'"


def sql_date(value: str | None) -> str:
    value = clean(value)
    if not value or not re.fullmatch(r"\d{4}-\d{2}-\d{2}", value):
        return "NULL"
    return f"DATE('{value}')"


def uid(kind: str, key: str) -> str:
    return str(uuid.uuid5(NS, f"{kind}:{key}"))


def parse_coinvolgimento(value: str | None) -> int | None:
    value = clean(value)
    if value is None:
        return None
    try:
        number = int(Decimal(value))
    except (InvalidOperation, ValueError):
        return None
    return number if 1 <= number <= 5 else None


def parse_clients() -> list[Client]:
    clients: list[Client] = []
    for rownum, line in enumerate(CLIENTS_FILE.read_text(encoding="utf-8").splitlines(), start=1):
        if not line.strip():
            continue
        parts = line.split(";")
        if len(parts) < len(FIELDS):
            parts += [""] * (len(FIELDS) - len(parts))
        raw = {field: clean(parts[index]) for index, field in enumerate(FIELDS)}
        clients.append(Client(rownum=rownum, raw=raw))
    return clients


def parse_notes() -> dict[str, list[Call]]:
    text = NOTES_FILE.read_text(encoding="utf-8")
    pattern = re.compile(r"===== FILE: ([^.]+)\.xml\.txt =====\s*(.*?)\s*===== END FILE:", re.S)
    notes: dict[str, list[Call]] = {}
    for note_id, xml_text in pattern.findall(text):
        try:
            root = ET.fromstring(xml_text.strip())
        except ET.ParseError:
            continue
        calls = []
        for elem in root.findall("chiamata"):
            calls.append(Call(
                note_id=note_id,
                number=clean(elem.get("number")),
                data=clean(elem.get("data")),
                operatore=clean(elem.get("operatore")),
                previous_interest=clean(elem.get("previousInterest")),
                new_interest=clean(elem.get("newInterest")),
                checkpoint=clean(elem.get("checkpoint")),
                durata=clean(elem.get("durata")),
                messaggio=clean(elem.get("messaggio")),
                text=(elem.text or "").strip(),
            ))
        notes[note_id] = calls
    return notes


def operator_id(username: str) -> str:
    return uid("operatore", username.upper())


def collect_operators(clients: list[Client]) -> list[str]:
    operators: set[str] = set()
    for client in clients:
        if client.raw.get("operatore"):
            operators.add(client.raw["operatore"] or "")
        for call in client.calls:
            if call.operatore:
                operators.add(call.operatore)
    return sorted(operators, key=str.upper)


def final_interest(client: Client) -> str | None:
    valid = [call for call in client.calls if call.new_interest and call.data]
    if valid:
        valid.sort(key=lambda c: (c.data or "", int(c.number) if (c.number or "").isdigit() else 0))
        if valid[-1].new_interest:
            return valid[-1].new_interest
    return client.raw.get("interessamento")


def note_text(call: Call) -> str:
    lines = []
    if call.number:
        lines.append(f"[Chiamata #{call.number}]")
    meta = [
        ("Stato precedente", call.previous_interest),
        ("Nuovo stato", call.new_interest),
        ("Checkpoint", call.checkpoint),
        ("Durata", call.durata),
        ("Messaggio", call.messaggio),
    ]
    lines.extend(f"{label}: {value}" for label, value in meta if value)
    if lines and call.text:
        lines.append("")
    lines.append(call.text)
    return "\n".join(lines).strip()


def write(name: str, statements: Iterable[str]) -> int:
    content = ["-- Auto-generato da scripts/generate_import_sql.py.\n"]
    count = 0
    for statement in statements:
        content.append(statement.rstrip() + "\n")
        count += 1
    (OUT_DIR / name).write_text("".join(content), encoding="utf-8")
    return count


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    clients = parse_clients()
    notes = parse_notes()
    for client in clients:
        note_id = client.raw.get("note_id")
        client.calls = notes.get(note_id or "", [])

    counts = {}
    counts["operatori"] = write("import_operatori.sql", (
        f"INSERT INTO OPERATORI (ID, USERNAME, ATTIVO) SELECT '{operator_id(op)}', {sql(op)}, 1 FROM SYSIBM.SYSDUMMY1 WHERE NOT EXISTS (SELECT 1 FROM OPERATORI WHERE USERNAME = {sql(op)});"
        for op in collect_operators(clients)
    ))

    counts["clienti"] = write("import_clienti.sql", (
        "INSERT INTO CLIENTI (ID, RAGIONE_SOCIALE, TIPO_CLIENTE, STATO_TRATTATIVA, COINVOLGIMENTO, PARTITA_IVA, CODICE_FISCALE, ACQUISIZIONE, OPERATORE_ID) VALUES "
        f"('{c.id}', {sql(c.raw.get('ragione_sociale'))}, {sql(c.raw.get('tipo_cliente'))}, {sql(final_interest(c))}, "
        f"{parse_coinvolgimento(c.raw.get('coinvolgimento')) if parse_coinvolgimento(c.raw.get('coinvolgimento')) is not None else 'NULL'}, "
        f"{sql(c.raw.get('partita_iva'))}, {sql(c.raw.get('codice_fiscale'))}, {sql_date(c.raw.get('acquisizione'))}, "
        f"{('(SELECT ID FROM OPERATORI WHERE USERNAME = ' + sql(c.raw.get('operatore')) + ')') if c.raw.get('operatore') else 'NULL'});"
        for c in clients
    ))

    counts["contatti"] = write("import_contatti.sql", (
        f"INSERT INTO CONTATTI_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES ('{uid('contatto', c.id + ':' + label)}', '{c.id}', {sql(value)});"
        for c in clients for label, value in (("persona", c.raw.get("persona_riferimento")), ("titolare", c.raw.get("titolare"))) if value
    ))
    counts["indirizzi"] = write("import_indirizzi.sql", (
        f"INSERT INTO INDIRIZZI_CLIENTE (ID, CLIENTE_ID, PAESE, REGIONE, PROVINCIA, CITTA, INDIRIZZO, NUMERO_CIVICO, CAP, PRINCIPALE) VALUES ('{uid('indirizzo', c.id)}', '{c.id}', {sql(c.raw.get('paese'))}, {sql(c.raw.get('regione'))}, {sql(c.raw.get('provincia'))}, {sql(c.raw.get('citta'))}, {sql(c.raw.get('indirizzo'))}, {sql(c.raw.get('numero_civico'))}, {sql(c.raw.get('cap'))}, 1);"
        for c in clients if any(c.raw.get(k) for k in ("paese", "regione", "provincia", "citta", "indirizzo", "numero_civico", "cap"))
    ))
    counts["telefoni"] = write("import_telefoni.sql", (
        f"INSERT INTO TELEFONI_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES ('{uid('telefono', c.id + ':' + label)}', '{c.id}', {sql(value)});"
        for c in clients for label, value in (("telefono", c.raw.get("telefono")), ("telefono2", c.raw.get("telefono2")), ("cellulare", c.raw.get("cellulare"))) if value
    ))
    counts["email"] = write("import_email.sql", (
        f"INSERT INTO EMAIL_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES ('{uid('email', c.id + ':' + label)}', '{c.id}', {sql(value)});"
        for c in clients for label, value in (("referente", c.raw.get("email_referente")), ("generica", c.raw.get("email_generica")), ("pec", c.raw.get("email_certificata"))) if value
    ))
    counts["siti"] = write("import_siti.sql", (
        f"INSERT INTO SITI_WEB_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES ('{uid('sito', c.id)}', '{c.id}', {sql(c.raw.get('sito_web'))});"
        for c in clients if c.raw.get("sito_web")
    ))

    note_statements = []
    for c in clients:
        sorted_calls = sorted(c.calls, key=lambda call: (call.data or "9999-99-99", int(call.number) if (call.number or "").isdigit() else 0))
        for idx, call in enumerate(sorted_calls):
            nid = uid("nota", f"{c.id}:{call.note_id}:{call.number}:{call.data}:{idx}")
            iid = uid("interazione", nid)
            op_expr = f"(SELECT ID FROM OPERATORI WHERE USERNAME = {sql(call.operatore)})" if call.operatore else "NULL"
            next_date = c.raw.get("prossima_chiamata") if idx == len(sorted_calls) - 1 else None
            note_statements.append(f"INSERT INTO NOTE_CLIENTE (ID, CLIENTE_ID, OPERATORE_ID, TESTO) VALUES ('{nid}', '{c.id}', {op_expr}, {sql(note_text(call))});")
            note_statements.append(f"INSERT INTO INTERAZIONI (ID, CLIENTE_ID, OPERATORE_ID, NOTA_ID, DATA_CONTATTO, PROSSIMO_CONTATTO) VALUES ('{iid}', '{c.id}', {op_expr}, '{nid}', {sql_date(call.data)}, {sql_date(next_date)});")
        if not sorted_calls and (c.raw.get("ultima_chiamata") or c.raw.get("prossima_chiamata")):
            iid = uid("interazione-sintetica", c.id)
            op_expr = f"(SELECT ID FROM OPERATORI WHERE USERNAME = {sql(c.raw.get('operatore'))})" if c.raw.get("operatore") else "NULL"
            note_statements.append(f"INSERT INTO INTERAZIONI (ID, CLIENTE_ID, OPERATORE_ID, DATA_CONTATTO, PROSSIMO_CONTATTO) VALUES ('{iid}', '{c.id}', {op_expr}, {sql_date(c.raw.get('ultima_chiamata'))}, {sql_date(c.raw.get('prossima_chiamata'))});")
    counts["note_interazioni"] = write("import_note_interazioni.sql", note_statements)

    report = [
        "Import legacy CRM\n",
        f"Cartella sorgente: {IMPORT_DIR}\n",
        f"Clienti letti: {len(clients)}\n",
        f"Documenti note letti: {len(notes)}\n",
        f"Documenti note collegati: {sum(1 for c in clients if c.calls)}\n",
        f"Chiamate XML importate: {sum(len(c.calls) for c in clients)}\n",
        f"Operatori distinti: {counts['operatori']}\n",
        "\nStatement generati:\n",
    ]
    report.extend(f"- {name}: {count}\n" for name, count in counts.items())
    (OUT_DIR / "import_report.txt").write_text("".join(report), encoding="utf-8")

if __name__ == "__main__":
    main()
