#!/bin/sh 
# script parameters
#

run_mvn() {
  # run a mvn build to download dependencies from the central maven repo
  mvn clean compile
  # remove vulnerable class from log4j jar file
  log4j_version_num=$(awk '/<artifactId>log4j-core/{getline; print}' pom.xml | awk '{$1=$1};1' )
  log4j_version_num=${log4j_version_num#<version>};
  log4j_version_num=${log4j_version_num%</version>};
  
  echo "removing JndiLookup.class from ${log4j_version_num}"
  zip -q -d "${HOME}/.m2/repository/org/apache/logging/log4j/log4j-core/${log4j_version_num}/log4j-core-${log4j_version_num}.jar org/apache/logging/log4j/core/lookup/JndiLookup.class"
  zip -q -d "${HOME}/.m2/repository/org/apache/logging/log4j/log4j-core/${log4j_version_num}/log4j-core-${log4j_version_num}.jar org/apache/logging/log4j/core/appender/mom/JmsAppender.class"
  zip -q -d "${HOME}/.m2/repository/org/apache/logging/log4j/log4j-core/${log4j_version_num}/log4j-core-${log4j_version_num}.jar org/apache/logging/log4j/core/appender/db/jdbc/JdbcAppender.class"

  # build uber jar
  mvn clean package
}

#################
run_mvn