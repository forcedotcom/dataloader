@echo off
setlocal

set DATALOADER_VERSION=@@FULL_VERSION@@
for /f "tokens=1 delims=." %%a in ("%DATALOADER_VERSION%") do (
  set DATALOADER_SHORT_VERSION=%%a
)
set DATALOADER_UBER_JAR_NAME=dataloader-%DATALOADER_VERSION%-uber.jar
set MIN_JAVA_VERSION=@@MIN_JAVA_VERSION@@

echo.
echo *************************************************************************
echo **            ___  ____ ___ ____   _    ____ ____ ___  ____ ____       **
echo **            ^|  \ ^|__^|  ^|  ^|__^|   ^|    ^|  ^| ^|__^| ^|  \ ^|___ ^|__/       **
echo **            ^|__/ ^|  ^|  ^|  ^|  ^|   ^|___ ^|__^| ^|  ^| ^|__/ ^|___ ^|  \       **
echo **                                                                     **
echo **  Data Loader v%DATALOADER_SHORT_VERSION% is a Salesforce supported Open Source project to   **
echo **  help you import data to and export data from your Salesforce org.  **
echo **  It requires Java JRE %MIN_JAVA_VERSION% or later to run.                           **
echo **                                                                     **
echo **  Github Project Url:                                                **
echo **       https://github.com/forcedotcom/dataloader                     **
echo **  Salesforce Documentation:                                          **
echo **       https://help.salesforce.com/articleView?id=data_loader.htm    **
echo **                                                                     **
echo *************************************************************************
echo.

:CheckMinJRE
    echo Data Loader requires Java JRE %MIN_JAVA_VERSION% or later. Checking if it is installed...

    PATH=%PATH%;%JAVA_HOME%\bin\;%ZULU_JAVA_HOME%\bin\;

    java -version 1>nul 2>nul || (
        goto NoJavaErrorExit
    )

    for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_FULL_VERSION=%%a
    )
    set JAVA_FULL_VERSION=%JAVA_FULL_VERSION:"=%

    for /f "tokens=1 delims=." %%m in ("%JAVA_FULL_VERSION%") do (
        set /A JAVA_MAJOR_VERSION=%%m
    )

    if %JAVA_MAJOR_VERSION% LSS %MIN_JAVA_VERSION% (
        goto JavaVersionErrorExit
    )

:Run
    java -D"org.eclipse.swt.browser.IEVersion=12001" -jar %DATALOADER_UBER_JAR_NAME% %*
    goto SuccessExit

:SuccessExit
    endlocal
    exit /b %errorlevel%

:NoJavaErrorExit
    echo Did not find java command.
    echo Either Java JRE %MIN_JAVA_VERSION% or later is not installed or PATH environment does not
    echo include the folder containing java executable.
    goto CommonJavaErrorExit

:JavaVersionErrorExit
    echo Found Java JRE version %JAVA_FULL_VERSION% whereas Data Loader requires Java JRE %MIN_JAVA_VERSION% or later.
    goto CommonJavaErrorExit

:CommonJavaErrorExit
    echo For example, download and install Zulu OpenJDK %MIN_JAVA_VERSION% or later JRE for Windows from here:
    echo    https://www.azul.com/downloads/zulu/zulu-windows/
    echo After the installation, update PATH environment variable by
    echo    - removing the path to older JRE's bin folder from PATH environment variable
    echo    - adding ^<full path to the JRE base folder^>\bin to PATH environment variable
    endlocal
    PAUSE
    exit /b 1
