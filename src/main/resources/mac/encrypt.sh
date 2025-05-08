#!/bin/zsh
DL_INSTALL_ROOT="$(dirname "$(readlink -f "$0")")"
source ${DL_INSTALL_ROOT}/util/util.sh

runDataLoader $@ run.mode=encrypt