@echo off
setlocal

CALL %~dp0\..\initialize.bat -skipbanner
java -cp "%~dp0\..\dataloader-%DATALOADER_VERSION%-uber.jar" com.salesforce.dataloader.security.EncryptionUtil %*

endlocal