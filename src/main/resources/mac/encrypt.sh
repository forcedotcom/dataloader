#!/bin/sh
DL_INSTALL_ROOT="$( cd -- "$( dirname -- "$0" )" >/dev/null 2>&1 && pwd -P )"
. "${DL_INSTALL_ROOT}/util/util.sh"

runDataLoader "$@" run.mode=encrypt