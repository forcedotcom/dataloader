#!/bin/sh -f

# $1 - password for the java key store containing code-signing cert
# $2 - file name of the java key store.
# $3 - URL of the TSA (Timestamp Authority)

unsignedArtifacts=false
while getopts u flag
do
    case "${flag}" in
        u) unsignedArtifacts=true;;
    esac
done

if [ ${unsignedArtifacts} = false  -a  "$#" -ne 3 ]; then
  echo "Usage: "
  echo "$0 -u"
  echo "$0 <Keystore Password> <Keystore File> <TSA URL>" >&2
  exit 1
fi

# build for macx86_64 platform
signingOptions=
if [ ${unsignedArtifacts} = false ]; then
  signingOptions="-Djarsigner.storepass=$1 -Djarsigner.keystore=$2 -Djarsigner.tsa=$3 -Djarsigner.skip=false -Djarsigner.alias=1"
fi

mvn clean package -DskipTests ${signingOptions} -DtargetOS=macos_x86_64 -Pzip

cp target/mac/dataloader_mac.zip .

# build for Mac ARM platform
#mvn clean package -DskipTests ${signingOptions} -DtargetOS=macos_arm_64 -Pzip

#cp target/mac/dataloader_mac.zip ./dataloader_mac_arm64.zip

# build for Windows platform
mvn clean package -DskipTests ${signingOptions} -DtargetOS=windows_x86_64 -Pzip

cp target/win/dataloader_win.zip .

# build for linux platform
#mvn clean package -DskipTests ${signingOptions} -DtargetOS=linux_x86_64 -Pzip

#cp target/linux/dataloader_linux.zip .
