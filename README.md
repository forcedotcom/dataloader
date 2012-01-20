# Build dataloader

    mvn clean install -DskipTests
    
The Linux 64 bit profile is used by default.  In order to use the windows profile, add the parameter <code>-Denv=win32</code>.  Take a look at the pom.xml file to see a full list of available profiles.

The build will generate a windows installer exe when executing the build in a windows environment.
    
# Execute dataloader

    java -jar target/dataloader-23.0.0-SNAPSHOT-jar-with-dependencies.jar
    
Use the command below to execute on Mac

    java -jar target/dataloader-23.0.0-SNAPSHOT-jar-with-dependencies.jar -XstartOnFirstThread

# Test dataloader

    mvn verify -fn
