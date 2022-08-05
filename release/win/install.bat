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

IF EXIST "%INSTALLATION_DIR%" (
    call :deleteExistingDir
)
if "%ERRORLEVEL%" == "0" (
    call :copyFilesToInstallationDir
) ELSE (
    echo.
    echo Did not overwrite currently installed data loader v%DATALOADER_VERSION%. You can re-run installation using another directory.
)
goto :exit

:deleteExistingDir
    set prompt="Do you want to overwrite currently installed Data Loader v%DATALOADER_VERSION% and its configuration in '%INSTALLATION_DIR%'?[Yes/No]"
    CALL :promptAndExecuteOperation %prompt% doDeleteExistingDir 1
    EXIT /b %ERRORLEVEL%
    
:doDeleteExistingDir
    echo Deleting existing Data Loader v%DATALOADER_VERSION%...
    rd /s /q "%INSTALLATION_DIR%"
    EXIT /b 0

:copyFilesToInstallationDir
    echo.
    set "SRC_DIR=%~dp0"
    IF %SRC_DIR:~-1%==\ SET "SRC_DIR=%SRC_DIR:~0,-1%"
    echo Copying files from '%SRC_DIR%' to '%INSTALLATION_DIR%'  ...
    xcopy "%SRC_DIR%" "%INSTALLATION_DIR%" /e /i
    del "%INSTALLATION_DIR%\install.bat" /q
    echo Your Data Loader v%DATALOADER_VERSION% is created in '%INSTALLATION_DIR%'
    CALL :promptForShortcut StartMenu
    CALL :promptForShortcut Desktop
    EXIT /b 0

REM Note for label renaming - promptForShortcut sets "yesDestination" var based on naming convention.
:createStartMenuShortcut
    IF NOT EXIST "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce\" (
        mkdir "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce"
    )
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:INSTALLATION_DIR""""; $Shortcut.TargetPath = """"$env:INSTALLATION_DIR\dataloader.bat""""; $Shortcut.IconLocation = """"$env:INSTALLATION_DIR\dataloader.ico""""; $Shortcut.WindowStyle=7; $Shortcut.Save()"
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Salesforce\Dataloader %DATALOADER_VERSION%.lnk" >nul
    EXIT /b 0

REM Note for label renaming - promptForShortcut sets "yesDestination" var based on naming convention.
:createDesktopShortcut
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:INSTALLATION_DIR""""; $Shortcut.TargetPath = """"$env:INSTALLATION_DIR\dataloader.bat""""; $Shortcut.IconLocation = """"$env:INSTALLATION_DIR\dataloader.ico""""; $Shortcut.WindowStyle=7; $Shortcut.Save()"
    for /f "usebackq tokens=3*" %%D IN (`reg query "HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\User Shell Folders" /v Desktop`) do set DESKTOP_DIR=%%D
    move "%USERPROFILE%\Desktop\Dataloader.lnk" "%DESKTOP_DIR%\Dataloader %DATALOADER_VERSION%.lnk" >nul
    EXIT /b 0
    
:promptForShortcut
REM Usage:
REM CALL :promptForShortcut <desktop | startMenu>
    set createLabel=create%~1Shortcut
    set desktopShortcutPrompt="Would you like to create a desktop icon? [Yes/No]"
    set startMenuShortcutPrompt="Would you like to create a start menu shortcut? [Yes/No]"
    set prompt=%startMenuShortcutPrompt%
    IF /I "%~1" == "desktop" (
        set prompt=%desktopShortcutPrompt%
    )
    CALL :promptAndExecuteOperation %prompt% %createLabel% 0
    EXIT /b %ERRORLEVEL%
    
:promptAndExecuteOperation
    echo.
    SET /p REPLY=%~1
    if /I "%REPLY%"=="Y" goto :%~2
    if /I "%REPLY%"=="Yes" goto :%~2
    if /I "%REPLY%"=="N" EXIT /b %~3
    if /I "%REPLY%"=="No" EXIT /b %~3
    echo Type Yes or No.
    goto :promptAndExecuteOperation
    EXIT /b %ERRORLEVEL%

:exit
    echo.
    echo Data Loader installation is quitting.
    endlocal
    PAUSE
    EXIT /b %ERRORLEVEL%