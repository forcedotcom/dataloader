#!/bin/sh -f
# $1 - password for the java key store containing code-signing cert
# $2 - file name of the java key store.
# $3 - URL of the TSA (Timestamp Authority)

usage() {
  echo "Usage: "
  echo "$0 -u"
  echo "$0 <Keystore Password> <Keystore File> <TSA URL>" >&2
  echo "$0 -n macos_x86_64 | macos_arm_64 | windows_x86_64 | linux_x86_64"
  exit 1
}

run_mvn() {
# $1 - code-signing params
# $2 - target OS name
# $3 - true - zip, false - do not zip

  if [ "$2" = macos_x86_64 ]
  then
    from=target/mac/dataloader_mac.zip
    to=./dataloader_mac.zip
  elif [ "$2" = macos_arm_64 ] 
  then
    from=target/mac/dataloader_mac.zip
    to=./dataloader_mac_arm64.zip
  elif [ "$2" = windows_x86_64 ] 
  then
    from=target/win/dataloader_win.zip
    to=./dataloader_win.zip
  elif [ "$2" = linux_x86_64 ] 
  then
    from=target/linux/dataloader_linux.zip
    to=./dataloader_linux.zip
  else
    usage
  fi

  zipOption=""
  if [ $3 = true ]
  then
    zipOption="-Pzip"
  fi
  mvn clean package -DskipTests $1 -DtargetOS=$2 ${zipOption}
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

if [ ${unsignedArtifacts} = false  -a  "$#" -ne 3 ]; then
  usage
fi

signingOptions=
if [ ${unsignedArtifacts} = false ]; then
  signingOptions="-Djarsigner.storepass=$1 -Djarsigner.keystore=$2 -Djarsigner.tsa=$3 -Djarsigner.skip=false -Djarsigner.alias=1"
fi

if [ ${doZip} = true ]; then
  run_mvn "${signingOptions}" "macos_x86_64" ${doZip}
  run_mvn "${signingOptions}" "windows_x86_64" ${doZip}
#  run_mvn "${signingOptions}" "macos_arm_64" ${doZip}
#  run_mvn "${signingOptions}" "linux_x86_64" ${doZip}
else
  run_mvn "${signingOptions}" "${targetOS}" ${doZip}
fi
