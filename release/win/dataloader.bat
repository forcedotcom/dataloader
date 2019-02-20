@echo off
setlocal

echo.
echo ***************************************************************************
echo **            ___  ____ ___ ____   _    ____ ____ ___  ____ ____         **
echo **            |  \ |__|  |  |__|   |    |  | |__| |  \ |___ |__/         **
echo **            |__/ |  |  |  |  |   |___ |__| |  | |__/ |___ |  \         **
echo **                                                                       **
echo **  Dataloder v45 is a Salesforce supported Open Source project to help  **
echo **  you import data to and export data from your Salesforce org.         **
echo **  It requires Zulu OpenJDK 11.0.x to run.                              **
echo **                                                                       **
echo **  Github Project Url:                                                  **
echo **       https://github.com/forcedotcom/dataloader                       **
echo **  Salesforce Documentation:                                            **
echo **       https://help.salesforce.com/articleView?id=data_loader.htm      **
echo **                                                                       **
echo ***************************************************************************
echo.

:CheckOpenJdk11
    echo Dataloader V45 requires Zulu OpenJDK 11 or above. Checking if it is installed...
    for /F "delims=" %%a in ('powershell -Command "foreach($path in (Get-ChildItem Env:Path).value -split ';') { if($path -like '*zulu*') { $jdkDir = $path -split 'bin\\' }}; echo $jdkDir"') do Set "zuluJdkDir=%%a"
    if "%zuluJdkDir%"=="" (
        echo Zulu OpenJDK 11+ is not installed, please download it from https://www.azul.com/downloads/zulu/
        PAUSE
        goto Exit
    ) else (
        set JAVA_HOME=%zuluJdkDir%
        goto Run
    )

:Run
    "%JAVA_HOME%\bin\java"  -jar dataloader-45.0.0-uber.jar salesforce.config.dir=configs

:Exit
    endlocal
