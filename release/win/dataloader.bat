@echo off
setlocal

echo.
echo ******************************************************************
echo **           Dataloader V45                                     **
echo **                                                              **
echo ** The Data Loader helps you to perform bulk operations data in **
echo ** your Force.com environment: insert, update, upsert and       **
echo ** delete data, as well as export data from Force.com objects.  **
echo ** You can use .csv files or relational databases as the source **
echo ** or target for this data movement.                            **
echo **                                                              **
echo ** Java requirement: Zulu OpenJDK version 11 or higher          **
echo **                                                              **
echo ** https://github.com/forcedotcom/dataloader                    **
echo ******************************************************************
echo.

:CheckOpenJdk11
    echo Dataloader V45 requires Zulu OpenJDK 11 or above. Checking if it is installed...
    for /F "delims=" %%a in ('powershell -Command "foreach($path in (Get-ChildItem Env:Path).value -split ';') { if($path -like '*zulu*') { $jdkDir = $path -split 'bin\\' }}; echo $jdkDir"') do Set "zuluJdkDir=%%a"
    if "%zuluJdkDir%"=="" (
        echo Zulu OpenJDK 11 is not installed, please download it from https://www.azul.com/downloads/zulu/
        PAUSE
        goto Exit
    ) else (
        echo Zulu OpenJDK 11 is installed in '%zuluJdkDir%' Checking if JAVA_HOME is set to correct path...
        if "%JAVA_HOME%"=="%zuluJdkDir%" (
            echo JAVA_HOME is set correctly
        ) ELSE (
            echo JAVA_HOME is currently set to '%JAVA_HOME%', for this session only we will change it to '%zuluJdkDir%'
            echo Note that JAVA_HOME is NOT permanentely changed, the change is ONLY for this session. Changing 'JAVA_HOME'...
            set JAVA_HOME=%zuluJdkDir%
        )
        goto Run
    )

:Run
    echo.
    echo Using Java from '%JAVA_HOME%bin' Dataloader starting...
    "%JAVA_HOME%\bin\java"  -jar dataloader-45.0.0-uber.jar salesforce.config.dir=configs

:Exit
    endlocal
