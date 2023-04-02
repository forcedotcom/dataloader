/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dataloader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.salesforce.dataloader.config.LinkedProperties;
import com.salesforce.dataloader.config.Messages;

public class LoggingUtil {
    
    public static final String SYS_PROP_LOG4J2_CONFIG_FILE = "log4j2.configurationFile";
    private static final String LOG_CONF_DEFAULT_XML = "log-conf.xml";
    private static final String LOG_CONF_DEFAULT_PROPERTIES = "log4j2.properties";
    public static String LOG_CONF_DEFAULT = LOG_CONF_DEFAULT_PROPERTIES;
    private static Logger logger;
    
    
    public static synchronized void initializeLog(Map<String, String> argsMap) throws FactoryConfigurationError, IOException {
        // check the environment variable for log4j
        String log4jConfigFilePath = System.getenv("LOG4J_CONFIGURATION_FILE");
        if (log4jConfigFilePath == null || log4jConfigFilePath.isEmpty()) {
            // check the system property for log4j2
            log4jConfigFilePath = System.getProperty(SYS_PROP_LOG4J2_CONFIG_FILE);
        }
        
        if (log4jConfigFilePath == null || log4jConfigFilePath.isEmpty()) { // use the default
            File logConfFile = new File(AppUtil.getConfigurationsDir() + "/" + LOG_CONF_DEFAULT_XML);
            if (logConfFile.exists()) {
                LOG_CONF_DEFAULT = LOG_CONF_DEFAULT_XML;
            } else {
                LOG_CONF_DEFAULT = LOG_CONF_DEFAULT_PROPERTIES;
            }
            log4jConfigFilePath = Paths.get(AppUtil.getConfigurationsDir(), LOG_CONF_DEFAULT).toString();
        }
       
        Path p = Paths.get(log4jConfigFilePath);
        File logConfFile;
        if (p.isAbsolute()) {
            logConfFile = Paths.get(log4jConfigFilePath).toFile();
        } else {
            logConfFile = Paths.get(System.getProperty("user.dir"), log4jConfigFilePath).toFile();
        }

        String log4jConfigFileAbsolutePath = logConfFile.getAbsolutePath();
        if (!logConfFile.exists()) {
            AppUtil.extractFromJar("/" + LOG_CONF_DEFAULT, logConfFile);
        }
        System.setProperty(SYS_PROP_LOG4J2_CONFIG_FILE, log4jConfigFileAbsolutePath);

        // Uncomment code block to check that logger is using the config file
        /*
         * 

        logger = new Logger(AppUtil.class);

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext();
        String logConfigLocation = loggerContext.getConfiguration().getConfigurationSource().getLocation();
        if (logConfigLocation == null) {
            logger.error("Unable to initialize logging using log4j2 config file at "
                    + log4jConfigFileAbsolutePath
                    + ". All error messages will be logged on STDOUT.");
        } else {
            logger.info("Using log4j2 configuration file at location: " + logConfigLocation);
        }
        */
        logger = LogManager.getLogger(AppUtil.class);
        logger.info(Messages.getString("AppUtil.logInit")); //$NON-NLS-1$
    }
    
    public static void setLoggingLevel(String newLevelStr) {
        if (newLevelStr == null) {
            return;
        }
        setLoggingLevelInPropertiesFile(newLevelStr);
        Level newLevel = Level.toLevel(newLevelStr);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
        loggerConfig.setLevel(newLevel);
        ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
    }
    
    private static void setLoggingLevelInPropertiesFile(String newLevelStr) {
        String log4j2ConfFile = System.getProperty(SYS_PROP_LOG4J2_CONFIG_FILE);
        if (newLevelStr == null || log4j2ConfFile == null || !log4j2ConfFile.endsWith(".properties")) {
            return;
        }
        Properties loggingProps = new LinkedProperties();
        try {
            loggingProps.load(new FileInputStream(log4j2ConfFile));
            loggingProps.put("rootLogger.level", newLevelStr);
            loggingProps.store(new FileOutputStream(log4j2ConfFile), "Logging config");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    
    public static String getLoggingConfigFile() {
        return System.getProperty(SYS_PROP_LOG4J2_CONFIG_FILE);
    }
    
    public static String getLoggingLevel() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
        return loggerConfig.getLevel().name();
    }
    
    public static String getLoggingFilePattern() {
        RollingFileAppender appender = (RollingFileAppender)getAppender(RollingFileAppender.class);
        if (appender == null) {
            return "";
        }
        return appender.getFilePattern();
    }

    public static String getLatestLoggingFile() {
        RollingFileAppender appender = (RollingFileAppender)getAppender(RollingFileAppender.class);
        if (appender == null) {
            return "";
        }
        return appender.getFileName();
    }
    
    private static Appender getAppender(Class<?> appenderClass) {
        LoggerContext logContext =  (LoggerContext) LogManager.getContext(false);

        Map<String, LoggerConfig> map = logContext.getConfiguration().getLoggers();
        LoggerConfig rootConfig = map.get("");
        Collection<Appender> appenders = rootConfig.getAppenders().values();
        for (Appender appender : appenders) {
            if (appender.getClass().isInstance(appenderClass)) {
                return appender;
            }
        }
        return null;
    }
}
