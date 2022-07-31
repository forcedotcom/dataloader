@echo off
SETLOCAL

IF NOT "%1"=="" GOTO :run
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
GOTO :exit

:run
SET DATALOADER_VERSION=@@FULL_VERSION@@
SET "CONFIG_DIR_OPTION=salesforce.config.dir=%1"
SET SKIP_ARGS_COUNT=1

SET BATCH_PROCESS_BEAN_ID_OPTION=
IF NOT "%2"=="" (
    set BATCH_PROCESS_BEAN_ID_OPTION=process.name=%2
    set SKIP_ARGS_COUNT=2
)

REM first argument is mandatory. Skip it because it is handled differently from other args.
SHIFT

REM skip the second argument if provided because it is handled differently from other args.
IF "%SKIP_ARGS_COUNT%" == "2" SHIFT

REM preserve rest of the command line arguments in ARGS variable
SET ARGS=
:preserveNextArg
    IF "%1" == "" GOTO :afterPreserveArgs
    IF "%ARGS%" == "" (
        SET ARGS=%1=%2
    ) ELSE (
        SET ARGS=%ARGS% %1=%2
    )
    SHIFT
    SHIFT
    GOTO :preserveNextArg

:afterPreserveArgs
CALL %~dp0..\dataloader.bat -skipbanner run.mode=batch %CONFIG_DIR_OPTION% %BATCH_PROCESS_BEAN_ID_OPTION% %ARGS%

:exit
ENDLOCAL
EXIT /b %ERRORLEVEL%