import importlib.util
import contextlib
import io
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch


SCRIPT = Path(__file__).resolve().parents[3] / "scripts" / "generate_import_sql.py"
SPEC = importlib.util.spec_from_file_location("generate_import_sql", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC.loader
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class ImportGeneratorTest(unittest.TestCase):
    def test_rejects_all_client_rows_with_extra_or_missing_fields_before_writing_output(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            valid = [""] * len(MODULE.FIELDS)
            too_many = valid.copy()
            too_many[0] = "Cliente; con separatore"
            too_few = valid[:-1]
            too_few[0] = "Cliente incompleto"
            too_few[-1] = "telefono secondario"
            source = root / "clients.txt"
            source.write_text(";".join(too_many) + ";\n" + ";".join(too_few) + "\n", encoding="utf-8")
            notes = root / "notes.txt"
            notes.write_text("", encoding="utf-8")

            with self.assertRaises(MODULE.ClientFormatError) as raised:
                MODULE.generate(source, notes, root / "output")

            self.assertEqual([(1, 30), (2, 28)], [(i.rownum, i.field_count) for i in raised.exception.issues])
            error = io.StringIO()
            with contextlib.redirect_stderr(error):
                MODULE.print_client_format_error(raised.exception)
            self.assertIn("riga 1: Cliente", error.getvalue())
            self.assertIn("1 campo extra", error.getvalue())
            self.assertIn("riga 2: Cliente incompleto", error.getvalue())
            self.assertIn("1 campo mancante", error.getvalue())
            self.assertFalse((root / "output").exists())

    def test_default_inputs_are_only_the_files_in_sibling_txt_data_directory(self):
        input_dir = (SCRIPT.parent / "../txt data").resolve()
        self.assertEqual(input_dir / "clients.txt", MODULE.DEFAULT_CLIENTS_FILE)
        self.assertEqual(input_dir / "tutte_le_note.txt", MODULE.DEFAULT_NOTES_FILE)

    def test_missing_inputs_are_listed_and_stop_generation(self):
        error = io.StringIO()
        missing = [MODULE.DEFAULT_CLIENTS_FILE, MODULE.DEFAULT_NOTES_FILE]
        with patch.object(Path, "is_file", return_value=False), contextlib.redirect_stderr(error), self.assertRaises(SystemExit) as exit_status:
            MODULE.require_input_files(missing)
        self.assertEqual(2, exit_status.exception.code)
        self.assertIn("../txt data/clients.txt", error.getvalue())
        self.assertIn("../txt data/tutte_le_note.txt", error.getvalue())

    def test_windows_launcher_is_location_independent_and_forwards_arguments(self):
        launcher = (SCRIPT.parent / "genera_import.bat").read_text(encoding="utf-8")
        self.assertIn("set \"SCRIPT_DIR=%~dp0\"", launcher)
        self.assertIn("%SCRIPT_DIR%generate_import_sql.py", launcher)
        self.assertIn('py -3 "%GENERATOR%" %*', launcher)
        self.assertIn('python "%GENERATOR%" %*', launcher)
        self.assertIn("pause", launcher)

    def test_derby_launcher_uses_foreign_key_safe_order(self):
        launcher = (SCRIPT.parent / "esegui_import_derby.bat").read_text(encoding="utf-8")
        ordered_files = [
            "import_operatori.sql", "import_clienti.sql", "import_contatti.sql",
            "import_indirizzi.sql", "import_telefoni.sql", "import_email.sql",
            "import_siti.sql", "import_note_interazioni.sql",
        ]
        run_section = launcher[launcher.index('> "%IJ_COMMANDS%"'):launcher.index("echo EXIT;")]
        positions = [run_section.index(f"/{filename}") for filename in ordered_files]
        self.assertEqual(sorted(positions), positions)
        self.assertIn('set "SCRIPT_DIR=%~dp0"', launcher)
        self.assertIn('set "IMPORT_DIR=%SCRIPT_DIR%..\\import scripts"', launcher)
        self.assertIn('set "CLIENTS_DB_URL_IJ=%CLIENTS_DB_URL:\\=/%"', launcher)
        self.assertIn("jdbc:derby:I:/Clizr/Tommaso/Clients", launcher)
        self.assertIn("ij.database=%CLIENTS_DB_URL_IJ%", launcher)
        self.assertIn('ij -p "%IJ_PROPERTIES%" "%IJ_COMMANDS%"', launcher)
        self.assertIn('findstr /C:"ERROR "', launcher)

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
