@echo off
SETLOCAL

CALL "%~dp0util\util.bat" showBanner
CALL "%~dp0util\util.bat" checkJavaVersion
IF "%ERRORLEVEL%" NEQ "0" (
    PAUSE
    GOTO :exit
)
java -cp "%~dp0*" com.salesforce.dataloader.process.DataLoaderRunner %*

:exit
ENDLOCAL
exit /b %ERRORLEVEL%