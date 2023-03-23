:; #!/bin/bash #
:; #
:; source "`dirname $0`/util/util.sh" #
:; checkJavaVersion #
:; java -cp "`dirname $0`/*" com.salesforce.dataloader.install.Installer #
:; exit 0 #

@echo off
setlocal
CALL "%~dp0util\util.bat" :checkJavaVersion
java -cp "%~dp0*" com.salesforce.dataloader.install.Installer %*
endlocal
PAUSE
