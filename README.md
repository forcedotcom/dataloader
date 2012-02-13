# Build Data Loader

    mvn clean install -DskipTests
    
The Linux 64 bit profile is used by default.  In order to use the windows profile, add the parameter <code>-Denv=win32</code>.  Take a look at the pom.xml file to see a full list of available profiles.

Note: salesforce.com officially supports dataloader on Windows XP and Windows 7.  The other platforms that dataloader can be compiled for are unofficial.

The build will generate a windows installer exe in the target directory when executing the build in a windows environment.  You must use the <code>-Denv=win32</code> parameter if you are building the windows installer.
    
# Execute Data Loader

To run the Data Loader GUI, run the command

    java -jar target/dataloader-24.0.0-jar-with-dependencies.jar
    
Use the command below to run the Data Loader GUI on Mac

    java -jar target/dataloader-24.0.0-jar-with-dependencies.jar -XstartOnFirstThread
    
To run Data Loader from the command line, use the command:

    java -cp target/dataloader-24.0.0-jar-with-dependencies.jar -Dsalesforce.config.dir=CONFIG_DIR com.salesforce.dataloader.process.ProcessRunner

The command-line version runs with whatever properties you have in your config.properties file, but you can also pass paramters at runtime as arguments to the program.

For example, the following command sets the operation to insert regardless of what settings are contained in the config.properties file:

    java -cp target/dataloader-24.0.0-jar-with-dependencies.jar -Dsalesforce.config.dir=CONFIG_DIR com.salesforce.dataloader.process.ProcessRunner process.operation=insert

The process-conf.xml file can be used to define properties for multiple processes.  Look at src/main/nsis/samples/conf/process-conf.xml for examples on how to configure it.  The way to run a process defined in process-conf.xml is to specify process name on command line like this:

    java -cp target/dataloader-24.0.0-jar-with-dependencies.jar -Dsalesforce.config.dir=CONFIG_DIR com.salesforce.dataloader.process.ProcessRunner process.name=opportunityUpsertProcess


# Test Data Loader

    mvn verify -fn
