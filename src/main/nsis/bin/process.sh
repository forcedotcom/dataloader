#/bin/bash
# change to directory of this script
cd "$( dirname "${BASH_SOURCE[0]}" )"

function error {
    echo "$1" >&2
}

function usage {
    error "
    Usage: $0 <configuration directory> [process name]

         configuration directory -- directory that contains configuration files,
             i.e. config.properties, process-conf.xml, database-conf.xml

         process name -- optional name of a batch process bean in process-conf.xml,
             for example:

                 process ../myconfigdir AccountInsert

             If process name is not specified, the parameter values from config.properties
             will be used to run the process instead of process-conf.xml,
             for example:

                 process ../myconfigdir
    "
    exit 1
}

if [ "$#" -eq 1 ]; then
    PROCESS_OPTION=""
elif [ "$#" -eq 2 ]; then
    PROCESS_OPTION="process.name=${2}"
else
    usage
fi

JAVA=$(which java)

if [ "$JAVA" == "" ]; then
    error "To run $0, the Java Runtime Environment (JRE) must be installed."
    exit 1
else
    "${JAVA}" -cp "../${pom.build.finalName}-uber.jar" -Dsalesforce.config.dir="${1}" com.salesforce.dataloader.process.ProcessRunner "${PROCESS_OPTION}"
fi

exit 0
