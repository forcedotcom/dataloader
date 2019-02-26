@echo off
setlocal

set DATALOADER_VERSION=45.0.0
set DATALOADER_SHORT_VERSION=45
set DATALOADER_UBER_JAR_NAME=dataloader-45.0.0-uber.jar

echo.
echo ***************************************************************************
echo **            ___  ____ ___ ____   _    ____ ____ ___  ____ ____         **
echo **            ^|  \ ^|__^|  ^|  ^|__^|   ^|    ^|  ^| ^|__^| ^|  \ ^|___ ^|__/         **
echo **            ^|__/ ^|  ^|  ^|  ^|  ^|   ^|___ ^|__^| ^|  ^| ^|__/ ^|___ ^|  \         **
echo **                                                                       **
echo **  Dataloder v%DATALOADER_SHORT_VERSION% is a Salesforce supported Open Source project to help  **
echo **  you import data to and export data from your Salesforce org.         **
echo **  It requires Zulu OpenJDK 11 to run.                                  **
echo **                                                                       **
echo **  Github Project Url:                                                  **
echo **       https://github.com/forcedotcom/dataloader                       **
echo **  Salesforce Documentation:                                            **
echo **       https://help.salesforce.com/articleView?id=data_loader.htm      **
echo **                                                                       **
echo ***************************************************************************
echo.

echo Data Loader installation creates a folder in your '%USERPROFILE%' directory.
set /p DIR_NAME=Which folder should it use? [default: dataloader] || set DIR_NAME=dataloader

set INSTALLATION_DIR=%USERPROFILE%\%DIR_NAME%\v%DATALOADER_VERSION%

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
    rd /s /q %INSTALLATION_DIR%
    goto CopyFiles
:DeleteDirNo
    goto Exit

:CopyFiles
    echo.
    echo Copying files to '%INSTALLATION_DIR%'  ...
    xcopy . "%INSTALLATION_DIR%" /e /i
    del "%INSTALLATION_DIR%\install.bat" /q
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