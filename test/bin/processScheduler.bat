@echo off
if not [%1]==[] goto run
echo.
echo Usage: processScheduler ^<configuration directory^>
echo   where
echo      configuration directory -- directory that contains configuration files,
echo                                 i.e. config.properties, process-conf.xml, database-conf.xml
goto end

:run

..\..\installshield\_jvm\bin\java.exe -cp ..\..\jar\DataLoader.jar -Dsalesforce.config.dir=%1 com.salesforce.dataloader.process.ProcessScheduler 

:end
