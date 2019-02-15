@echo off

REM echo To run dataloader.bat, set the JAVA_HOME environment variable to the directory where the Java Runtime Environment ^(JRE^) is installed.
REM echo Dataloader V45+ requires Openjdk 11 to launch.  You can download Zulu Java 11 from "https://www.azul.com/downloads/zulu/"
REM echo For example, set JAVA_HOME=C:\Program Files\Zulu\zulu-11
REM echo using Java at: "%JAVA_HOME%\bin"

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
set /p _dataloaderInstallationDirectory=Which folder in your home directory would you like to create in? [default: Dataloader] || set _dataloaderInstallationDirectory=Dataloader

set installationDir=%userprofile%\%_dataloaderInstallationDirectory%\v%version%

IF EXIST %installationDir% (
    goto DirExist
) ELSE (
    goto CopyFiles
)
        
:DirExist
    echo.
    echo We found an existing Dataloader V%version% in '%installationDir%'
    set /p _deleteExistingDirOrNot= Would you like to delete the existing and create it again? Selecting no will quit this installation [y/n, default: n]
    if "%_deleteExistingDirOrNot%"=="y" (
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

:CreateShortcut
    echo.
    echo Creating shortcut in '%userprofile%\Desktop'...
    
    IF EXIST "%userprofile%\Desktop\Dataloader.bat" (
        del /s "%userprofile%\Desktop\Dataloader.bat"
    )
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut(""""$Home\Desktop\Dataloader.lnk""""); $Shortcut.WorkingDirectory = """"$env:installationDir""""; $Shortcut.TargetPath = """"$env:installationDir\dataloader.bat""""; $Shortcut.IconLocation = """"$env:installationDir\dataloader.ico""""; $Shortcut.Save()"
    echo Shortcut is created in your desktop - %userprofile%\Desktop\Dataloader.bat

:Exit
    PAUSE