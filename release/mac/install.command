#!/bin/bash
DATALOADER_VERSION="@@FULL_VERSION@@"
DATALOADER_SHORT_VERSION=$(echo ${DATALOADER_VERSION} | cut -d'.' -f 1)
DATALOADER_UBER_JAR_NAME="dataloader-${DATALOADER_VERSION}-uber.jar"
MIN_JAVA_VERSION=@@MIN_JAVA_VERSION@@

echo ""
echo "*************************************************************************"
echo "**            ___  ____ ___ ____   _    ____ ____ ___  ____ ____       **"
echo "**            |  \ |__|  |  |__|   |    |  | |__| |  \ |___ |__/       **"
echo "**            |__/ |  |  |  |  |   |___ |__| |  | |__/ |___ |  \       **"
echo "**                                                                     **"
echo "**  Data Loader v$DATALOADER_SHORT_VERSION is a Salesforce supported Open Source project to   **"
echo "**  help you import data to and export data from your Salesforce org.  **"
echo "**  It requires Java JRE ${MIN_JAVA_VERSION} or later to run.                           **"
echo "**                                                                     **"
echo "**  Github Project Url:                                                **"
echo "**       https://github.com/forcedotcom/dataloader                     **"
echo "**  Salesforce Documentation:                                          **"
echo "**       https://help.salesforce.com/articleView?id=data_loader.htm    **"
echo "**                                                                     **"
echo "*************************************************************************"
echo ""

echo Data Loader installation requires you to provide an installation directory to create a version-specific subdirectory for the installation artifacts. 
echo It uses \'${HOME}\/\<relative path\>\' as the installation directory if you provide a relative path for the installation directory.
echo ""
read -p "Provide the installation directory [default: dataloader] : " INSTALLATION_DIR_NAME
INSTALLATION_DIR_NAME=${INSTALLATION_DIR_NAME:-dataloader}

[[ ${INSTALLATION_DIR_NAME} = /* ]] && DL_FULL_PATH="${INSTALLATION_DIR_NAME}/v${DATALOADER_VERSION}" || DL_FULL_PATH="${HOME}/${INSTALLATION_DIR_NAME}/v${DATALOADER_VERSION}"

echo Data Loader v$DATALOADER_VERSION will be installed in: $DL_FULL_PATH

# make sure there is a directory to install files
if [ -d "$DL_FULL_PATH" ]; then
    while true
    do
         echo ""
         echo Do you want to overwrite previously installed versions of Data Loader
         echo v$DATALOADER_VERSION and configurations in \'$DL_FULL_PATH\'?
         echo If not, installation will quit and you can restart installation using
         read -r -p "another directory.[Yes/No] " input
         case $input in
             [yY][eE][sS]|[yY])
                echo "Deleting existing Data Loader v$DATALOADER_VERSION ... "
                rm -rf "$DL_FULL_PATH"
                break
              ;;
             [nN][oO]|[nN])
                echo Data Loader installation is quitting.
                exit
              ;;
             *)
                echo "Type Yes or No."
             ;;
         esac
    done
fi

echo  Creating directory: $DL_FULL_PATH
mkdir -p "$DL_FULL_PATH"
SHELL_PATH=$(dirname "$0")
rsync -r "$SHELL_PATH"/.  "$DL_FULL_PATH" \
  --exclude='.*' \
  --exclude="install.command" \
  --exclude="dataloader.ico" \
  --exclude="fileicon" \
  --exclude="*.zip" \
  --exclude="META-INF"

sed -i '' 's|DATALOADER_WORK_DIRECTORY_PLACEHOLDER|'"$DL_FULL_PATH"'|g'  "$DL_FULL_PATH"/dataloader.command

"$SHELL_PATH"/fileicon set  "$DL_FULL_PATH"/dataloader.command "$SHELL_PATH"/dataloader.ico 1>/dev/null

while true
do
     echo ""
     read -r -p "Do you want to create an icon to launch Data Loader from your Desktop? [Yes/No] " input
     case $input in
         [yY][eE][sS]|[yY])
            rm   "$HOME/Desktop/DataLoader $DATALOADER_VERSION" 2>/dev/null
            ln -s  "$DL_FULL_PATH/dataloader.command"  "$HOME/Desktop/DataLoader $DATALOADER_VERSION"
            "$SHELL_PATH"/fileicon set  "$HOME/Desktop/DataLoader $DATALOADER_VERSION" "$SHELL_PATH"/dataloader.ico 1>/dev/null
            break
          ;;
         [nN][oO]|[nN])
            break
          ;;
         *)
            echo "Type Yes or No."
         ;;
     esac
done

while true
do
     echo ""
     echo "Do you want to create a link to launch Data Loader from your Applications"
     read -r -p "directory? [Yes/No] " input
     case $input in
         [yY][eE][sS]|[yY])
            rm   "/Applications/DataLoader $DATALOADER_VERSION" 2>/dev/null
            ln -s  "$DL_FULL_PATH/dataloader.command"  "/Applications/DataLoader $DATALOADER_VERSION"
            "$SHELL_PATH"/fileicon set  "/Applications/DataLoader $DATALOADER_VERSION" "$SHELL_PATH"/dataloader.ico 1>/dev/null
            break
          ;;
         [nN][oO]|[nN])
            break
          ;;
         *)
            echo "Type Yes or No."
         ;;
     esac
done

echo  Data Loader installation is quitting.
echo ""
