REM call the function specified in 1st param and return the errorlevel set by the function
REM
CALL :initVars
CALL %*
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
    echo **                                                                     **
    echo **                     Salesforce Data Loader                          **
    echo **                     ======================                          **
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
    CALL :initVars
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
    echo.
    GOTO :exitWithJavaDownloadMessage

:JavaVersionErrorExit
    echo Found Java JRE version %JAVA_FULL_VERSION% whereas Data Loader requires Java JRE %MIN_JAVA_VERSION% or later.
    GOTO :exitWithJavaDownloadMessage

:exitWithJavaDownloadMessage
    echo Java JRE %MIN_JAVA_VERSION% or later is not installed or DATALOADER_JAVA_HOME environment variable is not set.
    echo For example, download and install Zulu JRE %MIN_JAVA_VERSION% or later from here:
    echo     https://www.azul.com/downloads/
    echo.
    echo After the installation, set DATALOADER_JAVA_HOME environment variable to the value
    echo ^<full path to the JRE installation folder^>
    echo.
    EXIT /b -1

# Shortcut files have .lnk extension
:CreateShortcut
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""%~1""""); $Shortcut.WorkingDirectory = """"%~2""""; $Shortcut.TargetPath = """"%~2\dataloader.bat""""; $Shortcut.IconLocation = """"%~2\dataloader.ico""""; $Shortcut.WindowStyle=7; $Shortcut.Save()"
    EXIT /b 0

REM Note for label renaming - promptForShortcut sets "yesDestination" var based on naming convention.
:createStartMenuShortcut
    IF NOT EXIST "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce\" (
        mkdir "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce"
    )
    CALL :CreateShortcut "$Home\Desktop\Dataloader.lnk" "%~1"
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce\Dataloader %DATALOADER_VERSION%.lnk" >nul
    EXIT /b 0

REM Note for label renaming - promptForShortcut sets "yesDestination" var based on naming convention.
:createDesktopShortcut
    CALL :CreateShortcut "$Home\Desktop\Dataloader.lnk" "%~1"
    for /f "usebackq tokens=3*" %%D IN (`reg query "HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\User Shell Folders" /v Desktop`) do set DESKTOP_DIR=%%D
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%DESKTOP_DIR%\Dataloader %DATALOADER_VERSION%.lnk" >nul
    EXIT /b 0
    
:doDeleteExistingDir
    echo Deleting existing Data Loader v%DATALOADER_VERSION%...
    rd /s /q "%~1"
    EXIT /b 0