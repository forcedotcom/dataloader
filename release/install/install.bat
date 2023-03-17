@echo off
setlocal

SET ERRORLEVEL=0
CALL "%~dp0util\util.bat" :checkJavaVersion

java -cp "%~dp0*" com.salesforce.dataloader.install.Installer %*

:exit
    echo.
    echo Data Loader installation is quitting.
    endlocal
    PAUSE
    EXIT /b %ERRORLEVEL%