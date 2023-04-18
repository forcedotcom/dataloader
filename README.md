# Notes for Data Loader v45.0.0 and later
 
Data Loader v45 and later is built with JDK 11 and requires a 64-bit operating system. Install JRE 11 or later before installing Data Loader. See instructions further down to install via scripts.

Data Loader v44 is the last version built with Oracle Java 8. Developers can check out code from the branch “DataloaderV44Release”. End users can download v44 or older from this project’s “releases” tab.

# Install Data Loader on Mac or Windows
 
Follow the installation instructions for [macOS](https://help.salesforce.com/articleView?id=sf.loader_install_mac.htm) and [Windows](https://help.​salesforce.com/articleView?id=​loader_install_windows.htm).

NOTE: Salesforce officially supports Data Loader for Windows and macOS. Any other operating systems that Data Loader jar file can be run or compiled for are unofficial.

# Install Data Loader on Linux
Salesforce officially supports Data Loader for Windows and macOS. The details of supported OS versions and CPU architecture are provided in the Release Notes.

Extract contents of Data Loader zip file, rename `install.command` as `install.sh`, and run the following command (v48 and later):

    ./install.sh

# Execute Data Loader in GUI mode on Linux
Use the command below to run the Data Loader GUI on Linux.

    ./dataloader.sh
    
    OR
    
    java -jar dataloader-x.y.z.jar

# Execute Data Loader in Batch mode on Mac or Linux
NOTE: Batch mode is officially supported only on Windows. To run Data Loader in Batch mode on Windows, see [Batch mode for Windows](https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/loader_batchmode_intro.htm). 

To execute Data Loader in Batch mode on Mac or Linux, run one of the following commands from Data Loader installation directory:

Mac

    ./dataloader_console <path to config dir containing process-conf.xml and config.properties files> <process name> run.mode=batch
    
Linux

    ./dataloader.sh <path to config dir containing process-conf.xml and config.properties files> <process name> run.mode=batch
    
Alternately,
    
    java -jar dataloader-x.y.z.jar <path to config dir containing process-conf.xml and config.properties files> <process name> run.mode=batch
    
OR
    
    java -jar dataloader-x.y.z.jar salesforce.config.dir=<path to config dir containing process-conf.xml and config.properties files> process.name=<process name> run.mode=batch 
    

## Commands to create an encryption key file, encrypt a password, or decrypt a password
See [Batch mode for Windows](https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/loader_batchmode_intro.htm) for the detailed steps to create an encryption key file, encrypt a password, or decrypt a password on Windows.

Batch mode requires specifying an encrypted password in process-conf.xml, config.properties, or as a command line argument. The first step in encrypting a password is to create an encryption key file on Mac or Linux (Replace `dataloader_console` with `dataloader.sh` on Linux).

Following command generates an encryption key file on Mac and Linux. It uses the default encryption key file `${HOME}/.dataloader/dataloader.key` if an encryption key file is not specified.
    
    ./dataloader_console -k [<name of an encryption key file>]  run.mode=encrypt 
    
    OR
    
    java -jar dataloader-x.y.z.jar -k [<name of an encryption key file>]  run.mode=encrypt 
 
 Encrypt a password.
    
    ./dataloader_console -e <password in plain text> [<name of an encryption key file>] run.mode=encrypt 
    
    OR
    
    java -jar dataloader-x.y.z.jar -e <password in plain text> [<name of an encryption key file>] run.mode=encrypt

Decrypt a password.
    
    ./dataloader_console -d <encrypted password> [<path to keyfile>] run.mode=encrypt 
    
    OR
    
    java -jar dataloader-x.y.z.jar -d <encrypted password> [<path to keyfile if it is other than the default path>] run.mode=encrypt
    
# Build Data Loader
Developers need to use JDK 11 or later to build Data Loader. For example, developers can use [Zulu OpenJDK](https://www.azul.com/downloads/zulu) to build Data Loader.

    git clone git@github.com:forcedotcom/dataloader.git
    cd dataloader
    git submodule init
    git submodule update
    mvn clean package -DskipTests 
        or
    ./dlbuilder.sh

`dataloader_v<x.y.z>.zip` will be created in the root directory of the local git clone.

# Debug Data Loader
To run data loader for debugging with an IDE

    java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005  dataloader-x.y.z.jar

# Test Data Loader

See the [testing wiki](https://github.com/forcedotcom/dataloader/wiki/Testing-Dataloader)

# Resources

For more information, see the [Salesforce Data Loader Guide](https://na1.salesforce.com/help/doc/en/salesforce_data_loader.pdf). 

Questions can be directed to the [open source forum](https://developer.salesforce.com/forums?feedtype=RECENT&dc=APIs_and_Integration&criteria=ALLQUESTIONS&#!/feedtype=RECENT&criteria=ALLQUESTIONS&).

# Dependencies and plugins

Update SWT by running `python3 <root of the git clone>/updateSWT.py <root of the git clone>`. Requires python 3.9 or later.

All other dependencies and plugins are downloaded by maven from the central maven repo. Run `mvn versions:display-dependency-updates` to see which dependencies need an update. It will list all dependencies whose specified version in pom.xml needs an update. Run `mvn versions:use-latest-releases` to update these dependencies. Run `mvn versions:display-plugin-updates` again to check which plugins still need an update and update their versions manually.
