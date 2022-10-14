@echo off
SETLOCAL

CALL "%~dp0..\util\util.bat" checkJavaVersion
IF "%ERRORLEVEL%" NEQ "0" (
    PAUSE
    GOTO :exit
)
java -cp "%~dp0../*" com.salesforce.dataloader.security.EncryptionUtil %*

:exit
ENDLOCAL
EXIT /b %ERRORLEVEL%