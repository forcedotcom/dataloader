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
    echo To run encrypt.bat, set the JAVA_HOME environment variable to the directory where the Java Runtime Environment (JRE) is installed.
) ELSE (
    IF NOT EXIST "%JAVA_HOME%" (
        echo We couldn't find the Java Runtime Environment (JRE) in directory %JAVA_HOME%. To run process.bat, set the JAVA_HOME environment variable to the directory where the JRE is installed.
    ) ELSE (
        "%JAVA_HOME%\bin\java"  -cp ..\${pom.build.finalName}-uber.jar com.salesforce.dataloader.security.EncryptionUtil %*
    )
)


