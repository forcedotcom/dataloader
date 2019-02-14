#!/bin/bash

echo This will create a directory in your home directory to install Dataloader program.
read -p "Please enter directory name you want to use [dataloader]: " INSTALLATION_DIR_NAME
INSTALLATION_DIR_NAME=${INSTALLATION_DIR_NAME:-dataloader}
DL_FULL_PATH="$HOME/$INSTALLATION_DIR_NAME"
echo We will install to: $DL_FULL_PATH

# make sure there is a directory to install files
if [ -d "$DL_FULL_PATH" ]; then
    echo Directory $DL_FULL_PATH exists, will not proceed installation to existing directory.

    while true
    do
         read -r -p "Do you want to delete $DL_FULL_PATH? [Yes/No] " input
         case $input in
             [yY][eE][sS]|[yY])
                  echo "Deleting existing directory: $DL_FULL_PATH ... "
                  rm -rf "$DL_FULL_PATH"
                  break
              ;;
             [nN][oO]|[nN])
                echo  Quit dataloader installer
                exit
              ;;
             *)
             echo "Invalid input..."
             ;;
         esac
    done
fi

echo  Create directory:$DL_FULL_PATH
mkdir -p "$DL_FULL_PATH"
SHELL_PATH=$(dirname "$0")
rsync -r "$SHELL_PATH"/.  "$DL_FULL_PATH"  --exclude='.*'
rm ~/"$INSTALLATION_DIR_NAME"/install.command
rm ~/"$INSTALLATION_DIR_NAME"/dataloader.ico
rm ~/"$INSTALLATION_DIR_NAME"/fileicon


sed -i '' 's|DATALODER_WORK_DIRECTORY|'"$DL_FULL_PATH"'|g'  "$DL_FULL_PATH"/dataloader.command

"$SHELL_PATH"/fileicon set  "$DL_FULL_PATH"/dataloader.command "$SHELL_PATH"/dataloader.ico

while true
do
     read -r -p "Do you want to create a link to dataloader.command in your desktop? [Yes/No] " input
     case $input in
         [yY][eE][sS]|[yY])
              rm   $HOME/Desktop/dataloader.command 2>/dev/null
              ln -s  "$DL_FULL_PATH/dataloader.command"  $HOME/Desktop/DataLoader
              "$SHELL_PATH"/fileicon set  $HOME/Desktop/DataLoader "$SHELL_PATH"/dataloader.ico

              break
          ;;
         [nN][oO]|[nN])
            echo  Quit dataloader installer
            exit
          ;;
         *)
         echo "Invalid input..."
         ;;
     esac
done
