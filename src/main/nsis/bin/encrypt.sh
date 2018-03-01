#!/bin/bash

function error {
    echo "$1" >&2
}

# set execution path
EXE_PATH=$( dirname "${BASH_SOURCE[0]}" )

# test for java installed
JAVA=$(which java)

if [ "$JAVA" == "" ]; then
    error "To run $0, the Java Runtime Environment (JRE) must be installed."
    exit 1;
else
    "${JAVA}" -cp "${EXE_PATH}/../${pom.build.finalName}-uber.jar" com.salesforce.dataloader.security.EncryptionUtil "$@"
fi

exit 0
