@echo off
SETLOCAL

CALL "%~dp0..\util\util.bat" checkJavaVersion
IF "%ERRORLEVEL%" NEQ "0" (
    PAUSE
    GOTO :exit
)
java -jar "%~dp0..\dataloader-%DATALOADER_VERSION%-uber.jar" %* run.mode=batch

:exit
ENDLOCAL
EXIT /b %ERRORLEVEL%