@echo off
setlocal

set version=45

echo.
echo ******************************************************************
echo **           Dataloader V45 Installation                        **
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

echo Dataloader V%version% will be created in your home directory: %userprofile%
set /p dirName=Which folder in your home directory would you like to create in? [default: Dataloader] || set dirName=Dataloader

set installationDir=%userprofile%\%dirName%\v%version%

IF EXIST %installationDir% (
    goto ExistingDir
) ELSE (
    goto CopyFiles
)

:ExistingDir
    echo.
    echo We found an existing Dataloader V%version% in '%installationDir%'
    set /p deleteExistingDir=Would you like to delete the existing and create it again? Selecting no will quit this installation [y/n, default: n]
    if "%deleteExistingDir%"=="y" (
        echo Deleting existing Dataloader V%version%...
        rd /s /q %installationDir%
        goto CopyFiles
    ) else (
        goto Exit
    )

:CopyFiles
    echo.
    echo Copying files to %installationDir%...
    xcopy . "%installationDir%" /e /i
    del "%installationDir%\install.bat" /q
    echo Your Dataloader V%version% is created in '%installationDir%'

:CreateStartMenuShortcut
    echo.
    set /p createStartMenuShortcut=Would you like to create a start menu shortcut? [y/n, default: y]
    if "%createStartMenuShortcut%"=="n" (
        goto CreateDesktopShortcut
    )
    echo Creating start menu shortcut...
    IF NOT EXIST "%appdata%\Microsoft\Windows\Start Menu\Programs\Salesforce\" (
        mkdir "%appdata%\Microsoft\Windows\Start Menu\Programs\Salesforce"
    )
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:installationDir""""; $Shortcut.TargetPath = """"$env:installationDir\dataloader.bat""""; $Shortcut.IconLocation = """"$env:installationDir\dataloader.ico""""; $Shortcut.Save()"
    move "%userprofile%\Desktop\Dataloader.lnk" "%appdata%\Microsoft\Windows\Start Menu\Programs\Dataloader\DataloaderV%version%.lnk" >nul
    echo Start menu shortcut is created

 :CreateDesktopShortcut
    echo.
    set /p createDesktopShortcut=Would you like to create a desktop shortcut? [y/n, default: y]
    if "%createDesktopShortcut%"=="n" (
        goto Exit
    )
    echo Creating desktop shortcut...
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:installationDir""""; $Shortcut.TargetPath = """"$env:installationDir\dataloader.bat""""; $Shortcut.IconLocation = """"$env:installationDir\dataloader.ico""""; $Shortcut.Save()"
    move "%userprofile%\Desktop\Dataloader.lnk" "%userprofile%\Desktop\DataloaderV%version%.lnk" >nul
    echo Desktop shortcut is created

:Exit
    echo.
    echo End of installation
    endlocal
    PAUSE