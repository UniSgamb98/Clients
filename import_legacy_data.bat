@echo off
setlocal EnableExtensions
set "ROOT=%~dp0"

call :run_import
set "FINAL_EXIT=%ERRORLEVEL%"
goto :show_result

:show_result
echo.
if "%FINAL_EXIT%"=="0" (
  echo Operazione terminata correttamente.
) else (
  echo Operazione terminata con errori. Codice: %FINAL_EXIT%.
  echo La finestra rimarra aperta per permetterti di leggere il messaggio.
  if exist "%ROOT%import_legacy_data.log" echo Log completo: "%ROOT%import_legacy_data.log"
)
echo.
echo Premi un tasto per chiudere questa finestra...
pause >nul
endlocal & exit /b %FINAL_EXIT%

:run_import
set "NOTES_DIR=%ROOT%notes to merge"
set "SCRIPTS_DIR=%ROOT%scripts"
set "DATA_DIR=%ROOT%txt data"
set "IMPORT_DIR=%ROOT%import scripts"
set "MERGE_PS1=%NOTES_DIR%\merge.ps1"
set "MERGED_NOTES=%NOTES_DIR%\tutte_le_note.txt"
set "GENERATOR=%SCRIPTS_DIR%\generate_import_sql.py"
set "IMPORT_SQL=%IMPORT_DIR%\import all.sql"
set "IMPORT_LOG=%ROOT%import_legacy_data.log"
set "DERBY_LIB=C:\Apache\db-derby-10.17.1.0-bin\lib"
set "DERBY_JAR=%DERBY_LIB%\derbyrun.jar"

if defined CLIENTS_DERBY_URL (
  set "DERBY_URL=%CLIENTS_DERBY_URL%"
) else (
  set "DERBY_URL=jdbc:derby://localhost:1527/Clients;user=APP;password=pw"
)

if not exist "%MERGE_PS1%" (
  echo ERRORE: file non trovato: "%MERGE_PS1%"
  exit /b 1
)

if not exist "%GENERATOR%" (
  echo ERRORE: file non trovato: "%GENERATOR%"
  exit /b 1
)

if not exist "%DATA_DIR%\clients.txt" (
  echo ERRORE: file non trovato: "%DATA_DIR%\clients.txt"
  exit /b 1
)

if not exist "%DERBY_JAR%" (
  echo ERRORE: derbyrun.jar non trovato: "%DERBY_JAR%"
  exit /b 1
)

echo [1/3] Eseguo merge.ps1 da "%NOTES_DIR%"...
pushd "%NOTES_DIR%" || exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -File "%MERGE_PS1%"
set "MERGE_EXIT=%ERRORLEVEL%"
popd
if not "%MERGE_EXIT%"=="0" (
  echo ERRORE: merge.ps1 terminato con codice %MERGE_EXIT%.
  exit /b %MERGE_EXIT%
)

if not exist "%MERGED_NOTES%" (
  echo ERRORE: merge.ps1 non ha generato "%MERGED_NOTES%".
  exit /b 1
)
copy /Y "%MERGED_NOTES%" "%DATA_DIR%\tutte_le_note.txt" >nul
if errorlevel 1 (
  echo ERRORE: copia di tutte_le_note.txt non riuscita.
  exit /b 1
)

echo [2/3] Rigenero gli script SQL con generate_import_sql.py...
pushd "%SCRIPTS_DIR%" || exit /b 1
where py >nul 2>nul
if errorlevel 1 (
  python "%GENERATOR%"
) else (
  py -3 "%GENERATOR%"
)
set "PY_EXIT=%ERRORLEVEL%"
popd
if not "%PY_EXIT%"=="0" (
  echo ERRORE: generate_import_sql.py terminato con codice %PY_EXIT%.
  exit /b %PY_EXIT%
)

if not exist "%IMPORT_SQL%" (
  echo ERRORE: script master non generato: "%IMPORT_SQL%"
  exit /b 1
)

echo [3/3] Importo gli script in %DERBY_URL%...
set "IJ_SQL=%TEMP%\ij_import_all_%RANDOM%.sql"
(
  echo connect '%DERBY_URL%';
  echo run 'import all.sql';
  echo exit;
) > "%IJ_SQL%"

pushd "%IMPORT_DIR%" || exit /b 1
java -jar "%DERBY_JAR%" ij "%IJ_SQL%" > "%IMPORT_LOG%" 2>&1
set "IJ_EXIT=%ERRORLEVEL%"
popd
del /Q "%IJ_SQL%" >nul 2>nul
type "%IMPORT_LOG%"
echo.
echo Log completo: "%IMPORT_LOG%"
findstr /R /C:"^ERROR [0-9A-Z][0-9A-Z]*:" "%IMPORT_LOG%" >nul
if not errorlevel 1 set "IJ_EXIT=1"

if not "%IJ_EXIT%"=="0" (
  echo ERRORE: Derby IJ terminato con codice %IJ_EXIT%.
  exit /b %IJ_EXIT%
)

echo Import completato.
exit /b 0
