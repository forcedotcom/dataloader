@ECHO OFF
REM call the function specified in 1st param and return the errorlevel set by the function
REM
CALL :initVars
CALL %*
EXIT /b %ERRORLEVEL%

:initVars
    SET DATALOADER_VERSION=@@FULL_VERSION@@
    EXIT /b 0

:runDataLoader
    java -cp "%~dp0..\*" com.salesforce.dataloader.process.DataLoaderRunner %*
    EXIT /b %ERRORLEVEL%

REM Shortcut files have .lnk extension
:CreateShortcut
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""%~1""""); $Shortcut.WorkingDirectory = """"%~2""""; $Shortcut.TargetPath = """"%~2\dataloader.bat""""; $Shortcut.IconLocation = """"%~2\dataloader.ico""""; $Shortcut.WindowStyle=7; $Shortcut.Save()"
    EXIT /b 0
