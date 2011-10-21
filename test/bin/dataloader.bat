@echo off

if not [%1]==[] goto run
echo "Usage: dataloader <configuration directory>"
goto end

:run

REM Set path to the required windows dll's
set PATH=%PATH%;..\..\lib
..\..\installshield\_jvm\bin\java.exe -cp ../../jar/DataLoader.jar -Dsalesforce.config.dir=%1 com.salesforce.dataloader.process.DataLoaderRunner 

if errorlevel 1 echo Error starting Apex Data Loader.

:end

