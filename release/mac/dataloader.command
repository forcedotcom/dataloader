# How to create your desktop link in Mac
    #    run ln -s  $$HOME/dataloader/dataloader.command  $HOME/Desktop/dataloader.command

export JAVA_HOME=$(/usr/libexec/java_home -v 11)


# Please change work directory accordingly, eg you create a folder "dataloader" in your user home directory.
export DATALODER_WORK_DIRECTORY="$HOME/dataloader"

# DATALODER_WORK_DIRECTORY/
#                         /configs
#                          config.properties
#                         /jars
#                          dataloader-xxx-uber.jar


# This is used for Mac user Terminal
if [ -z "$JAVA_HOME" ]
then
    echo "Please download Zulu Openjdk here: https://www.azul.com/downloads/zulu/zulu-mac/"
else
    echo "$JAVA_HOME"
    cd $DATALODER_WORK_DIRECTORY   #change to your own customized directory
    java -XstartOnFirstThread -jar jars/dataloader-45.0.0-uber.jar salesforce.config.dir=$DATALODER_WORK_DIRECTORY/configs
fi
