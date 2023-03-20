:; #!/bin/bash #
:; #
:; java -cp "`dirname $0`/*" com.salesforce.dataloader.install.Installer #
:; exit 0 #

@echo off
setlocal
java -cp "%~dp0*" com.salesforce.dataloader.install.Installer %*
endlocal
PAUSE
