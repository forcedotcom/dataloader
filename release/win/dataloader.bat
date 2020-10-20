@echo off
setlocal

echo.
echo *************************************************************************
echo **            ___  ____ ___ ____   _    ____ ____ ___  ____ ____       **
echo **            ^|  \ ^|__^|  ^|  ^|__^|   ^|    ^|  ^| ^|__^| ^|  \ ^|___ ^|__/       **
echo **            ^|__/ ^|  ^|  ^|  ^|  ^|   ^|___ ^|__^| ^|  ^| ^|__/ ^|___ ^|  \       **
echo **                                                                     **
echo **  Data Loader v@@SHORT_VERSION@@ is a Salesforce supported Open Source project to   **
echo **  help you import data to and export data from your Salesforce org.  **
echo **  It requires Zulu OpenJDK 11.0.x to run.                            **
echo **                                                                     **
echo **  Github Project Url:                                                **
echo **       https://github.com/forcedotcom/dataloader                     **
echo **  Salesforce Documentation:                                          **
echo **       https://help.salesforce.com/articleView?id=data_loader.htm    **
echo **                                                                     **
echo *************************************************************************
echo.

rem set ZULU_JAVA_HOME=D:\Java\zulu11_64\bin\
if NOT "%ZULU_JAVA_HOME%"=="" (
	echo Using '%ZULU_JAVA_HOME%'
	echo Skipping Search
)

:CheckOpenJdk11
	echo Data Loader requires Zulu OpenJDK 11. Checking if it is installed...
	
	if "%ZULU_JAVA_HOME%"=="" (
		echo Checking in PATH
		for /F "delims=" %%a in ('powershell -Command "foreach($path in (Get-ChildItem Env:Path).value -split ';') { if($path -like '*zulu*') { echo $path }};"') do (
			echo Found Zulu in: '%%a' - Checking version	
			FOR /f "tokens=4 delims=. " %%t IN ('"%%a\java" -fullversion 2^>^&1' ) do (
				if %%t==^"11 (
					echo Version check suceeded for %%a
					Set "ZULU_JAVA_HOME=%%a"
				)
				if NOT %%t==^"11 (
					echo Version check failed
				)
			)
		)
	)
	REM If still nothing found
	if "%ZULU_JAVA_HOME%"=="" (
		echo Zulu OpenJDK 11 not found. Download Zulu OpenJDK 11 for Windows here:
		echo    https://www.azul.com/downloads/zulu/zulu-windows/
		PAUSE
		goto Exit

:Run
    "%ZULU_JAVA_HOME%\java"  -jar dataloader-@@FULL_VERSION@@-uber.jar salesforce.config.dir=configs

:Exit
    endlocal
