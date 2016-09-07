# Build Data Loader

    git clone git@github.com:forcedotcom/dataloader.git
    git submodule init
    git submodule update
    mvn clean package -DskipTests
    
The build will include the appropriate eclipse swt jar by detecting the operating system type.  If you would like to manually specify the eclipse swt jar, take a look at the pom.xml file to see a full list of available profiles.

Note: salesforce.com officially supports dataloader on Windows XP and Windows 7.  The other platforms that dataloader can be compiled for are unofficial.

The build will generate a windows installer exe in the target directory when executing the build in a windows environment.  If you are packaging the windows installer for distribution, you must use the following command to specify the win32 profile because the installer is packaged with a 32-bit JRE.

    mvn clean package -P win32,-win64 -DskipTests
    
# Execute Data Loader

To run the Data Loader GUI, run the command

    java -jar target/dataloader-38.0-uber.jar
    
Use the command below to run the Data Loader GUI on Mac

    java -XstartOnFirstThread -jar target/dataloader-38.0-uber.jar

To run data loader for debug

    java -XstartOnFirstThread -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005  target/dataloader-37.0.1-uber.jar

To run Data Loader from the command line, use the command:

    java -cp target/dataloader-38.0-uber.jar -Dsalesforce.config.dir=CONFIG_DIR com.salesforce.dataloader.process.ProcessRunner

The command-line version runs with whatever properties you have in your config.properties file, but you can also pass paramters at runtime as arguments to the program.

For example, the following command sets the operation to insert regardless of what settings are contained in the config.properties file:

    java -cp target/dataloader-38.0-uber.jar -Dsalesforce.config.dir=CONFIG_DIR com.salesforce.dataloader.process.ProcessRunner process.operation=insert

The process-conf.xml file can be used to define properties for multiple processes.  Look at src/main/nsis/samples/conf/process-conf.xml for examples on how to configure it.  The way to run a process defined in process-conf.xml is to specify process name on command line like this:

    java -cp target/dataloader-38.0-uber.jar -Dsalesforce.config.dir=CONFIG_DIR com.salesforce.dataloader.process.ProcessRunner process.name=opportunityUpsertProcess


# Test Data Loader

See the [testing wiki](https://github.com/forcedotcom/dataloader/wiki/Testing-Dataloader)

# Resources

For more information, see the [wiki](http://wiki.apexdevnet.com/index.php/Tools), or the [Data Loader Developer's Guide](https://na1.salesforce.com/help/doc/en/salesforce_data_loader.pdf). 

Questions can be directed to the [open source forum](https://developer.salesforce.com/forums?feedtype=RECENT&dc=APIs_and_Integration&criteria=ALLQUESTIONS&#!/feedtype=RECENT&criteria=ALLQUESTIONS&).

# Dependencies

Force Web Service Connector version is here. [Force Wsc and Patner API](https://mvnrepository.com/artifact/com.force.api)

