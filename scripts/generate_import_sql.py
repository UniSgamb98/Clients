#!/usr/bin/env python3
"""Genera gli script Derby per importare l'esportazione del vecchio CRM.

Il comando non modifica il database.  Produce file SQL ordinati, rieseguibili solo
su un database vuoto (gli operatori sono invece inseriti con una guardia), e un
report con le anomalie riscontrate.
"""
from __future__ import annotations

import argparse
import re
import sys
import uuid
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from datetime import date
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Iterable

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CLIENTS_FILE = ROOT / "scripts" / "clients.txt"
DEFAULT_NOTES_FILE = ROOT / "scripts" / "tutte_le_note.txt"
SPECIAL_NULLS = {"", "?", "??", "???", "BLANK", "NULL", "NULLO"}
NS = uuid.UUID("8a05d4bc-97cc-4df0-bf06-000000000000")
FIELDS = [
    "ragione_sociale", "persona_riferimento", "email_referente", "telefono", "paese", "regione", "citta",
    "indirizzo", "numero_civico", "provincia", "cap", "interessamento", "tipo_cliente", "partita_iva",
    "codice_fiscale", "titolare", "email_generica", "email_certificata", "sito_web", "note_id", "operatore",
    "volte_contattati", "ultima_chiamata", "prossima_chiamata", "coinvolgimento", "acquisizione", "checkpoint",
    "telefono2", "cellulare",
]
MARKER = re.compile(
    r"===== FILE:\s*(.+?)\.xml(?:\.txt)?\s*=====\s*(.*?)\s*===== END FILE:\s*\1\.xml(?:\.txt)?\s*=====",
    re.DOTALL,
)


@dataclass
class Call:
    note_id: str
    ordinal: int
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
    calls: list[Call] = field(default_factory=list)
    id: str = field(init=False)

    def __post_init__(self) -> None:
        key = self.raw.get("note_id") or f"riga-{self.rownum}-{self.raw.get('ragione_sociale') or ''}"
        self.id = uid("cliente", key)


@dataclass
class Diagnostics:
    malformed_client_rows: list[int] = field(default_factory=list)
    invalid_dates: set[str] = field(default_factory=set)
    invalid_involvement_rows: list[int] = field(default_factory=list)
    xml_errors: list[str] = field(default_factory=list)
    duplicate_note_ids: list[str] = field(default_factory=list)


def clean(value: str | None) -> str | None:
    if value is None:
        return None
    stripped = value.strip()
    return None if stripped.upper() in SPECIAL_NULLS else stripped


def normalized_operator(value: str | None) -> str | None:
    value = clean(value)
    return value.upper() if value else None


def uid(kind: str, key: str) -> str:
    return str(uuid.uuid5(NS, f"{kind}:{key}"))


def sql(value: str | None) -> str:
    value = clean(value)
    return "NULL" if value is None else "'" + value.replace("'", "''") + "'"


def valid_iso_date(value: str | None) -> str | None:
    value = clean(value)
    if not value:
        return None
    try:
        return date.fromisoformat(value).isoformat()
    except ValueError:
        return None


def sql_date(value: str | None, diagnostics: Diagnostics) -> str:
    parsed = valid_iso_date(value)
    if parsed:
        return f"DATE('{parsed}')"
    if clean(value):
        diagnostics.invalid_dates.add(clean(value) or "")
    return "NULL"


def parse_involvement(value: str | None) -> int | None:
    value = clean(value)
    if value is None:
        return None
    try:
        parsed = Decimal(value)
        integer = int(parsed)
    except (InvalidOperation, ValueError):
        return None
    return integer if parsed == integer and 1 <= integer <= 5 else None


