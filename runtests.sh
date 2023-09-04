#!/bin/bash -f

usage() {
  echo "Usage: "
  echo "$0 [-d] [-t <test class name without the package prefix com.salesforce.dataloader e.g. dyna.DateConverterTest>] <test org URL> <test admin username> <test regular user username> <encrypted test password>"
  echo "listening on port 5005 for IDE to start the debugging session if -d is specified."
  exit 1
}

# To generate encrypted password
# build jar with the following command:
# mvn clean package -DskipTests
# run the following command to get encrypted password for the test admin account:
#java -cp target/dataloader-*.jar com.salesforce.dataloader.security.EncryptionUtil -e <password>

test=""
debug=""
encryptionFile=${HOME}/.dataloader/dataLoader.key
password=""

while getopts ":dv:t:f:" flag
do
  case "${flag}" in
    d)
      debug="-Dmaven.surefire.debug"
      ;;
    t)
      test="-Dtest=com.salesforce.dataloader.${OPTARG}"
      ;;
    f)
      encryptionFile="${OPTARG}"
      ;;
    *)
      usage
      ;;
  esac
done
shift $((OPTIND -1))

# $1 contains the test org URL
# $2 test admin user username
# $3 test regular user username
# $4 test admin and regular user encoded password

if [ "$#" -lt 4 ]; then
  usage
fi 

echo $@
mvn clean package
encryptedPassword="$(java -cp ./target/* com.salesforce.dataloader.process.DataLoaderRunner run.mode=encrypt -e ${4} ${encryptionFile} 2>&1 /dev/null | tail -1)"
echo ${encryptedPassword}
mvn -Dtest.endpoint=${1} -Dtest.user.default=${2} -Dtest.user.restricted=${3} -Dtest.password=${encryptedPassword} -Dtest.encryptionFile=${encryptionFile} verify ${debug} ${test}