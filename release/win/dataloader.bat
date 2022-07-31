@echo off
setlocal

CALL %~dp0\initialize.bat %*
set DATALOADER_UBER_JAR_NAME=%~dp0\dataloader-%DATALOADER_VERSION%-uber.jar

:Run
    java -jar %DATALOADER_UBER_JAR_NAME% %*

endlocal
exit /b %errorlevel%