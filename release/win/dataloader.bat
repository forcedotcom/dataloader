@echo off
SETLOCAL

CALL "%~dp0util\util.bat" showBanner
CALL "%~dp0util\util.bat" checkJavaVersion
IF "%ERRORLEVEL%" NEQ "0" GOTO :exit
java -jar "%~dp0dataloader-%DATALOADER_VERSION%-uber.jar" %*

:exit
ENDLOCAL
exit /b %ERRORLEVEL%