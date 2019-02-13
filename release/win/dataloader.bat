@echo off 
echo Dataloader V45  requires Openjdk 11 to launch.  You can download Zulu Java 11 
echo from "https://www.azul.com/downloads/zulu".
echo To run dataloader.bat, set the JAVA_HOME environment variable to the directory 
echo where the Java Runtime Environment (JRE) is installed.
echo For example, set JAVA_HOME=C:\Program Files\Zulu\zulu-11
	
echo Using Java at: "%JAVA_HOME%\bin"
 
IF "%JAVA_HOME%" == "" (
    echo To run dataloader.bat, set the JAVA_HOME environment variable to the directory where the Java Runtime Environment is installed.
	echo For example, set JAVA_HOME=C:\Program Files\Zulu\zulu-11
) ELSE (
    echo Using Java at: %JAVA_HOME%\bin    
	cd %~dp0      ;Change to batch file directory
    "%JAVA_HOME%\bin\java"  -jar INSTALLATION_DIRECTORY\dataloader-45.0.0-uber.jar salesforce.config.dir=INSTALLATION_DIRECTORY\configs	
)

