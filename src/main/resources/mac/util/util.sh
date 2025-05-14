#!/bin/sh

# Detect shell
if [ -n "${BASH_VERSION:-}" ]; then
    SHELL_NAME="bash"
elif [ -n "${ZSH_VERSION:-}" ]; then
    SHELL_NAME="zsh"
else
    SHELL_NAME="sh"
fi

include() {
    if [ -f "$1" ]; then
        . "$1"
    fi
}

initVars() {
    DATALOADER_VERSION="@@FULL_VERSION@@"
    DATALOADER_SHORT_VERSION=$(echo "${DATALOADER_VERSION}" | cut -d'.' -f 1)
    MIN_JAVA_VERSION=@@MIN_JAVA_VERSION@@
    
    # Source shell-specific files if they exist
    if [ "$SHELL_NAME" = "bash" ]; then
        include "${HOME}/.bash_profile"
        include "${HOME}/.bashrc"
    elif [ "$SHELL_NAME" = "zsh" ]; then
        include "${HOME}/.zsh_profile"
        include "${HOME}/.zshrc"
    fi
    include "${HOME}/.profile"
}

checkJavaVersion() {
    initVars
    
    echo "Data Loader requires Java JRE ${MIN_JAVA_VERSION} or later. Checking if it is installed..."
    if [ -n "${DATALOADER_JAVA_HOME:-}" ]; then
        JAVA_HOME="${DATALOADER_JAVA_HOME}"
    fi
    
    PATH="${JAVA_HOME}/bin:${PATH}"
    JAVA_VERSION=$(java -version 2>&1 | grep -i version | cut -d'"' -f 2 | cut -d'.' -f 1)
    if [ -z "${JAVA_VERSION:-}" ]; then
        echo "Did not find java command."
        echo ""
        exitWithJavaDownloadMessage
    fi
    if [ "${JAVA_VERSION}" -lt "${MIN_JAVA_VERSION}" ]; then
        echo "Found Java JRE version ${JAVA_VERSION} whereas Data Loader requires Java JRE ${MIN_JAVA_VERSION} or later."
        exitWithJavaDownloadMessage
    fi
}

exitWithJavaDownloadMessage() {
    echo "Java JRE ${MIN_JAVA_VERSION} or later is not installed or DATALOADER_JAVA_HOME environment variable is not set."
    echo "For example, download and install Zulu JRE ${MIN_JAVA_VERSION} or later from here:"
    echo "    https://www.azul.com/downloads/"
    exit 1
}

runDataLoader() {
    checkJavaVersion
    SCRIPT_DIR=$( cd -- "$( dirname -- "$0" )" >/dev/null 2>&1 && pwd -P )
    java -cp "${SCRIPT_DIR}/*" com.salesforce.dataloader.process.DataLoaderRunner "$@"
}