def parse_clients(path: Path, diagnostics: Diagnostics) -> list[Client]:
    clients: list[Client] = []
    for rownum, line in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), 1):
        if not line.strip():
            continue
        parts = line.split(";")
        # È ammesso un solo separatore finale; altri campi mancanti/eccedenti sono segnalati.
        logical_length = len(parts) - 1 if parts and parts[-1] == "" else len(parts)
        if logical_length != len(FIELDS):
            diagnostics.malformed_client_rows.append(rownum)
        parts = (parts + [""] * len(FIELDS))[:len(FIELDS)]
        raw = {name: clean(parts[index]) for index, name in enumerate(FIELDS)}
        if raw["coinvolgimento"] and parse_involvement(raw["coinvolgimento"]) is None:
            diagnostics.invalid_involvement_rows.append(rownum)
        clients.append(Client(rownum, raw))
    return clients


def parse_notes(path: Path, diagnostics: Diagnostics) -> dict[str, list[Call]]:
    documents: dict[str, list[Call]] = {}
    for note_id, xml_text in MARKER.findall(path.read_text(encoding="utf-8-sig")):
        note_id = note_id.strip()
        if note_id in documents:
            diagnostics.duplicate_note_ids.append(note_id)
            continue
        try:
            root = ET.fromstring(xml_text.strip())
        except ET.ParseError as error:
            diagnostics.xml_errors.append(f"{note_id}: {error}")
            continue
        calls: list[Call] = []
        for ordinal, elem in enumerate(root.findall("chiamata")):
            calls.append(Call(
                note_id, ordinal, clean(elem.get("number")), clean(elem.get("data")),
                normalized_operator(elem.get("operatore")), clean(elem.get("previousInterest")),
                clean(elem.get("newInterest")), clean(elem.get("checkpoint")), clean(elem.get("durata")),
                clean(elem.get("messaggio")), "".join(elem.itertext()).strip(),
            ))
        documents[note_id] = calls
    return documents


def chronological_key(call: Call) -> tuple[str, int, int]:
    number = int(call.number) if (call.number or "").isdigit() else -1
    return (valid_iso_date(call.data) or "9999-12-31", number, call.ordinal)


def final_interest(client: Client) -> str | None:
    # Senza una data valida non è possibile stabilire che una chiamata sia cronologicamente l'ultima.
    changes = [call for call in client.calls if call.new_interest and valid_iso_date(call.data)]
    return max(changes, key=chronological_key).new_interest if changes else client.raw.get("interessamento")


def note_text(call: Call) -> str:
    lines: list[str] = []
    if call.number:
        lines.append(f"[Chiamata #{call.number}]")
    for label, value in (
        ("Stato precedente", call.previous_interest), ("Nuovo stato", call.new_interest),
        ("Checkpoint", call.checkpoint), ("Durata", call.durata), ("Messaggio", call.messaggio),
    ):
        if value:
            lines.append(f"{label}: {value}")
    if lines and call.text:
        lines.append("")
    if call.text:
        lines.append(call.text)
    return "\n".join(lines)


def operator_expression(operator: str | None) -> str:
    operator = normalized_operator(operator)
    return f"(SELECT ID FROM OPERATORI WHERE USERNAME = {sql(operator)})" if operator else "NULL"


def write_sql(out_dir: Path, name: str, statements: Iterable[str]) -> int:
    rows = list(statements)
    header = "-- Auto-generato da scripts/generate_import_sql.py; eseguire nell'ordine indicato nel report.\n"
    (out_dir / name).write_text(header + "\n".join(rows) + ("\n" if rows else ""), encoding="utf-8")
    return len(rows)


