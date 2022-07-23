@echo off
setlocal

set EXE_PATH=%~dp0
set DATALOADER_VERSION=@@FULL_VERSION@@

IF NOT "%DATALOADER_JAVA_HOME%" == "" (
    set JAVA_HOME=%DATALOADER_JAVA_HOME%
)

IF "%JAVA_HOME%" == "" (
    echo To run encrypt.bat, set the JAVA_HOME environment variable to the directory where the Java Runtime Environment ^(JRE^) is installed.
) ELSE (
    PATH=%JAVA_HOME%\bin\;%PATH%;
    java -cp "%EXE_PATH%\..\dataloader-%DATALOADER_VERSION%-uber.jar" com.salesforce.dataloader.security.EncryptionUtil %*
)
endlocal