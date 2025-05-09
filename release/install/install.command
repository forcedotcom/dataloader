:; #!/bin/zsh #
:; #
:; DL_INSTALL_ROOT="$(cd "$(dirname "$0")" && pwd -P)" #
:; source "${DL_INSTALL_ROOT}/util/util.sh" #
:; runDataLoader run.mode=install $@  #
:; exit $? #

@echo off
CALL "%~dp0util\util.bat" :runDataLoader run.mode=install %*