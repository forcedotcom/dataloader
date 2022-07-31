@echo off
SETLOCAL

CALL %~dp0..\util\util.bat checkJavaVersion
IF "%ERRORLEVEL%" NEQ "0" EXIT /b %ERRORLEVEL%
java -cp "%~dp0..\dataloader-%DATALOADER_VERSION%-uber.jar" com.salesforce.dataloader.security.EncryptionUtil %*

ENDLOCAL