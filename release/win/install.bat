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

echo Data Loader installation requires you to provide an installation directory to create a version-specific subdirectory for the installation artifacts.
echo It uses '%USERPROFILE%\^<relative path^>' as the installation directory if you provide a relative path for the installation directory.
echo.
set /p DIR_NAME=Provide the installation directory [default: dataloader] : || set DIR_NAME=dataloader

if "%DIR_NAME:~1,1%" == ":" (
    REM absolute path specified
    set INSTALLATION_DIR=%DIR_NAME%\v%DATALOADER_VERSION%
) else (
    if "%DIR_NAME:~0,1%" == "\" (
        REM absolute path specified
        set INSTALLATION_DIR=%DIR_NAME%\v%DATALOADER_VERSION%
    ) else (
        REM relative path specified
        set INSTALLATION_DIR=%USERPROFILE%\%DIR_NAME%\v%DATALOADER_VERSION%
    )
)

IF EXIST %INSTALLATION_DIR% (
    goto ExistingDir
) ELSE (
    goto CopyFiles
)

:ExistingDir
    echo.
    echo Do you want to overwrite previously installed versions of Data Loader v%DATALOADER_VERSION% and configurations in '%INSTALLATION_DIR%'?
    set /p DELETE_EXISTING_DIR=If not, installation will quit and you can restart installation using another directory.[Yes/No]
    if /I "%DELETE_EXISTING_DIR%"=="Y" goto DeleteDirYes
    if /I "%DELETE_EXISTING_DIR%"=="Yes" goto DeleteDirYes
    if /I "%DELETE_EXISTING_DIR%"=="N" goto DeleteDirNo
    if /I "%DELETE_EXISTING_DIR%"=="No" goto DeleteDirNo
    echo Type Yes or No.
    goto ExistingDir
:DeleteDirYes
    echo Deleting existing Data Loader v%DATALOADER_VERSION%...
    rd /s /q "%INSTALLATION_DIR%"
    goto CopyFiles
:DeleteDirNo
    goto Exit

:CopyFiles
    echo.
    set SRC_DIR=%~dp0
    IF %SRC_DIR:~-1%==\ SET SRC_DIR=%SRC_DIR:~0,-1%
    echo Copying files from '%SRC_DIR%' to '%INSTALLATION_DIR%'  ...
    xcopy "%SRC_DIR%" "%INSTALLATION_DIR%" /e /i
    del "%INSTALLATION_DIR%\install.bat" /q
    del "%INSTALLATION_DIR%\dataloader.ico" /q
    rmdir "%INSTALLATION_DIR%\META-INF" /s /q
    echo Your Data Loader v%DATALOADER_VERSION% is created in '%INSTALLATION_DIR%'

:CreateStartMenuShortCut
    echo.
    set /p REPLY=Would you like to create a start menu shortcut? [Yes/No]
    if /I "%REPLY%"=="Y" goto StartMenuShortCutYes
    if /I "%REPLY%"=="Yes" goto StartMenuShortCutYes
    if /I "%REPLY%"=="N" goto StartMenuShortCutNo
    if /I "%REPLY%"=="No" goto StartMenuShortCutNo
    echo Type Yes or No.
    goto CreateStartMenuShortCut
:StartMenuShortCutNo
    goto CreateDesktopShortcut
:StartMenuShortCutYes
    IF NOT EXIST "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce\" (
        mkdir "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce"
    )
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:INSTALLATION_DIR""""; $Shortcut.TargetPath = """"$env:INSTALLATION_DIR\dataloader.bat""""; $Shortcut.IconLocation = """"$env:INSTALLATION_DIR\dataloader.ico""""; $Shortcut.Save()"
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce\Dataloader %DATALOADER_VERSION%.lnk" >nul

:CreateDesktopShortcut
    echo.
    set /p REPLY=Would you like to create a desktop icon? [Yes/No]
    if /I "%REPLY%"=="Y" goto StartMenuShortCutYes
    if /I "%REPLY%"=="Yes" goto StartMenuShortCutYes
    if /I "%REPLY%"=="N" goto StartMenuShortCutNo
    if /I "%REPLY%"=="No" goto StartMenuShortCutNo
    echo Type Yes or No.
    goto CreateDesktopShortcut
:StartMenuShortCutNo
    goto Exit
:StartMenuShortCutYes
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:INSTALLATION_DIR""""; $Shortcut.TargetPath = """"$env:INSTALLATION_DIR\dataloader.bat""""; $Shortcut.IconLocation = """"$env:INSTALLATION_DIR\dataloader.ico""""; $Shortcut.Save()"
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%USERPROFILE%\Desktop\Dataloader %DATALOADER_VERSION%.lnk" >nul

:Exit
    echo.
    echo Data Loader installation is quitting.
    endlocal
    PAUSE
