#!/bin/bash -f

usage() {
  echo "Usage: "
  echo "$0 [-v <version number such as 52.0.0>] [-d] [-t <test class name without the package prefix com.salesforce.dataloader e.g. dyna.DateConverterTest>] <test org URL> <test admin username> <test regular user username> <encrypted test password>"
  echo "listening on port 5005 for IDE to start the debugging session if -d is specified."
  exit 1
}

# To generate encrypted password
# build uber jar with the following command:
# mvn -DtargetOS=macos_x86_64 clean package -DskipTests
# run the following command to get encrypted password for the test admin account:
#java -cp target/dataloader-*-uber.jar com.salesforce.dataloader.security.EncryptionUtil -e <password>

version=""
test=""
debug=""

while getopts ":dv:t:" flag
do
  case "${flag}" in
    d)
      debug="-Dmaven.surefire.debug"
      ;;
    t)
      test="-Dtest=com.salesforce.dataloader.${OPTARG}"
      ;;
    v)
      version="${OPTARG}"
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

echo "num arguments = $#"

if [ "$#" -lt 4 ]; then
  usage
fi 

echo $@

cp pom.xml pomtest.xml

if [ ! ${version} = "" ]
then
  sed -i '' "s/<version>[0-9][0-9]\.[0-9]\.[0-9]<\/version>/<version>${version}<\/version>/g" pomtest.xml
  sed -i '' "s/<force.wsc.version>[0-9][0-9]\.[0-9]\.[0-9]<\/force.wsc.version>/<force.wsc.version>${version}<\/force.wsc.version>/g" pomtest.xml
fi

sed -i '' "s/http:\/\/testendpoint/${1}/g" pomtest.xml
sed -i '' "s/admin@org.com/${2}/g" pomtest.xml
sed -i '' "s/standard@org.com/${3}/g" pomtest.xml
sed -i '' "s/<test\.password>/<test\.password>${4}/g" pomtest.xml

# delete H2 directory left by previous test run
mvn -f pomtest.xml clean test -Pintegration-test ${debug} ${test}
rm pomtest.xml
