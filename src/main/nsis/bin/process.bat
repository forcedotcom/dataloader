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
echo          If process name is not specified, the parameter values from config.properties
echo          will be used to run the process instead of process-conf.xml,
echo          for example:
echo.
echo              process ../myconfigdir
echo.

goto end

:run
set PROCESS_OPTION=
if not [%2]==[] set PROCESS_OPTION=process.name=%2

..\Java\bin\java.exe -cp ..\${pom.build.finalName}-jar-with-dependencies.jar -Dsalesforce.config.dir=%1 com.salesforce.dataloader.process.ProcessRunner %PROCESS_OPTION%

:end
