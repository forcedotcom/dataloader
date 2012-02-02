/*
 * Copyright (c) 2012, salesforce.com, inc.
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
package com.salesforce.dataloader;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.exception.PasswordExpiredException;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * This class represents the base class for all data loader JUnit tests. TODO: ProcessScheduler test? TODO: Encryption
 * test
 * 
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 8.0
 */
abstract public class TestBase extends TestCase {
    
    private static final String API_CLIENT_NAME = "DataLoaderBatch/" + Controller.APP_VERSION;

    private static final String TEST_FILES_DIR = "src/test/resources/testfiles";
    private static final String TEST_CONF_DIR = TEST_FILES_DIR + "/conf";
    private static final String TEST_DATA_DIR = TEST_FILES_DIR + "/data";
    private static final String TEST_STATUS_DIR = "target/status";

    protected static final String DEFAULT_ACCOUNT_EXT_ID_FIELD = "Oracle_Id__c";
    protected static final String DEFAULT_CONTACT_EXT_ID_FIELD = "NumberId__c";

    protected static final String ACCOUNT_NUMBER_PREFIX = "ACCT";
    protected static final String ACCOUNT_WHERE_CLAUSE = "AccountNumber__c like '" + ACCOUNT_NUMBER_PREFIX + "%'";
    protected static final String CONTACT_TITLE_PREFIX = "CONTTL";
    protected static final String CONTACT_WHERE_CLAUSE = "Title like '" + CONTACT_TITLE_PREFIX + "%'";
    protected static final int SAVE_RECORD_LIMIT = 200;

    // logger
    private static Logger logger = Logger.getLogger(TestBase.class);

    protected String baseName; // / base name of the test (without the "test")
    private Controller controller;
    private File dataloaderHome;
    String oldThreadName;
    PartnerConnection binding;

