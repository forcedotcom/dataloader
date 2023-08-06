#!/bin/bash

include () {
    [[ -f "$1" ]] && source "$1"
}

initVars() {
    DATALOADER_VERSION="@@FULL_VERSION@@"
    DATALOADER_SHORT_VERSION=$(echo ${DATALOADER_VERSION} | cut -d'.' -f 1)
    MIN_JAVA_VERSION=@@MIN_JAVA_VERSION@@
    include ${HOME}/.profile
    include ${HOME}/.bash_profile
    include ${HOME}/.bashrc
    include ${HOME}/.zsh_profile
    include ${HOME}/.zshrc
}

checkJavaVersion() {
    initVars
    
    echo "Data Loader requires Java JRE ${MIN_JAVA_VERSION} or later. Checking if it is installed..."
    if [ ! -z "${DATALOADER_JAVA_HOME}" ]
    then
        JAVA_HOME=${DATALOADER_JAVA_HOME}
    fi
    
    PATH=${JAVA_HOME}/bin:${PATH}
    JAVA_VERSION=$(java -version 2>&1 | grep -i version | cut -d'"' -f 2 | cut -d'.' -f 1)
    if [ -z "${JAVA_VERSION}" ]
    then
        echo "Did not find java command."
        echo ""
        exitWithJavaDownloadMessage
    fi
    if [ ${JAVA_VERSION} \< ${MIN_JAVA_VERSION} ]
    then
        echo "Found Java JRE version ${JAVA_VERSION} whereas Data Loader requires Java JRE ${MIN_JAVA_VERSION} or later."
        exitWithJavaDownloadMessage
    fi
}

exitWithJavaDownloadMessage() {
    echo "Java JRE ${MIN_JAVA_VERSION} or later is not installed or DATALOADER_JAVA_HOME environment variable is not set."
    echo "For example, download and install Zulu JRE ${MIN_JAVA_VERSION} or later from here:"
    echo "    https://www.azul.com/downloads/"
    exit -1
}

runDataLoader() {
    checkJavaVersion
    SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
    java -cp "${SCRIPT_DIR}/../*" com.salesforce.dataloader.process.DataLoaderRunner $@
}