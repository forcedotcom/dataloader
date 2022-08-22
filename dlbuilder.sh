#!/bin/sh 
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
  echo "$0"
  echo "$0 <keystore password> <PKCS11 config file> <signature algorithm, e.g. RSA1024, RSA2048, ECCP256, ECCP384> <TSA URL> <certchain file> <keystore alias>"
  echo "$0 -n"
  exit 1
}

run_mvn() {
# $1 - true => sign artifacts, false => do not sign artifacts
# $2 - true - zip, false - do not zip
# $3 - keystore password
# $4 - PKCS11 config file
# $5 - signature algorithm
# $6 - TSA URL
# $7 - certchain pem file
# $8 - keystore alias

  # run a mvn build to download dependencies from the central maven repo
  mvn clean compile
  # remove vulnerable class from log4j jar file
  log4j_version_num=`awk '/<artifactId>log4j-core/{getline; print}' pom.xml | awk '{$1=$1};1' `
  log4j_version_num=${log4j_version_num#<version>};
  log4j_version_num=${log4j_version_num%</version>};
  
  echo "removing JndiLookup.class from ${log4j_version_num}"
  zip -q -d ${HOME}/.m2/repository/org/apache/logging/log4j/log4j-core/${log4j_version_num}/log4j-core-${log4j_version_num}.jar org/apache/logging/log4j/core/lookup/JndiLookup.class
  zip -q -d ${HOME}/.m2/repository/org/apache/logging/log4j/log4j-core/${log4j_version_num}/log4j-core-${log4j_version_num}.jar org/apache/logging/log4j/core/appender/mom/JmsAppender.class
  zip -q -d ${HOME}/.m2/repository/org/apache/logging/log4j/log4j-core/${log4j_version_num}/log4j-core-${log4j_version_num}.jar org/apache/logging/log4j/core/appender/db/jdbc/JdbcAppender.class

  # build uber jar
  mvn clean package -DskipTests 

  jarfile=`find ./target -name dataloader-*-uber.jar -not -path "./target/win/*" -not -path "./target/mac/*" -not -path "./target/linux/*" -print -quit` 
  # sign uber jar 
  if [ $1 = true ]; then
    jarsigner -storepass "$3" -verbose -providerClass sun.security.pkcs11.SunPKCS11 -providerArg "$4" -keystore NONE -storetype PKCS11 -sigalg "$5" -tsa "$6" -certchain "$7" ${jarfile} "$8"
  fi

  if [ $2 = true ]
  then
    echo "packaging dataloader_mac_<version>.zip"
     mvn package -DskipTests -Duberjar.skip -Pzip,mac_x86_64,!win32_x86_64,!linux_x86_64
    cp target/mac/*.zip .
    echo "packaging dataloader_win_<version>.zip"
    mvn package -DskipTests -Duberjar.skip -Pzip,!mac_x86_64,win32_x86_64,!linux_x86_64
    cp target/win/dataloader_win_v*.zip .
    echo "packaging dataloader_linux_<version>.zip"
    mvn package -DskipTests -Duberjar.skip -Pzip,!mac_x86_64,!win32_x86_64,linux_x86_64
    cp target/linux/dataloader_linux_v*.zip .
  fi

  if [ $1 = true ]; then
    jarsigner -storepass "$3" -verbose -providerClass sun.security.pkcs11.SunPKCS11 -providerArg "$4" -keystore NONE -storetype PKCS11 -sigalg "$5" -tsa "$6" -certchain "$7" ./dataloader_mac.zip "$8"
    jarsigner -storepass "$3" -verbose -providerClass sun.security.pkcs11.SunPKCS11 -providerArg "$4" -keystore NONE -storetype PKCS11 -sigalg "$5" -tsa "$6" -certchain "$7" ./dataloader_win.zip "$8"
    jarsigner -storepass "$3" -verbose -providerClass sun.security.pkcs11.SunPKCS11 -providerArg "$4" -keystore NONE -storetype PKCS11 -sigalg "$5" -tsa "$6" -certchain "$7" ./dataloader_linux.zip "$8"
  fi
}

#################

doSign=false
doZip=true

while getopts ":n" flag
do
    case "${flag}" in
        n) 
          doZip=false
          ;;
        *)
          usage
          ;;
    esac
done
shift $((OPTIND -1))

if [ "$#" -eq 6 ]; then
  doSign=true
else
  if [ ${doZip} = true -a "$#" -ne 0 ]; then
    usage
  fi
fi


run_mvn "${doSign}" "${doZip}" "$1" "$2" "$3" "$4" "$5" "$6"
