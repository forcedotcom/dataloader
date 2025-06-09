#!/bin/zsh

# Reset getopts state (important for zsh)
OPTIND=1

# Initialize variables
debug=""
batchmodeargs=""
encryptionargs=""
configdir="salesforce.config.dir=./configs"
version=""

# Parse command line options
while getopts ":dbe:v:" flag; do
  case "${flag}" in
    d)
      debug="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=0.0.0.0:5005,suspend=y"
      ;;
    b)
      # batchmodeargs="run.mode=batch ./configs upsertAccounts"
      batchmodeargs="run.mode=batch ./configs extractAccounts"
      # batchmodeargs="run.mode=batch ./configs extract sfdc.entity=account sfdc.extractionSOQL='select id,name from account' dataAccess.name=batchextract.csv"
      configdir=""
      ;;
    e)
      encryptionargs="run.mode=encrypt"
      configdir=""
      ;;
    v)
      version="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -${OPTARG}" >&2
      exit 1
      ;;
    :)
      echo "Option -${OPTARG} requires an argument." >&2
      exit 1
      ;;
  esac
done

# Shift past the processed options
shift $((OPTIND-1))

# Find the JAR file
jarname="$(find ./target -name 'dataloader-[0-9][0-9].[0-9].[0-9].jar' | tail -1)"

# Validate JAR exists
if [[ -z "$jarname" || ! -f "$jarname" ]]; then
    echo "Error: Data Loader JAR not found in ./target" >&2
    exit 1
fi

# Validate DATALOADER_JAVA_HOME
if [[ -z "$DATALOADER_JAVA_HOME" ]]; then
    echo "Error: DATALOADER_JAVA_HOME environment variable not set" >&2
    exit 1
fi

# Debug output (optional)
if [[ -n "$debug" ]]; then
    echo "🔍 Starting Salesforce Data Loader in DEBUG mode..."
    echo "   Debug Port: 5005"
    echo "   JAR: $jarname"
    echo ""
fi

# Execute Data Loader
exec "${DATALOADER_JAVA_HOME}/bin/java" --enable-native-access=ALL-UNNAMED ${=debug} -cp "${jarname}" com.salesforce.dataloader.process.DataLoaderRunner ${=batchmodeargs} ${=configdir} ${=encryptionargs} "$@"