    protected TestBase(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    public void setUp() {
        // reset binding
        binding = null;

        // get the test name and lowercase the 1st letter of the name for file for readability
        String baseNameOrig = getName().substring(4);
        this.baseName = baseNameOrig.substring(0, 1).toLowerCase() + baseNameOrig.substring(1);

        // name the current thread.  useful for test logging
        try {
            this.oldThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName(getName());
        } catch (Exception e) {
            // ignore, just leave the default thread name intact
        }

        String dataloaderDirSysProp = null;
        if (System.getProperty("basedir") != null) {
            // the basedir propety is set by maven
            dataloaderDirSysProp = "basedir";
        } else {
            // we are running in eclipse 
            dataloaderDirSysProp = "user.dir";
        }
        
        String dataloaderDirProp = System.getProperty(dataloaderDirSysProp);
        
        if (dataloaderDirProp == null || dataloaderDirProp.length() == 0)
            throw new IllegalStateException("Property must be set: " + dataloaderDirSysProp);
        
        this.dataloaderHome = new File(dataloaderDirProp);
        if (!this.dataloaderHome.isDirectory())
            throw new IllegalStateException("Invalid dataloader home: " + this.dataloaderHome);
       
        // copy test config properties into target directory 
        String configFileName = "config.properties";
        String targetPath = dataloaderDirProp + File.separator +  "target";
        String configFilePath = getTestConfDir()  + File.separator + configFileName;  
        String dbConfigFileName = "database-conf.xml";
        String dbConfigFilePath = getTestConfDir() + File.separator + dbConfigFileName;
        try {
            FileUtils.copyFileToDirectory(new File(configFilePath), new File(targetPath), true);
            FileUtils.copyFileToDirectory(new File(dbConfigFilePath), new File(targetPath), true);
        } catch (IOException e) {
            fail(e);
        }
        
        // configure the Controller to point to our testing config
        System.setProperty(Config.LOADER_CONFIG_DIR_SYSPROP, targetPath);
        initController();
    }

    protected void initController() {
        if (controller == null) {
            try {
                controller = Controller.getInstance(getName(), true);
            } catch (ControllerInitializationException e) {
                fail("While initializing controller instance", e);
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        // restore original thread name
        if(this.oldThreadName != null && this.oldThreadName.length() > 0) {
            try {
                Thread.currentThread().setName(this.oldThreadName);
            } catch (Exception e) {
                // ignore, just leave the default thread name intact
            }
        }
    }

    protected Controller getController() {
        return controller;
    }

    /**
     * @return PartnerConnection - binding to use to call the salesforce API
     */
    protected PartnerConnection getBinding() {
        if(binding != null) {
            return binding;
        }
        ConnectorConfig bindingConfig = new ConnectorConfig();
        bindingConfig.setUsername(getController().getConfig().getString(Config.USERNAME));
        bindingConfig.setPassword(getController().getConfig().getString(Config.PASSWORD));
        String configEndpoint = getController().getConfig().getString(Config.ENDPOINT);
        if (!configEndpoint.equals("")) { //$NON-NLS-1$
            String serverPath;
            try {
                serverPath = new URI(Connector.END_POINT).getPath();
                bindingConfig.setAuthEndpoint(configEndpoint + serverPath);
                bindingConfig.setServiceEndpoint(configEndpoint + serverPath);
                bindingConfig.setManualLogin(true);
                // set long timeout for tests with larger data sets
                bindingConfig.setReadTimeout(5 * 60 * 1000);
                if (getController().getConfig().getBoolean(Config.DEBUG_MESSAGES)) {
                    bindingConfig.setTraceMessage(true);
                    bindingConfig.setPrettyPrintXml(true);
                    String filename = getController().getConfig().getString(Config.DEBUG_MESSAGES_FILE);
                    if (filename.length() > 0) {
                        try {
                            bindingConfig.setTraceFile(filename);
                        } catch (FileNotFoundException e) {
                            logger.warn(Messages.getFormattedString("Client.errorMsgDebugFilename", filename));
                        }
                    }
                }
            } catch (URISyntaxException e) {
                fail("Error parsing endpoint URL: " + Connector.END_POINT + ", error: " + e.getMessage());
            }
        }
        logger.info("Getting binding for URL: " + bindingConfig.getAuthEndpoint());
        binding = newConnection(bindingConfig, 0);
        return binding;
    }

    /**
     * @param bindingConfig
     * @return PartnerConnection
     * @throws ConnectionException
     */
    private PartnerConnection newConnection(ConnectorConfig bindingConfig, int retries) {
        try {
            PartnerConnection newBinding = Connector.newConnection(bindingConfig);

            newBinding.setCallOptions(API_CLIENT_NAME, null);

            logger.info("Logging in as " + bindingConfig.getUsername() + "/" + bindingConfig.getPassword() + " to URL: " + bindingConfig.getAuthEndpoint());
            if (bindingConfig.isManualLogin()) {
                LoginResult loginResult = newBinding.login(bindingConfig.getUsername(), bindingConfig.getPassword());
                // if password has expired, throw an exception
                if (loginResult.getPasswordExpired()) {
                    throw new PasswordExpiredException(Messages.getString("Client.errorExpiredPassword")); //$NON-NLS-1$
                }
                // update session id and service endpoint based on response
                newBinding.setSessionHeader(loginResult.getSessionId());
                bindingConfig.setServiceEndpoint(loginResult.getServerUrl());
            }
            return newBinding;
        } catch (ConnectionException e) {
            // in case of exception try to get a connection again
            if (retries < 3) {
                retries++;
                return newConnection(bindingConfig, retries);
            }
            fail("Error getting web service proxy binding", e);
        }
        // make eclipse happy
        return null;
    }

    protected File getResource(String path) {
        return new File(this.dataloaderHome, path);
    }
    
    protected String getResourcePath(String path) {
        return getResource(path).getAbsolutePath();
    }

    protected String getTestConfDir() {
        return getResourcePath(TEST_CONF_DIR);
    }

    protected String getTestFilesDir() {
        return getResourcePath(TEST_FILES_DIR);
    }

    protected String getTestDataDir() {
        return getResourcePath(TEST_DATA_DIR);
    }

    protected String getTestStatusDir() {
        return getResourcePath(TEST_STATUS_DIR);
    }

    /**
     * @param e
     */
    protected PartnerConnection checkBinding(int retries, ApiFault e) {
        logger.info("Retry#" + retries + " getting a binding after an error.  Code: " + e.getExceptionCode().toString()
                + ", detail: " + e.getExceptionMessage());
        if(retries < 3) { // && (e.getExceptionCode() == ExceptionCode.INVALID_SESSION_ID || e.getExceptionMessage().indexOf("Invalid Session ID") != -1)) {
            return getBinding();
        } else {
            return null;
        }
    }

    protected void fail(String m, Throwable t) {
        final StringWriter failMessage = new StringWriter();
        final PrintWriter pw = new PrintWriter(failMessage);
        pw.println(m);
        pw.print("Message: ");
        pw.println(t.getMessage());
        pw.println("Stack trace:");
        t.printStackTrace(pw);
        fail(String.valueOf(failMessage));
    }

    protected void fail(Throwable t) {
        final StringWriter stackTrace = new StringWriter();
        t.printStackTrace(new PrintWriter(stackTrace));
        fail("Unexpected exception of type " + t.getClass().getCanonicalName(), t);
    }

}
