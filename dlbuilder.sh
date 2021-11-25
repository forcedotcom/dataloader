#!/bin/sh -f
# script parameters
# $1 - password for the java key store containing code-signing cert
# $2 - PKCS11 config file (see example below)
# $3 - signature algorithm such as RSA1024, RSA2048, ECCP256, ECCP384
# $4 - URL of the TSA (Timestamp Authority)
# $5 - location of the .pem file containing intermediate certs in the cert chain
# $6 - keystore alias

# Example PKCS11 config file 
#
#    name = OpenSC-PKCS11
#    description = SunPKCS11 via OpenSC
#    library = /usr/local//Cellar/opensc/0.22.0/lib/pkcs11/opensc-pkcs11.so
#    slotListIndex = 0
#

usage() {
  echo "Usage: "
  echo "$0 -u"
  echo "$0 <keystore password> <PKCS11 config file> <signature algorithm, e.g. RSA1024, RSA2048, ECCP256, ECCP384> <TSA URL> <certchain file> <keystore alias>"
  echo "$0 -n mac_x86_64 | mac_aarch64 | win32_x86_64 | linux_x86_64"
  exit 1
}

run_mvn() {
# $1 - code-signing params
# $2 - target OS name
# $3 - true - zip, false - do not zip
# $4 - keystore password
# $5 - PKCS11 config file
# $6 - signature algorithm
# $7 - TSA URL
# $8 - certchain pem file
# $9 - keystore alias

  osSuffix=""
  if [ "$2" = mac_x86_64 ]
  then
    osSuffix="mac"
    zipdir="target/${osSuffix}"
    from="${zipdir}/dataloader_${osSuffix}.zip"
    to=./dataloader_${osSuffix}.zip
  elif [ "$2" = mac_aarch64 ] 
  then
    osSuffix="mac"
    zipdir="target/${osSuffix}"
    from="${zipdir}/dataloader_${osSuffix}.zip"
    to=./dataloader_${osSuffix}_arm64.zip
  elif [ "$2" = win32_x86_64 ] 
  then
    osSuffix="win"
    zipdir="target/${osSuffix}"
    from=target/win/dataloader_${osSuffix}.zip
    to=./dataloader_${osSuffix}.zip
  elif [ "$2" = linux_x86_64 ] 
  then
    osSuffix="linux"
    zipdir="target/${osSuffix}"
    from=target/win/dataloader_${osSuffix}.zip
    to=./dataloader_${osSuffix}.zip
  else
    usage
  fi

  # build uber jar
  mvn clean package -DskipTests -DtargetOS=$2 

  jarfile=`find ./target -name dataloader-*-uber.jar -not -path "./target/win/*" -not -path "./target/mac/*" -not -path "./target/linux/*" -print -quit` 
  # sign uber jar if -u flag not set
  if [ $1 = false ]; then
    jarsigner -storepass "$4" -verbose -providerClass sun.security.pkcs11.SunPKCS11 -providerArg "$5" -keystore NONE -storetype PKCS11 -sigalg "$6" -tsa "$7" -certchain "$8" ${jarfile} "$9"
  fi

  # zip if requested, make sure to not rebuild uber file to preserve the signed copy
  zipOption=""
  if [ $3 = true ]
  then
    zipOption="-Duberjar.skip -Pzip"
    mvn package -DskipTests -DtargetOS=$2 -Duberjar.skip -Pzip
  fi

  # sign zip if -u flag not set
  if [ $1 = false ]; then
    jarsigner -storepass "$4" -verbose -providerClass sun.security.pkcs11.SunPKCS11 -providerArg "$5" -keystore NONE -storetype PKCS11 -sigalg "$6" -tsa "$7" -certchain "$8" target/${osSuffix}/dataloader_${osSuffix}.zip "$9"
  fi

  # copy zip if zip is requested
  if [ $3 = true ]
  then
    cp ${from} ${to}
  fi
}

#################

unsignedArtifacts=false
doZip=true

while getopts ":un:" flag
do
    case "${flag}" in
        u)
          unsignedArtifacts=true
          ;;
        n) 
          targetOS=${OPTARG}
          doZip=false
          unsignedArtifacts=true
          ;;
        *)
          usage
          ;;
    esac
done
shift $((OPTIND -1))

if [ ${unsignedArtifacts} = false  -a  "$#" -ne 6 ]; then
  usage
fi

signingOptions=
if [ ${unsignedArtifacts} = false ]; then
  signingOptions="-Djarsigner.storepass=$1 -Djarsigner.keystore=$2 -Djarsigner.tsa=$3 -Djarsigner.skip=false -Djarsigner.alias=1"
fi

if [ ${doZip} = true ]; then
  run_mvn "${unsignedArtifacts}" "mac_x86_64" ${doZip} "$1" "$2" "$3" "$4" "$5" "$6"
  run_mvn "${unsignedArtifacts}" "win32_x86_64" ${doZip} "$1" "$2" "$3" "$4" "$5" "$6"
#  run_mvn "${unsignedArtifacts}" "mac_aarch64" ${doZip} "$1" "$2" "$3" "$4" "$5" "$6"
#  run_mvn "${unsignedArtifacts}" "linux_x86_64" ${doZip} "$1" "$2" "$3" "$4" "$5" "$6"
else
  run_mvn "${unsignedArtifacts}" "${targetOS}" ${doZip} "$1" "$2" "$3" "$4" "$5" "$6"
fi
