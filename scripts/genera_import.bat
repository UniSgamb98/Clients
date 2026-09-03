@echo off
setlocal

rem Usa sempre lo script Python che si trova accanto a questo file BAT.
rem In questo modo il comando funziona anche se viene avviato da un'altra cartella.
set "SCRIPT_DIR=%~dp0"
set "GENERATOR=%SCRIPT_DIR%generate_import_sql.py"

if not exist "%GENERATOR%" (
    echo ERRORE: generatore non trovato: "%GENERATOR%"
    echo Conservare genera_import.bat e generate_import_sql.py nella stessa cartella.
    exit /b 2
)

rem Il launcher "py" e' normalmente disponibile nelle installazioni Python per Windows.
where py >nul 2>nul
if not errorlevel 1 goto :run_py

rem Fallback per installazioni che espongono direttamente python.exe nel PATH.
where python >nul 2>nul
if not errorlevel 1 goto :run_python

echo ERRORE: Python 3 non trovato.
echo Installare Python 3 e abilitare il launcher "py" oppure aggiungere python.exe al PATH.
exit /b 3

:run_py
py -3 "%GENERATOR%" %*
set "RESULT=%ERRORLEVEL%"
goto :done

:run_python
python "%GENERATOR%" %*
set "RESULT=%ERRORLEVEL%"
goto :done

:done
if not "%RESULT%"=="0" (
    echo ERRORE: generazione interrotta con codice %RESULT%.
    echo.
    pause
    exit /b %RESULT%
)

echo.
echo Generazione completata correttamente.
exit /b 0
