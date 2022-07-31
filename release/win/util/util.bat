REM call the function specified in 1st param and return the errorlevel set by the function
REM
CALL :initVars
CALL :%~1
EXIT /b %ERRORLEVEL%

:initVars
    SET DATALOADER_VERSION=@@FULL_VERSION@@
    FOR /f "tokens=1 delims=." %%a IN ("%DATALOADER_VERSION%") DO (
      SET DATALOADER_SHORT_VERSION=%%a
    )
    SET MIN_JAVA_VERSION=@@MIN_JAVA_VERSION@@
    IF NOT "%DATALOADER_JAVA_HOME%" == "" (
        SET "JAVA_HOME=%DATALOADER_JAVA_HOME%"
    )
    EXIT /b 0

:showBanner
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
    EXIT /b 0

:checkJavaVersion
    echo Data Loader requires Java JRE %MIN_JAVA_VERSION% or later. Checking if it is installed...

    SET "PATH=%JAVA_HOME%\bin\;%PATH%;"

    java -version 1>nul 2>nul || (
        GOTO :NoJavaErrorExit
    )

    FOR /f "tokens=3" %%a IN ('java -version 2^>^&1 ^| FINDSTR /i "version"') DO (
        SET JAVA_FULL_VERSION=%%a
    )
    SET JAVA_FULL_VERSION=%JAVA_FULL_VERSION:"=%

    FOR /f "tokens=1 delims=." %%m IN ("%JAVA_FULL_VERSION%") DO (
        SET /A JAVA_MAJOR_VERSION=%%m
    )

    IF %JAVA_MAJOR_VERSION% LSS %MIN_JAVA_VERSION% (
        GOTO :JavaVersionErrorExit
    )
    EXIT /b 0

:NoJavaErrorExit
    echo Did not find java command.
    echo Either Java JRE %MIN_JAVA_VERSION% or later is not installed or PATH environment does not
    echo include the folder containing java executable.
    GOTO :CommonJavaErrorExit

:JavaVersionErrorExit
    echo Found Java JRE version %JAVA_FULL_VERSION% whereas Data Loader requires Java JRE %MIN_JAVA_VERSION% or later.
    GOTO :CommonJavaErrorExit

:CommonJavaErrorExit
    echo For example, download and install Zulu OpenJDK %MIN_JAVA_VERSION% or later JRE for Windows from here:
    echo    https://www.azul.com/downloads/zulu/zulu-windows/
    echo After the installation, update PATH environment variable by
    echo    - removing the path to older JRE's bin folder from PATH environment variable
    echo    - adding ^<full path to the JRE base folder^>\bin to PATH environment variable
    PAUSE
    EXIT /b -1