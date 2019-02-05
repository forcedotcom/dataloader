# How to create your desktop link in Mac
#    export DESKTOP="/Users/xbian/Desktop"
#    run ln -s  $DATALODER_WORK_DIRECTORY/dataloader.command  $DESKTOP/dataloader.command


# Please change work directory accordingly
export DATALODER_WORK_DIRECTORY="$HOME/dataloader"


# DATALODER_WORK_DIRECTORY/
#                         /configs
#                          config.properties
#                         /libs
#                          dataloader-xxx-uber.jar


# This is used for Mac user Terminal
if [ -z "$JAVA_HOME" ]
then 
    echo "Please download Zulu Openjdk here: https://www.azul.com/downloads/zulu/zulu-mac/ and set up JAVA_HOME environment variable to point to your jdk directory
    eg: export JAVA_HOME=\"/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home\" "

else
    echo "$JAVA_HOME"
    cd $DATALODER_WORK_DIRECTORY   #change to your own customized directory
    java -XstartOnFirstThread -jar libs/dataloader-44.1.0-uber.jar -Dsalesforce.config.dir=configs
fi 