def generate(clients_file: Path, notes_file: Path, out_dir: Path) -> dict[str, int]:
    diagnostics = Diagnostics()
    clients = parse_clients(clients_file, diagnostics)
    notes = parse_notes(notes_file, diagnostics)
    for client in clients:
        client.calls = notes.get(client.raw.get("note_id") or "", [])
    out_dir.mkdir(parents=True, exist_ok=True)

    operators = sorted({operator for client in clients for operator in
                        ([normalized_operator(client.raw.get("operatore"))] + [call.operatore for call in client.calls])
                        if operator})
    counts: dict[str, int] = {}
    counts["import_operatori.sql"] = write_sql(out_dir, "import_operatori.sql", (
        f"INSERT INTO OPERATORI (ID, USERNAME, ATTIVO) SELECT '{uid('operatore', op)}', {sql(op)}, 1 "
        f"FROM SYSIBM.SYSDUMMY1 WHERE NOT EXISTS (SELECT 1 FROM OPERATORI WHERE USERNAME = {sql(op)});"
        for op in operators
    ))
    counts["import_clienti.sql"] = write_sql(out_dir, "import_clienti.sql", (
        "INSERT INTO CLIENTI (ID, RAGIONE_SOCIALE, TIPO_CLIENTE, STATO_TRATTATIVA, COINVOLGIMENTO, "
        "PARTITA_IVA, CODICE_FISCALE, ACQUISIZIONE, OPERATORE_ID) VALUES "
        f"('{c.id}', {sql(c.raw.get('ragione_sociale'))}, {sql(c.raw.get('tipo_cliente'))}, {sql(final_interest(c))}, "
        f"{parse_involvement(c.raw.get('coinvolgimento')) or 'NULL'}, {sql(c.raw.get('partita_iva'))}, "
        f"{sql(c.raw.get('codice_fiscale'))}, {sql_date(c.raw.get('acquisizione'), diagnostics)}, "
        f"{operator_expression(c.raw.get('operatore'))});" for c in clients
    ))
    counts["import_contatti.sql"] = write_sql(out_dir, "import_contatti.sql", (
        f"INSERT INTO CONTATTI_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES "
        f"('{uid('contatto', c.id + ':' + label)}', '{c.id}', {sql(value)});"
        for c in clients for label, value in (("persona", c.raw.get("persona_riferimento")), ("titolare", c.raw.get("titolare"))) if value
    ))
    counts["import_indirizzi.sql"] = write_sql(out_dir, "import_indirizzi.sql", (
        "INSERT INTO INDIRIZZI_CLIENTE (ID, CLIENTE_ID, PAESE, REGIONE, PROVINCIA, CITTA, INDIRIZZO, NUMERO_CIVICO, CAP, PRINCIPALE) VALUES "
        f"('{uid('indirizzo', c.id)}', '{c.id}', {sql(c.raw.get('paese'))}, {sql(c.raw.get('regione'))}, "
        f"{sql(c.raw.get('provincia'))}, {sql(c.raw.get('citta'))}, {sql(c.raw.get('indirizzo'))}, "
        f"{sql(c.raw.get('numero_civico'))}, {sql(c.raw.get('cap'))}, 1);"
        for c in clients if any(c.raw.get(key) for key in ("paese", "regione", "provincia", "citta", "indirizzo", "numero_civico", "cap"))
    ))
    counts["import_telefoni.sql"] = write_sql(out_dir, "import_telefoni.sql", (
        f"INSERT INTO TELEFONI_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES ('{uid('telefono', c.id + ':' + label)}', '{c.id}', {sql(value)});"
        for c in clients for label, value in (("telefono", c.raw.get("telefono")), ("telefono2", c.raw.get("telefono2")), ("cellulare", c.raw.get("cellulare"))) if value
    ))
    counts["import_email.sql"] = write_sql(out_dir, "import_email.sql", (
        f"INSERT INTO EMAIL_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES ('{uid('email', c.id + ':' + label)}', '{c.id}', {sql(value)});"
        for c in clients for label, value in (("referente", c.raw.get("email_referente")), ("generica", c.raw.get("email_generica")), ("pec", c.raw.get("email_certificata"))) if value
    ))
    counts["import_siti.sql"] = write_sql(out_dir, "import_siti.sql", (
        f"INSERT INTO SITI_WEB_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES ('{uid('sito', c.id)}', '{c.id}', {sql(c.raw.get('sito_web'))});"
        for c in clients if c.raw.get("sito_web")
    ))

    note_rows: list[str] = []
    for client in clients:
        calls = sorted(client.calls, key=chronological_key)
        for index, call in enumerate(calls):
            base = f"{client.id}:{call.note_id}:{call.ordinal}"
            note_id, interaction_id = uid("nota", base), uid("interazione", base)
            next_contact = client.raw.get("prossima_chiamata") if index == len(calls) - 1 else None
            note_rows.append(
                f"INSERT INTO NOTE_CLIENTE (ID, CLIENTE_ID, OPERATORE_ID, TESTO) VALUES "
                f"('{note_id}', '{client.id}', {operator_expression(call.operatore)}, {sql(note_text(call))});")
            note_rows.append(
                "INSERT INTO INTERAZIONI (ID, CLIENTE_ID, OPERATORE_ID, NOTA_ID, DATA_CONTATTO, PROSSIMO_CONTATTO) VALUES "
                f"('{interaction_id}', '{client.id}', {operator_expression(call.operatore)}, '{note_id}', "
                f"{sql_date(call.data, diagnostics)}, {sql_date(next_contact, diagnostics)});")
        if not calls and (client.raw.get("ultima_chiamata") or client.raw.get("prossima_chiamata")):
            interaction_id = uid("interazione-sintetica", client.id)
            note_rows.append(
                "INSERT INTO INTERAZIONI (ID, CLIENTE_ID, OPERATORE_ID, NOTA_ID, DATA_CONTATTO, PROSSIMO_CONTATTO) VALUES "
                f"('{interaction_id}', '{client.id}', {operator_expression(client.raw.get('operatore'))}, NULL, "
                f"{sql_date(client.raw.get('ultima_chiamata'), diagnostics)}, {sql_date(client.raw.get('prossima_chiamata'), diagnostics)});")
    counts["import_note_interazioni.sql"] = write_sql(out_dir, "import_note_interazioni.sql", note_rows)

    linked_ids = {c.raw.get("note_id") for c in clients if c.raw.get("note_id") in notes}
    call_count = sum(len(c.calls) for c in clients)
    report = [
        "IMPORT LEGACY CRM - REPORT\n",
        f"Clienti letti: {len(clients)}\n", f"Documenti XML letti: {len(notes)}\n",
        f"Documenti XML collegati: {len(linked_ids)}\n", f"Documenti XML non collegati: {len(set(notes) - linked_ids)}\n",
        f"Chiamate XML importate: {call_count}\n", f"Operatori distinti: {len(operators)}\n",
        f"Righe clienti malformate: {len(diagnostics.malformed_client_rows)}",
        (f" ({', '.join(map(str, diagnostics.malformed_client_rows[:20]))})\n" if diagnostics.malformed_client_rows else "\n"),
        f"Coinvolgimenti non validi: {len(diagnostics.invalid_involvement_rows)}\n",
        f"Date non valide: {len(diagnostics.invalid_dates)}",
        (f" ({', '.join(sorted(diagnostics.invalid_dates))})\n" if diagnostics.invalid_dates else "\n"),
        f"XML non validi: {len(diagnostics.xml_errors)}\n", f"NoteId duplicati: {len(diagnostics.duplicate_note_ids)}\n",
        "\nOrdine di esecuzione e statement:\n",
    ]
    report.extend(f"{index}. {name}: {counts[name]}\n" for index, name in enumerate(counts, 1))
    if diagnostics.xml_errors:
        report.append("\nErrori XML:\n" + "\n".join(diagnostics.xml_errors) + "\n")
    (out_dir / "import_report.txt").write_text("".join(report), encoding="utf-8")
    return counts


def display_path(path: Path) -> str:
    """Mostra i file del progetto con un percorso breve e comprensibile."""
    try:
        return path.resolve().relative_to(ROOT.resolve()).as_posix()
    except ValueError:
        return str(path)


def require_input_files(paths: Iterable[Path]) -> None:
    missing = [path for path in paths if not path.is_file()]
    if not missing:
        return
    print("ERRORE: file di input non trovati:", file=sys.stderr)
    for path in missing:
        print(f"- {display_path(path)}", file=sys.stderr)
    raise SystemExit(2)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--clients", type=Path, default=DEFAULT_CLIENTS_FILE)
    parser.add_argument("--notes", type=Path, default=DEFAULT_NOTES_FILE)
    parser.add_argument("--output", type=Path, default=ROOT / "import scripts")
    args = parser.parse_args()
    require_input_files((args.clients, args.notes))
    generate(args.clients, args.notes, args.output)


if __name__ == "__main__":
    main()
