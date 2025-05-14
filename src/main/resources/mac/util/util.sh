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

runDataLoader() {
    initVars
    SCRIPT_DIR=$( cd -- "$( dirname -- "$0" )" >/dev/null 2>&1 && pwd -P )
    java -cp "${SCRIPT_DIR}/*" com.salesforce.dataloader.process.DataLoaderRunner "$@"
}