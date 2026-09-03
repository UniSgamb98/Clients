@echo off
setlocal EnableExtensions

rem Configurazione predefinita. I valori possono essere sovrascritti tramite
rem le variabili di ambiente DERBY_LIB, CLIENTS_DB_URL, CLIENTS_DB_USER e
rem CLIENTS_DB_PASSWORD prima di avviare questo file.
if not defined DERBY_LIB set "DERBY_LIB=C:\Apache\db-derby-10.17.1.0-bin\lib"
if not defined CLIENTS_DB_URL set "CLIENTS_DB_URL=jdbc:derby:I:/Clizr/Tommaso/Clients"
if not defined CLIENTS_DB_USER set "CLIENTS_DB_USER=APP"
if not defined CLIENTS_DB_PASSWORD set "CLIENTS_DB_PASSWORD=pw"

set "SCRIPT_DIR=%~dp0"
set "IMPORT_DIR=%SCRIPT_DIR%..\import scripts"
set "DERBY_RUN=%DERBY_LIB%\derbyrun.jar"
set "IJ_PROPERTIES=%TEMP%\clients-import-ij.properties"
set "IJ_COMMANDS=%TEMP%\clients-import-ij.sql"
set "IMPORT_LOG=%IMPORT_DIR%\import_execution.log"

if not exist "%DERBY_RUN%" (
    echo ERRORE: derbyrun.jar non trovato:
    echo "%DERBY_RUN%"
    exit /b 2
)

where java >nul 2>nul
if errorlevel 1 (
    echo ERRORE: java.exe non trovato nel PATH.
    exit /b 3
)

call :require_sql "import_operatori.sql" || exit /b 4
call :require_sql "import_clienti.sql" || exit /b 4
call :require_sql "import_contatti.sql" || exit /b 4
call :require_sql "import_indirizzi.sql" || exit /b 4
call :require_sql "import_telefoni.sql" || exit /b 4
call :require_sql "import_email.sql" || exit /b 4
call :require_sql "import_siti.sql" || exit /b 4
call :require_sql "import_note_interazioni.sql" || exit /b 4

rem IJ accetta anche slash in stile Unix: evitano ambiguita nei percorsi RUN.
set "IMPORT_DIR_IJ=%IMPORT_DIR:\=/%"
rem Nei file Java .properties il backslash e' un carattere di escape. Convertirlo
rem evita che I:\Clizr\Tommaso\Clients diventi erroneamente I:ClizrTommasoClients.
set "CLIENTS_DB_URL_IJ=%CLIENTS_DB_URL:\=/%"

> "%IJ_PROPERTIES%" echo ij.database=%CLIENTS_DB_URL_IJ%;user=%CLIENTS_DB_USER%;password=%CLIENTS_DB_PASSWORD%

> "%IJ_COMMANDS%" (
    echo RUN '%IMPORT_DIR_IJ%/import_operatori.sql';
    echo RUN '%IMPORT_DIR_IJ%/import_clienti.sql';
    echo RUN '%IMPORT_DIR_IJ%/import_contatti.sql';
    echo RUN '%IMPORT_DIR_IJ%/import_indirizzi.sql';
    echo RUN '%IMPORT_DIR_IJ%/import_telefoni.sql';
    echo RUN '%IMPORT_DIR_IJ%/import_email.sql';
    echo RUN '%IMPORT_DIR_IJ%/import_siti.sql';
    echo RUN '%IMPORT_DIR_IJ%/import_note_interazioni.sql';
    echo EXIT;
)

echo Connessione a Derby e importazione in corso...
echo Database: %CLIENTS_DB_URL_IJ%
echo Log: "%IMPORT_LOG%"
echo.

java -jar "%DERBY_RUN%" ij -p "%IJ_PROPERTIES%" "%IJ_COMMANDS%" > "%IMPORT_LOG%" 2>&1
set "RESULT=%ERRORLEVEL%"

del "%IJ_PROPERTIES%" >nul 2>nul
del "%IJ_COMMANDS%" >nul 2>nul

if not "%RESULT%"=="0" (
    echo ERRORE: IJ ha terminato con codice %RESULT%.
    echo Controllare il log: "%IMPORT_LOG%"
    exit /b %RESULT%
)

findstr /C:"ERROR " /C:"ERROR:" /C:"Exception" "%IMPORT_LOG%" >nul 2>nul
if not errorlevel 1 (
    echo ERRORE: Derby ha segnalato uno o piu errori.
    echo Controllare il log: "%IMPORT_LOG%"
    exit /b 5
)

echo Importazione completata correttamente.
echo Log disponibile in: "%IMPORT_LOG%"
exit /b 0

:require_sql
if exist "%IMPORT_DIR%\%~1" exit /b 0
echo ERRORE: file SQL non trovato: "%IMPORT_DIR%\%~1"
exit /b 1
