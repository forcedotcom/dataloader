#!/bin/sh -f

# $1 - password for the java key store containing code-signing cert
# $2 - file name of the java key store.
# $3 - URL of the TSA (Timestamp Authority)

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <Keystore Password> <Keystore File> <TSA URL>" >&2
  exit 1
fi

# build for macx86_64 platform
mvn clean package -DskipTests -D"jarsigner.storepass=$1" -D"jarsigner.keystore=$2" -D"jarsigner.tsa=$3" -D'jarsigner.skip=false' -D'jarsigner.alias=1' -Pmacx86_64,zip,-win32_x86_64,-macarm64

# build for Mac ARM platform
#mvn clean package -DskipTests -D"jarsigner.storepass=$1" -D"jarsigner.keystore=$2" -D"jarsigner.tsa=$3" -D'jarsigner.skip=false' -D'jarsigner.alias=1' -Pmacarm64,zip,-win32_x86_64,-macx86_64

cp target/mac/dataloader_mac.zip .

# build for Windows platform
mvn clean package -DskipTests -D"jarsigner.storepass=$1" -D"jarsigner.keystore=$2" -D"jarsigner.tsa=$3" -D'jarsigner.skip=false' -D'jarsigner.alias=1' -Pwin32_x86_64,zip,-macarm64,-macx86_64

cp target/win/dataloader_win.zip .
