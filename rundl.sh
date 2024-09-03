#!/bin/bash -f

debug=""
batchmodeargs=""
encryptionargs=""
configdir="salesforce.config.dir=./configs"
while getopts ":dbe:v:" flag
do
  case "${flag}" in
    d)
      debug="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=y"
      ;;
    b)
      batchmodeargs="./.. upsertAccounts run.mode=batch"
      ;;
    e)
      encryptionargs="run.mode=encrypt"
      configdir=""
      ;;
    v)
      version="${OPTARG}"
      ;;
  esac
done

jarname="$(find ./target -name 'dataloader-[0-9][0-9].[0-9].[0-9].jar' | tail -1)"


java ${debug} -cp ${jarname} com.salesforce.dataloader.process.DataLoaderRunner ${batchmodeargs} ${configdir} ${encryptionargs} $@
#java ${debug} -cp ${jarname} com.salesforce.dataloader.action.visitor.RESTTest
