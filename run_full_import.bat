@echo off
setlocal EnableExtensions

set "ROOT=%~dp0"
set "NOTES_DIR=%ROOT%notes to merge"
set "SCRIPTS_DIR=%ROOT%scripts"
set "MERGE_PS1=%NOTES_DIR%\merge.ps1"
set "GENERATOR=%SCRIPTS_DIR%\generate_import_sql.py"
set "IMPORT_SQL=%SCRIPTS_DIR%\import all.sql"
set "DERBY_LIB=C:\Apache\db-derby-10.17.1.0-bin\lib"
set "DERBY_JAR=%DERBY_LIB%\derbyrun.jar"
set "DERBY_URL=jdbc:derby:I:\Clizr\Tommaso\Clients;user=APP;password=pw"

if not exist "%MERGE_PS1%" (
  echo ERRORE: file non trovato: "%MERGE_PS1%"
  exit /b 1
)

if not exist "%GENERATOR%" (
  echo ERRORE: file non trovato: "%GENERATOR%"
  exit /b 1
)

if not exist "%IMPORT_SQL%" (
  echo ERRORE: file non trovato: "%IMPORT_SQL%"
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

echo [2/3] Rigenero gli script SQL con generate_import_sql.py da "%SCRIPTS_DIR%"...
pushd "%SCRIPTS_DIR%" || exit /b 1
where py >nul 2>nul
if "%ERRORLEVEL%"=="0" (
  py -3 "%GENERATOR%"
) else (
  python "%GENERATOR%"
)
set "PY_EXIT=%ERRORLEVEL%"
popd
if not "%PY_EXIT%"=="0" (
  echo ERRORE: generate_import_sql.py terminato con codice %PY_EXIT%.
  exit /b %PY_EXIT%
)

echo [3/3] Apro Derby IJ ed eseguo "%IMPORT_SQL%"...
set "PS1=%TEMP%\ij_import_all.ps1"

(
  echo $ErrorActionPreference = 'Stop'
  echo $props = Join-Path $env:TEMP 'ij-auto.properties'
  echo Set-Content -Path $props -Value 'ij.database=%DERBY_URL%' -Encoding ASCII
  echo Set-Location '%SCRIPTS_DIR%'
  echo java -jar '%DERBY_JAR%' ij -p $props 'import all.sql'
) > "%PS1%"

start "Derby IJ import all" powershell -NoExit -ExecutionPolicy Bypass -File "%PS1%"

endlocal
