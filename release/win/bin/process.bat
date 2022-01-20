@echo off
if not [%1]==[] goto run
echo.
echo Usage: process ^<configuration directory^> ^[process name^]
echo.
echo      configuration directory -- directory that contains configuration files,
echo          i.e. config.properties, process-conf.xml, database-conf.xml
echo.
echo      process name -- optional name of a batch process bean in process-conf.xml,
echo          for example:
echo.
echo              process ../myconfigdir AccountInsert
echo.
echo          If process bean id is not specified, the value of the property process.name in config.properties
echo          will be used to run the process instead of process-conf.xml,
echo          for example:
echo.
echo              process ../myconfigdir
echo.

goto end

:run
set EXE_PATH=%~dp0
set DATALOADER_VERSION=@@FULL_VERSION@@

set PROCESS_BEAN_ID_OPTION=
if not [%2]==[] set PROCESS_BEAN_ID_OPTION=process.name=%2

IF NOT "%DATALOADER_JAVA_HOME%" == "" (
    set JAVA_HOME="%DATALOADER_JAVA_HOME%"
)

IF "%JAVA_HOME%" == "" (
    echo To run process.bat, set the JAVA_HOME environment variable to the directory where the Java Runtime Environment ^(JRE^) is installed.
) ELSE (
    IF NOT EXIST "%JAVA_HOME%" (
        echo We couldn't find the Java Runtime Environment ^(JRE^) in directory "%JAVA_HOME%". To run process.bat, set the JAVA_HOME environment variable to the directory where the JRE is installed.
    ) ELSE (
        "%JAVA_HOME%\bin\java" -jar "%EXE_PATH%\..\dataloader-%DATALOADER_VERSION%-uber.jar" run.mode=batch salesforce.config.dir=%1 %PROCESS_BEAN_ID_OPTION%
    )
)

:end
exit /b %errorlevel%
