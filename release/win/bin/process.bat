@echo off
setlocal

if not [%1]==[] goto run
echo.
echo Usage: process ^<configuration directory^> ^[batch process bean id^]
echo.
echo      configuration directory -- required -- directory that contains configuration files,
echo          i.e. config.properties, process-conf.xml, database-conf.xml
echo.
echo      batch process bean id -- optional -- id of a batch process bean in process-conf.xml,
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
set CONFIG_DIR_OPTION=salesforce.config.dir=%1
set SKIP_COUNT=1

set BATCH_PROCESS_BEAN_ID_OPTION=
if not [%2]==[] (
    set BATCH_PROCESS_BEAN_ID_OPTION=process.name=%2
    set SKIP_COUNT=2
)

set args=
shift
if %SKIP_COUNT% == 2 shift
:start
    if [%1] == [] goto done
    if "%args%" == "" (
        set args=%1=%2
    ) else (
        set args=%args% %1=%2
    )
    shift
    shift
    goto start
:done

CALL %EXE_PATH%\..\dataloader.bat -skipbanner run.mode=batch %CONFIG_DIR_OPTION% %BATCH_PROCESS_BEAN_ID_OPTION% %args%

:end
exit /b %errorlevel%
