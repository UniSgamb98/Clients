import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[3] / "scripts" / "generate_import_sql.py"
SPEC = importlib.util.spec_from_file_location("generate_import_sql", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC.loader
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class ImportGeneratorTest(unittest.TestCase):
    def test_windows_launcher_is_location_independent_and_forwards_arguments(self):
        launcher = (SCRIPT.parent / "genera_import.bat").read_text(encoding="utf-8")
        self.assertIn("set \"SCRIPT_DIR=%~dp0\"", launcher)
        self.assertIn("%SCRIPT_DIR%generate_import_sql.py", launcher)
        self.assertIn('py -3 "%GENERATOR%" %*', launcher)
        self.assertIn('python "%GENERATOR%" %*', launcher)

    def test_generates_linked_note_and_interaction_and_latest_interest(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            fields = [""] * len(MODULE.FIELDS)
            for name, value in {
                "ragione_sociale": "O' Dental", "interessamento": "INFO", "note_id": "note-1",
                "operatore": "victoria", "coinvolgimento": "3.0", "prossima_chiamata": "2024-07-01",
            }.items():
                fields[MODULE.FIELDS.index(name)] = value
            (root / "clients.txt").write_text(";".join(fields) + ";\n", encoding="utf-8")
            (root / "notes.txt").write_text("""===== FILE: note-1.xml =====
<companyNotes><chiamata cancelled="true" data="2024-05-01" newInterest="INFO" operatore="santolo" number="1">Prima</chiamata><chiamata data="2024-05-20" newInterest="CLIENTE" operatore="teresa" number="2" previousInterest="BLANK">Testo ? libero</chiamata></companyNotes>
===== END FILE: note-1.xml =====""", encoding="utf-8")

            MODULE.generate(root / "clients.txt", root / "notes.txt", root / "out")

            customers = (root / "out/import_clienti.sql").read_text()
            timeline = (root / "out/import_note_interazioni.sql").read_text()
            operators = (root / "out/import_operatori.sql").read_text()
            self.assertIn("'O'' Dental'", customers)
            self.assertIn("'CLIENTE', 3", customers)
            self.assertEqual(2, timeline.count("INSERT INTO NOTE_CLIENTE"))
            self.assertEqual(2, timeline.count("INSERT INTO INTERAZIONI"))
            self.assertIn("Testo ? libero", timeline)
            self.assertNotIn("Stato precedente: BLANK", timeline)
            self.assertIn("'SANTOLO'", operators)
            self.assertIn("'TERESA'", operators)
            self.assertIn("'VICTORIA'", operators)

    def test_special_values_are_null_and_invalid_involvement_is_reported(self):
        diagnostics = MODULE.Diagnostics()
        self.assertIsNone(MODULE.clean("???"))
        self.assertEqual("testo BLANK libero", MODULE.clean("testo BLANK libero"))
        self.assertIsNone(MODULE.parse_involvement("6.0"))
        self.assertEqual("NULL", MODULE.sql_date("2024-02-30", diagnostics))
        self.assertIn("2024-02-30", diagnostics.invalid_dates)


if __name__ == "__main__":
    unittest.main()
