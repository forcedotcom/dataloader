# Notes for Data Loader v45.0.0 and later
 
Data Loader v45 and later is built with JDK 11 and requires a 64-bit operating system. Install JRE 11 or later before installing Data Loader. See instructions further down to install via scripts.
 
Data Loader v44 is the last version built with Oracle Java 8. Developers can check out code from the branch “DataloaderV44Release”. End users can download v44 or older from this project’s “releases” tab.

# Java Requirement
 
Developers need to use JDK 11 or later such as [Zulu OpenJDK](https://www.azul.com/downloads/zulu) before building Data Loader.
# Build Data Loader

    git clone git@github.com:forcedotcom/dataloader.git
    cd dataloader
    git submodule init
    git submodule update
    mvn clean package -DskipTests
    
The build will include the appropriate eclipse swt jar by detecting your operating system type. If you would like to manually specify the eclipse swt jar, take a look at the pom.xml file to see a full list of available profiles.

Note: Salesforce officially supports Data Loader for 64-bit Windows and macOS. Any other platforms that Data Loader can be compiled for are unofficial.

For macOS and Windows, the build will generate needed JARs in the target directory. 

    
# Execute Data Loader

To run the Data Loader GUI, run the command

    java -jar target/dataloader-x.y.z-uber.jar
    
Use the command below to run the Data Loader GUI on Mac

    java -XstartOnFirstThread -jar target/dataloader-x.y.z-uber.jar

To run data loader for debug

    java -XstartOnFirstThread -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005  target/dataloader-x.y.z-uber.jar

To run Data Loader in Batch mode on Windows, see [Batch mode for Windows platform](https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/loader_batchmode_intro.htm]). Batch mode is supported only on Windows platform.

Data Loader can be executed in Batch mode on other unsupported platforms using the following command:

    java -jar target/dataloader-x.y.z-uber.jar run.mode=batch process.name=<process name> salesforce.config.dir=<path to config dir containing process-conf.xml and config.properties files>

Commands to encrypt password:

    java -cp target/dataloader-x.y.z-uber.jar com.salesforce.dataloader.security.EncryptionUtil -k <path to keyfile>
    java -cp target/dataloader-x.y.z-uber.jar com.salesforce.dataloader.security.EncryptionUtil -e <password in plain text> <path to keyfile>

# Execute Data Loader With Scripts for v45 and Later
 
Launch scripts are provided to help end users launch Data Loader for Windows and macOS. Zip files are provided for macOS and Windows environments in the project's "releases" tab. There are specific installation instructions for [macOS](https://help.salesforce.com/articleView?id=sf.loader_install_mac.htm) and [Windows](https://help.​salesforce.com/articleView?id=​loader_install_windows.htm).
 
A new directory called "release" was added to the project’s root directory. Developers can build Data Loader and place the dataloader-x.y.z-uber.jar inside "release/mac" or "release\win" directory to launch a customized build.


# Test Data Loader

See the [testing wiki](https://github.com/forcedotcom/dataloader/wiki/Testing-Dataloader)

# Resources

For more information, see the [wiki](http://wiki.apexdevnet.com/index.php/Tools), or the [Data Loader Developer's Guide](https://na1.salesforce.com/help/doc/en/salesforce_data_loader.pdf). 

Questions can be directed to the [open source forum](https://developer.salesforce.com/forums?feedtype=RECENT&dc=APIs_and_Integration&criteria=ALLQUESTIONS&#!/feedtype=RECENT&criteria=ALLQUESTIONS&).

# Dependencies

Force Web Service Connector version is here. [Force Wsc and Patner API](https://mvnrepository.com/artifact/com.force.api)

