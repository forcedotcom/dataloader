@echo off
SETLOCAL

IF NOT "%~1"=="-skipbanner" (
        CALL %~dp0\util\util.bat showBanner
    )
)
CALL %~dp0\util\util.bat checkJavaVersion
IF "%ERRORLEVEL%" NEQ "0" GOTO :exit

java -jar "%~dp0dataloader-%DATALOADER_VERSION%-uber.jar" %*

:exit
ENDLOCAL
exit /b %ERRORLEVEL%