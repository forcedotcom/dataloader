#!/bin/bash -f

debug=""
batchmodeargs=""

while getopts ":dbv:" flag
do
  case "${flag}" in
    d)
      debug="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=y"
      ;;
    b)
      batchmodeargs="./configs upsertAccounts run.mode=batch"
      ;;
    v)
      version="${OPTARG}"
      ;;
  esac
done

jarname="$(find ./target -name 'dataloader-[0-9][0-9].[0-9].[0-9].jar' | tail -1)"
java ${debug} -cp ${jarname} com.salesforce.dataloader.process.DataLoaderRunner ${batchmodeargs} $@ salesforce.config.dir=./configs