## APEX DATALOADER README 

---------------------------------------------
I.  Building the apex dataloader
---------------------------------------------

There are two "out of the box" ways to build the apex dataloader.

The first is to use Eclipse to build the apex dataloader.  Go to File->Import->Import Existing Project.  Choose the base directory and it should find the dataloader project. Before you do this you need to follow the instruction below for the ant build and run the "ant pre" target.

The second way to build the data loader is through apache ant.  This requires that you have ant and perl installed.  Go to the build directory and edit the build.properties file.
Set values for ANT_HOME, JAVA_HOME, and app.home.  Also if you files are not on c: then you'll need to set home.dir as well.  Open a command prompt and from the build directory
you can issue ant commands to build the dataloader.  The current commands are

ant partnerwsdl  - 

ant jar_partnerwsdl - Generates the java src from the partner.wsdl, compilers, and jars the partnerwsdl code.  Use this task if you need to use a new wsdl.

ant compile - Compiles the dataloader src.

ant compile_test - Compiles the dataloader JUnit test src.

ant compile_all - Compiles all the dataloader src, including the WSDL jar.

ant jar_dataloader - Creates one jar with all the necessary classes.  Most of the time you will only want to run this command.

ant run_dataloader - Runs the Gui Version of the dataloader

ant run_process - Runs the command line version of the dataloader, using config.properties file

ant test - Runs a JUnit test, -Dtestcase can be used to specify test file or class to execute.

ant autobuild - Compiles and runs JUnit test suite and sends email specified by test.emailToList property via mailhost in testEmail target, look at autobuildWeekly example of that.


II. Running the apex dataloader
---------------------------------------------

To run the dataloader, you must have Java 1.5.0_06 or a later version of Java 1.5.0.

Put the dataloader jar and the swt libs - swt-awt-win32-3063.dll, swt-awt-win32-3062.dll, swt-win32-3062.dll in the base directory.

To run the gui, use the command 

java -Xms64m -Xmx512m -DentityExpansionLimit=128000 -Dsalesforce.config.dir=CONFIG_DIR -classpath "%cd%\DataLoader.jar" com.salesforce.dataloader.process.DataLoaderRunner

If you do not specify the salesforce.config.dir, then the dataloader will look in the base dir.  The config files are a log-conf.xml, process-conf.xml, database-conf.xml and a config.properties


To run the command line, use:

java -Xms256m -Xmx384m -Dsalesforce.config.dir=CONFIG_DIR -classpath DataLoader.jar com.salesforce.dataloader.process.ProcessRunner

The command line will run with whatever properties you have in your config.properties, but you can also pass paramters at runtime as arguments to the program.

For instance:

java -Xms256m -Xmx384m -Dsalesforce.config.dir=CONFIG_DIR -classpath DataLoader.jar com.salesforce.dataloader.process.ProcessRunner process.operation=insert

will set the operation to insert regardless of what the config.properties has.

process-conf.xml file can be used to define properties for multiple processes.  Look at a sample\conf\process-conf.xml for examples on how to configure it.  The way to run a process defined in process-conf.xml is to specify process name on command line like this:

java -Xms256m -Xmx384m -Dsalesforce.config.dir=CONFIG_DIR -classpath DataLoader.jar com.salesforce.dataloader.process.ProcessRunner process.name=opportunityUpsertProcess

For more information see the [wiki](http://wiki.apexdevnet.com/index.php/Tools), or the [Apex Dataloader Manual](https://na1.salesforce.com/help/doc/en/salesforce_data_loader.pdf) 

Questions can be directed to the [open source forum](http://boards.developerforce.com/t5/Open-Source/bd-p/sforceExplorer)


