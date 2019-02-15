#!/bin/bash

echo ""
echo "***************************************************************************"
echo "**            ___  ____ ___ ____   _    ____ ____ ___  ____ ____         **"
echo "**            |  \ |__|  |  |__|   |    |  | |__| |  \ |___ |__/         **"
echo "**            |__/ |  |  |  |  |   |___ |__| |  | |__/ |___ |  \         **"
echo "**                                                                       **"
echo "**  Dataloder is a Salesforce supported Open Source project to help      **"
echo "**  Salesforce user to import and export data with Salesforce platform.  **"
echo "**  It requires Zulu OpenJDK 11 or higher to run.                        **"
echo "**                                                                       **"
echo "**  Github Project Url:                                                  **"
echo "**       https://github.com/forcedotcom/dataloader                       **"
echo "**  Salesforce Documentation:                                            **"
echo "**       https://help.salesforce.com/articleView?id=data_loader.htm      **"
echo "**                                                                       **"
echo "***************************************************************************"
echo ""

export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Please change work directory accordingly, eg you create a folder "dataloader" in your user home directory.
# export DATALODER_WORK_DIRECTORY="$HOME/dataloader"


if [ -z "$JAVA_HOME" ]
then
    echo "Please download Zulu Openjdk here: https://www.azul.com/downloads/zulu/zulu-mac/"
else
    echo "$JAVA_HOME"
    cd DATALODER_WORK_DIRECTORY   #change to your own customized directory
    java -XstartOnFirstThread -jar dataloader-45.0.0-uber.jar salesforce.config.dir=DATALODER_WORK_DIRECTORY/configs
fi
