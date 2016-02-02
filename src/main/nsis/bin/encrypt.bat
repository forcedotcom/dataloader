@echo off

IF "%JAVA_HOME%" == "" (
    for /f "tokens=*" %%i in ('${pom.build.finalName}-java-home.exe') do (
        IF EXIST "%%i" (
            set JAVA_HOME=%%i
        ) ELSE (
            echo %%i
        )
    )
)

IF "%JAVA_HOME%" == "" (
    echo Please set JAVA_HOME before running the encrypt.bat
) ELSE (
    IF NOT EXIST "%JAVA_HOME%" (
        echo Could not find "%JAVA_HOME%"
    ) ELSE (
        "%JAVA_HOME%\bin\java"  -cp ..\${pom.build.finalName}-uber.jar com.salesforce.dataloader.security.EncryptionUtil %*
    )
)


