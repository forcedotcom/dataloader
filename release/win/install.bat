@echo off

REM echo To run dataloader.bat, set the JAVA_HOME environment variable to the directory where the Java Runtime Environment ^(JRE^) is installed.
REM echo Dataloader V45+ requires Openjdk 11 to launch.  You can download Zulu Java 11 from "https://www.azul.com/downloads/zulu/"
REM echo For example, set JAVA_HOME=C:\Program Files\Zulu\zulu-11
REM echo using Java at: "%JAVA_HOME%\bin"


echo We need to create a directory in your home directory %userprofile% to install your Datalaoder Program.
echo Run this with administractor privilege 
set /p _dataloaderInstallationDirectory=Input the directory name,  press enter directly to use the default "dataloader" || set _dataloaderInstallationDirectory=dataloader


set _fullDir=%userprofile%\%_dataloaderInstallationDirectory%
echo %_fullDir%
REM %%si converts %%i to an 8.3 full path
FOR %%i IN (%_fullDir%) DO IF EXIST %%~si\NUL (	
		echo The directory:%_fullDir% already exists
		goto DirExist
		)
goto CopyFiles
		
:DirExist
	set /p _deleteDirOrNot=Do you want to delete? If not, we will exit installation. y/n?
	echo %_deleteDirOrNot%
	if "%_deleteDirOrNot%" =="y" (
	
			echo Deleting existing dataloader installation directory!
			FOR %%i IN (%_fullDir%) DO rd /s /q %%~si
	) else (
	echo 
	goto Exit
	)

:CopyFiles
	REM Supress prompt with target as directory
    echo d | xcopy /s %~dp0. %_fullDir%

:ModifyDataloaderBat	
	REM Modifying dataloader.bat
	
	REM IF "%JAVA_HOME%" == "" (
    powershell -Command "(gc %_fullDir%\dataloader.bat) -replace 'INSTALLATION_DIRECTORY', '%_fullDir%' | Out-File -encoding "UTF8" %_fullDir%\dataloader.bat"
	REM cd /D C:\Users\xuehai\workspace\dataloader\release\win
    REM "%JAVA_HOME%\bin\java"  -jar C:\Users\xuehai\workspace\dataloader\release\win\jars\dataloader-45.0.0-uber.jar salesforce.config.dir=C:\Users\xuehai\workspace\dataloader\release\win\configs	
	del /s "%userprofile%\Desktop\dataloader.bat"
	mklink "%userprofile%\Desktop\dataloader.bat" "%_fullDir%\dataloader.bat"

:Exit
	set _dataloaderInstallationDirectory=
	set _fullDir=
	set _deleteDirOrNot=
	Echo Done!

