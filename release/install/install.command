:; #!/bin/bash #
:; #
:; DL_INSTALL_ROOT="$(dirname "$(readlink -f "$0")")" #
:; source "${DL_INSTALL_ROOT}/../../src/main/resources/mac/util/util.sh" #
:; runDataLoader $@ run.mode=install #
:; exit $? #

@echo off
CALL "%~dp0util\util.bat" :runDataLoader %* run.mode=install
