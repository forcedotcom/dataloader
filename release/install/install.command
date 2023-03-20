:; #!/bin/bash
:; ###########
:; # start of installer code for Mac and Linux
:; ###########
:; DL_INSTALL_ROOT=`dirname $0`
:; source ${DL_INSTALL_ROOT}/util/util.sh
:;
:; java -cp "${DL_INSTALL_ROOT}/*" com.salesforce.dataloader.install.Installer
:;
:; echo  Data Loader installation is quitting.
:; echo ""
:; exit 0
:; ###########
:; # end of installer code for Mac and Linux
:; ###########
:;
:;
; #########
; # start of installer code for Windows
; #########
@echo off
setlocal

SET ERRORLEVEL=0
java -cp "%~dp0*" com.salesforce.dataloader.install.Installer %*

:exit
    echo.
    echo Data Loader installation is quitting.
    endlocal
    PAUSE
    EXIT /b %ERRORLEVEL%