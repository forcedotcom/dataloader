@echo off
setlocal

CALL "%~dp0util\util.bat" showBanner

echo Data Loader installation requires you to provide an installation directory to create a version-specific subdirectory for the installation artifacts.
echo It uses '%USERPROFILE%\^<relative path^>' as the installation directory if you provide a relative path for the installation directory.
echo.
set /p "DIR_NAME=Provide the installation directory [default: dataloader]" : || set DIR_NAME=dataloader

if "%DIR_NAME:~1,1%" == ":" (
    REM absolute path specified
    set "INSTALLATION_DIR=%DIR_NAME%\v%DATALOADER_VERSION%"
) else (
    if "%DIR_NAME:~0,1%" == "\" (
        REM absolute path specified
        set "INSTALLATION_DIR=%DIR_NAME%\v%DATALOADER_VERSION%"
    ) else (
        REM relative path specified
        set "INSTALLATION_DIR=%USERPROFILE%\%DIR_NAME%\v%DATALOADER_VERSION%"
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
    goto :exit

:CopyFiles
    echo.
    set "SRC_DIR=%~dp0"
    IF %SRC_DIR:~-1%==\ SET "SRC_DIR=%SRC_DIR:~0,-1%"
    echo Copying files from '%SRC_DIR%' to '%INSTALLATION_DIR%'  ...
    xcopy "%SRC_DIR%" "%INSTALLATION_DIR%" /e /i
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
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:INSTALLATION_DIR""""; $Shortcut.TargetPath = """"$env:INSTALLATION_DIR\dataloader.bat""""; $Shortcut.IconLocation = """"$env:INSTALLATION_DIR\dataloader.ico""""; $Shortcut.WindowStyle=7; $Shortcut.Save()"
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce\Dataloader %DATALOADER_VERSION%.lnk" >nul

:CreateDesktopShortcut
    echo.
    set /p REPLY=Would you like to create a desktop icon? [Yes/No]
    if /I "%REPLY%"=="Y" goto DesktopShortCutYes
    if /I "%REPLY%"=="Yes" goto DesktopShortCutYes
    if /I "%REPLY%"=="N" goto DesktopShortCutNo
    if /I "%REPLY%"=="No" goto DesktopShortCutNo
    echo Type Yes or No.
    goto CreateDesktopShortcut
:DesktopShortCutNo
    goto :exit
:DesktopShortCutYes
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:INSTALLATION_DIR""""; $Shortcut.TargetPath = """"$env:INSTALLATION_DIR\dataloader.bat""""; $Shortcut.IconLocation = """"$env:INSTALLATION_DIR\dataloader.ico""""; $Shortcut.WindowStyle=7; $Shortcut.Save()"
    for /f "usebackq tokens=3*" %%D IN (`reg query "HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\User Shell Folders" /v Desktop`) do set DESKTOP_DIR=%%D
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%DESKTOP_DIR%\Dataloader %DATALOADER_VERSION%.lnk" >nul
    goto :exit
    
:exit
    echo.
    echo Data Loader installation is quitting.
    endlocal
    PAUSE
