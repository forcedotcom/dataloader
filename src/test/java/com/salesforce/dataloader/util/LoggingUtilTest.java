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
import org.apache.logging.log4j.Level; 
import org.apache.logging.log4j.LogManager; 
import org.apache.logging.log4j.Logger; 
import org.junit.Assert; import org.junit.Test;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.IOException; 
import java.util.Properties;
import java.io.FileOutputStream;

public class LoggingUtilTest {
    @Test
    public void testInitializeLog() throws IOException {
        // Setup
        File tempFile = File.createTempFile("log4j2", ".properties");
        tempFile.deleteOnExit();
        Properties props = new Properties();
        props.setProperty("rootLogger.level", "DEBUG");
        props.store(new FileOutputStream(tempFile), null);
        System.setProperty(LoggingUtil.SYS_PROP_LOG4J2_CONFIG_FILE, tempFile.getAbsolutePath());
    
        // Test
        LoggingUtil.initializeLog(null);
    
        // Verify
        Logger logger = LogManager.getLogger(LoggingUtil.class);
        Assert.assertNotNull(logger);
        Assert.assertEquals(Level.DEBUG, logger.getLevel());
    }
    
    @Test
    public void testSetLoggingLevel() {
        Logger logger = LogManager.getLogger(LoggingUtil.class);
        LoggingUtil.setLoggingLevel("INFO");
        Assert.assertEquals(Level.INFO, logger.getLevel());
    }
    
    @Test
    public void testSetLoggingLevelInPropertiesFile() throws IOException {
        // Setup
        File tempFile = File.createTempFile("log4j2", ".properties");
        tempFile.deleteOnExit();
        Properties props = new Properties();
        props.setProperty("rootLogger.level", "DEBUG");
        props.store(new FileOutputStream(tempFile), null);
        System.setProperty(LoggingUtil.SYS_PROP_LOG4J2_CONFIG_FILE, tempFile.getAbsolutePath());
    
        // Test
        LoggingUtil.setLoggingLevel("INFO");
    
        // Verify
        Properties updatedProps = new Properties();
        updatedProps.load(new FileInputStream(tempFile));
        Assert.assertEquals("INFO", updatedProps.getProperty("rootLogger.level"));
    }
    
    @Test
    public void testGetLoggingConfigFile() {
        System.setProperty(LoggingUtil.SYS_PROP_LOG4J2_CONFIG_FILE, "junk.properties");
        String configFile = LoggingUtil.getLoggingConfigFile();
        Assert.assertNotNull(configFile);
    }
    
    @Test
    public void testGetLoggingLevel() {
        LoggingUtil.setLoggingLevel("WARN");
        String level = LoggingUtil.getLoggingLevel();
        Assert.assertEquals("WARN", level);
    }
    
    @Test
    public void testGetLoggingFilePattern() {
        String pattern = LoggingUtil.getLoggingFilePattern();
        Assert.assertNotNull(pattern);
    }
    
    @Test
    public void testGetLatestLoggingFile() {
        String latestFile = LoggingUtil.getLatestLoggingFile();
        Assert.assertNotNull(latestFile);
    }
}