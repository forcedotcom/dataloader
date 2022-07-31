@echo off
SETLOCAL

IF NOT [%1]==[] GOTO run
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

GOTO end

:run
SET EXE_PATH=%~dp0
SET DATALOADER_VERSION=@@FULL_VERSION@@
SET CONFIG_DIR_OPTION=salesforce.config.dir=%1
SET SKIP_COUNT=1

SET BATCH_PROCESS_BEAN_ID_OPTION=
IF NOT [%2]==[] (
    set BATCH_PROCESS_BEAN_ID_OPTION=process.name=%2
    set SKIP_COUNT=2
)

SET args=
SHIFT
IF %SKIP_COUNT% == 2 SHIFT
:start
    IF [%1] == [] GOTO done
    IF "%args%" == "" (
        SET args=%1=%2
    ) ELSE (
        SET args=%args% %1=%2
    )
    SHIFT
    SHIFT
    goto start
:done

CALL %EXE_PATH%\..\dataloader.bat -skipbanner run.mode=batch %CONFIG_DIR_OPTION% %BATCH_PROCESS_BEAN_ID_OPTION% %args%

:end
ENDLOCAL
EXIT /b %errorlevel%
