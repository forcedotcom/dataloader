@echo off

..\Java\bin\java.exe -cp ..\${pom.build.finalName}-jar-with-dependencies.jar com.salesforce.dataloader.security.EncryptionUtil %*
