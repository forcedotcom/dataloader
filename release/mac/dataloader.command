#!/bin/bash
DATALOADER_VERSION="@@FULL_VERSION@@"
DATALOADER_SHORT_VERSION=$(echo ${DATALOADER_VERSION} | cut -d'.' -f 1)
DATALOADER_UBER_JAR_NAME="dataloader-${DATALOADER_VERSION}-uber.jar"

echo ""
echo "*************************************************************************"
echo "**            ___  ____ ___ ____   _    ____ ____ ___  ____ ____       **"
echo "**            |  \ |__|  |  |__|   |    |  | |__| |  \ |___ |__/       **"
echo "**            |__/ |  |  |  |  |   |___ |__| |  | |__/ |___ |  \       **"
echo "**                                                                     **"
echo "**  Data Loader v${DATALOADER_SHORT_VERSION} is a Salesforce supported Open Source project to   **"
echo "**  help you import data to and export data from your Salesforce org.  **"
echo "**  It requires Java JRE 11 or later to run.                           **"
echo "**                                                                     **"
echo "**  Github Project Url:                                                **"
echo "**       https://github.com/forcedotcom/dataloader                     **"
echo "**  Salesforce Documentation:                                          **"
echo "**       https://help.salesforce.com/articleView?id=data_loader.htm    **"
echo "**                                                                     **"
echo "*************************************************************************"
echo ""

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f 2 | cut -d'.' -f 1)

if [ -z "${JAVA_VERSION}" ] | [ ${JAVA_VERSION} \< 11 ]
then
    echo "Java JRE 11 or later is not installed. For example, download and install Zulu OpenJDK 11 or later JRE for macOS from https://www.azul.com/downloads/zulu/zulu-mac/"
else
    cd DATALOADER_WORK_DIRECTORY_PLACEHOLDER 
    java -XstartOnFirstThread -jar ${DATALOADER_UBER_JAR_NAME} salesforce.config.dir=DATALOADER_WORK_DIRECTORY_PLACEHOLDER/configs
fi
