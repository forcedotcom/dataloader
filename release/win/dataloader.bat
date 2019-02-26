@echo off
setlocal

echo.
echo *************************************************************************
echo **            ___  ____ ___ ____   _    ____ ____ ___  ____ ____       **
echo **            ^|  \ ^|__^|  ^|  ^|__^|   ^|    ^|  ^| ^|__^| ^|  \ ^|___ ^|__/       **
echo **            ^|__/ ^|  ^|  ^|  ^|  ^|   ^|___ ^|__^| ^|  ^| ^|__/ ^|___ ^|  \       **
echo **                                                                     **
echo **  Data Loader v45 is a Salesforce supported Open Source project to   **
echo **  help you import data to and export data from your Salesforce org.  **
echo **  It requires Zulu OpenJDK 11.0.x to run.                            **
echo **                                                                     **
echo **  Github Project Url:                                                **
echo **       https://github.com/forcedotcom/dataloader                     **
echo **  Salesforce Documentation:                                          **
echo **       https://help.salesforce.com/articleView?id=data_loader.htm    **
echo **                                                                     **
echo *************************************************************************
echo.

:CheckOpenJdk11
    REM: Todo, we need to improve this to better detect JAVA 11+, rather than depending on default zulu installed folder.
    echo Data Loader requires Zulu OpenJDK 11. Checking if it is installed...
    for /F "delims=" %%a in ('powershell -Command "foreach($path in (Get-ChildItem Env:Path).value -split ';') { if($path -like '*zulu*') { $jdkDir = $path -split 'bin\\' }}; echo $jdkDir"') do Set "ZULU_JAVA_HOME=%%a"
    if "%ZULU_JAVA_HOME%"=="" (
        echo Zulu OpenJDK is not installed. Download Zulu OpenJDK 11 for Windows here:
        echo    https://www.azul.com/downloads/zulu/zulu-windows/
        PAUSE
        goto Exit
    )

:Run
    "%ZULU_JAVA_HOME%\bin\java"  -jar dataloader-45.0.0-uber.jar salesforce.config.dir=configs

:Exit
    endlocal
