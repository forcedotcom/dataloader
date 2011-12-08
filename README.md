# Build dataloader

    mvn clean install -DskipTests
    
The Linux 64 bit profile is used by default.  In order to use the windows profile, add the parameter <code>-Denv=win32</code>.  Take a look at the pom.xml file to see a full list of available profiles.
    
# Execute dataloader

    java -jar target/dataloader-22.0.0-SNAPSHOT-jar-with-dependencies.jar

# Test dataloader
