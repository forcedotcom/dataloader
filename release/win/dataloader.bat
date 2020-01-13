@echo off
setlocal

echo.
echo *************************************************************************
echo **            ___  ____ ___ ____   _    ____ ____ ___  ____ ____       **
echo **            ^|  \ ^|__^|  ^|  ^|__^|   ^|    ^|  ^| ^|__^| ^|  \ ^|___ ^|__/       **
echo **            ^|__/ ^|  ^|  ^|  ^|  ^|   ^|___ ^|__^| ^|  ^| ^|__/ ^|___ ^|  \       **
echo **                                                                     **
echo **  Data Loader v47 is a Salesforce supported Open Source project to   **
echo **  help you import data to and export data from your Salesforce org.  **
echo **  It requires Oracle Java JDK 11.0.x to run.                         **
echo **                                                                     **
echo **  Github Project Url:                                                **
echo **       https://github.com/forcedotcom/dataloader                     **
echo **  Salesforce Documentation:                                          **
echo **       https://help.salesforce.com/articleView?id=data_loader.htm    **
echo **                                                                     **
echo *************************************************************************
echo.

:CheckOpenJdk11
    REM: Modified Bat file to work with vendor Install of Oracle JDK 11
    REM: Todo, we need to improve this to better detect JAVA 11+, rather than depending on default zulu installed folder.
    echo Data Loader requires Oracle JDK 11. Checking if it is installed...
    for /F "delims=" %%a in ('powershell -Command "foreach($path in (Get-ChildItem Env:Path).value -split ';') { if($path -like '*jdk-*') { $jdkDir = $path -split 'bin' }}; echo $jdkDir"') do Set "JAVA_HOME=%%a"
    if "%JAVA_HOME%"=="" (
        echo OpenJDK is not installed. Download JDK version 11 or Higher for Windows here:
        echo    https://www.oracle.com/technetwork/java/javase/downloads/index.html
        PAUSE
        goto Exit
    )

:Run
    "%JAVA_HOME%\bin\java"  -jar dataloader-47.0.0-uber.jar salesforce.config.dir=configs

:Exit
    endlocal
