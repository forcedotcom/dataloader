@echo off

..\Java\bin\java.exe -cp ..\${pom.build.finalName}-uber.jar com.salesforce.dataloader.security.EncryptionUtil %*
