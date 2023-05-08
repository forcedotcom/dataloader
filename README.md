# Prerequisites

Latest release of Data Loader requires JRE 11 or later.

# Installing Data Loader
Salesforce officially supports Data Loader for Windows and macOS. All other operating systems are unsupported. The list of supported macOS and Windows versions and CPU architecture for a released version of Data Loader is provided in the [Release Notes](https://github.com/forcedotcom/dataloader/releases) for that release.

Follow the installation instructions for [macOS](https://help.salesforce.com/articleView?id=sf.loader_install_mac.htm) and [Windows](https://help.​salesforce.com/articleView?id=​loader_install_windows.htm).

Installing on Linux: Extract contents of Data Loader zip file, rename `install.command` as `install.sh`, and run the following command:

    ./install.sh

# Running Data Loader in GUI mode

For running Data Loader on macOS or Windows, follow the [instructions](https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/configuring_the_data_loader.htm).

For running Data Loader on Linux, type the following command in a command shell:

    ./dataloader.sh
    
    OR
    
    java -jar dataloader-x.y.z.jar

Consult the [documentation](https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/configuring_the_data_loader.htm) for the details of how to configure and use Data Loader.

# Running Data Loader in Batch mode
Batch mode is officially supported only on Windows. To run Data Loader in Batch mode on Windows, see [Batch mode for Windows](https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/loader_batchmode_intro.htm). 

Execute the following command on Mac (Replace `dataloader_console` with `dataloader.sh` on Linux):

    ./dataloader_console <config dir containing process-conf.xml and config.properties files> <process name> run.mode=batch
    
Alternately execute one of the following commands:
    
    java -jar dataloader-x.y.z.jar <config dir containing process-conf.xml and config.properties files> <process name> run.mode=batch
    
    OR
    
    java -jar dataloader-x.y.z.jar salesforce.config.dir=<config dir containing process-conf.xml and config.properties files> process.name=<process name> run.mode=batch 
    

## Commands to create an encryption key file, encrypt a password, or decrypt a password
See [Batch mode for Windows](https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/loader_batchmode_intro.htm) for the detailed steps to create an encryption key file, encrypt a password, or decrypt a password on Windows.

Batch mode requires specifying an encrypted password in process-conf.xml, config.properties, or as a command line argument. The first step in encrypting a password is to create an encryption key file on Mac or Linux.

Execute the following command to generate an encryption key file on Mac (Replace `dataloader_console` with `dataloader.sh` on Linux):
    
    ./dataloader_console -k [<encryption key file>]  run.mode=encrypt 
    
    OR
    
    java -jar dataloader-x.y.z.jar -k [<encryption key file>]  run.mode=encrypt 
 
 Execute the following command to encrypt a password on Mac (Replace `dataloader_console` with `dataloader.sh` on Linux):
    
    ./dataloader_console -e <password in plain text> [<encryption key file>] run.mode=encrypt 
    
    OR
    
    java -jar dataloader-x.y.z.jar -e <password in plain text> [<encryption key file>] run.mode=encrypt

Execute the following command to decrypt a password on Mac (Replace `dataloader_console` with `dataloader.sh` on Linux):
    
    ./dataloader_console -d <encrypted password> [<encryption key file>] run.mode=encrypt 
    
    OR
    
    java -jar dataloader-x.y.z.jar -d <encrypted password> [<encryption key file>] run.mode=encrypt

NOTE: these commands use the default encryption key file `${HOME}/.dataloader/dataloader.key` if an encryption key file is not specified.

# Reporting an issue
Collect the following information before reaching out to Salesforce Support or reporting an issue on github:
- Data Loader version, operation being performed, and screenshots of the issue.
- Config files: `config.properties`, `log4j2.properties` or `log-conf.xml`, `process-conf.xml`.
- log file:
  - Set the root log level to “debug” in Advanced Config dialog (v58 and later) or in `log-conf.xml`.
  - Rerun data loader to reproduce the issue.
  - Send the log output located in the file shown by “Logging output file” info in the Advanced Settings dialog of their data loader. Logging output file info is shown in the Advanced Settings dialog as of v58.
  - If you are using v58 or earlier, the default location of the debug log is `<tempdir>/sdl.log`
  - The default tempdir is `%USER%\AppData\Local\Temp` on Windows
  - The default tempdir is `${TMPDIR}` on macOS
- Provide a sample csv file containing as few as possible columns and rows to reproduce the issue.
- Provide the following information about your org (it is available in the log file if data loader version > 58.0.0, the log level is set to debug and the user logs in):
  - Org id: Settings >> Company Profile >> Company Information >> value of Salesforce organization id field
  - instance : Settings >> Company Profile >> Company Information >> value of Instance field
  - User id: follow the instructions in this article or this article.

NOTE:
Remove all personal, business-specific, an any other sensitive information before reporting an issue on a public forum such as github. Sensitive information may be in any files you may upload including config files, data files such as csv files, log files, screenshots, console logs, and more.

# Building Data Loader
Developers need to use JDK 11 or later to build Data Loader. For example, developers can use [Zulu OpenJDK](https://www.azul.com/downloads/zulu) to build Data Loader.

    git clone git@github.com:forcedotcom/dataloader.git
    cd dataloader
    git submodule init
    git submodule update
    mvn clean package -DskipTests 
        or
    ./dlbuilder.sh

`dataloader_v<x.y.z>.zip` will be created in the root directory of the local git clone.

# Debugging Data Loader
To run data loader for debugging with an IDE

    java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005  dataloader-x.y.z.jar

# Testing Data Loader

See the [testing wiki](https://github.com/forcedotcom/dataloader/wiki/Testing-Dataloader)

# Resources

For more information, see the [Salesforce Data Loader Guide](https://na1.salesforce.com/help/doc/en/salesforce_data_loader.pdf). 

Questions can be directed to the [open source forum](https://developer.salesforce.com/forums?feedtype=RECENT&dc=APIs_and_Integration&criteria=ALLQUESTIONS&#!/feedtype=RECENT&criteria=ALLQUESTIONS&).

# Dependencies and plugins

Update SWT by running `python3 <root of the git clone>/updateSWT.py <root of the git clone>`. Requires python 3.9 or later.

All other dependencies and plugins are downloaded by maven from the central maven repo. Run `mvn versions:display-dependency-updates` to see which dependencies need an update. It will list all dependencies whose specified version in pom.xml needs an update. Run `mvn versions:use-latest-releases` to update these dependencies. Run `mvn versions:display-plugin-updates` again to check which plugins still need an update and update their versions manually.
