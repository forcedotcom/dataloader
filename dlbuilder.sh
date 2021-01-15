#!/bin/sh -f

# $1 - password for the java key store containing code-signing cert
# $2 - file name of the java key store.

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 KeystorePassword KeystoreFile" >&2
  exit 1
fi

mvn clean package -DskipTests  -D"jarsigner.storepass=$1"  -D"jarsigner.keystore=$2" -D'jarsigner.skip=false' -D'jarsigner.alias=1' -Pzip

cp target/mac/dataloader_mac.zip .

mvn clean package -DskipTests -D"jarsigner.storepass=$1" -D"jarsigner.keystore=$2" -D'jarsigner.skip=false' -D'jarsigner.alias=1' -Pwin64,zip,-mac64

cp target/win/dataloader_win.zip .
