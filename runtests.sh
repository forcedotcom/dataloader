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

while getopts ":dv:t:" flag
do
  case "${flag}" in
    d)
      debug="-Dmaven.surefire.debug"
      ;;
    t)
      test="-Dtest=com.salesforce.dataloader.${OPTARG}"
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

sed -i '' "s/http:\/\/testendpoint/${1}/g" pomtest.xml
sed -i '' "s/admin@org.com/${2}/g" pomtest.xml
sed -i '' "s/standard@org.com/${3}/g" pomtest.xml
sed -i '' "s/<test\.password>/<test\.password>${4}/g" pomtest.xml

mvn -f pomtest.xml clean test -Pintegration-test ${debug} ${test}
rm pomtest.xml
