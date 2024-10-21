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
package com.salesforce.dataloader;

import com.salesforce.dataloader.client.BulkV1Client;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.PasswordExpiredException;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.FactoryConfigurationError;

/**
 * This class represents the base class for all data loader JUnit tests. TODO: ProcessScheduler test? TODO: Encryption
 * test
 * 
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 8.0
 */
abstract class TestBase {

    private static final Pattern INSIDE_BRACKETS_TEST_PARAMETERS = Pattern.compile("\\[.+\\]");
    @Rule
    public TestName testName = new TestName();
    
    /* *********
     * Start of the section declaring
     * static variables that need to be initialized after loading test properties
     * *********
     */
    private static final Properties TEST_PROPS;
    private static final String TEST_FILES_DIR;
    protected static final String TEST_CONF_DIR;
    private static final String TEST_DATA_DIR;
    private static final String TEST_STATUS_DIR;

    protected static final String DEFAULT_ACCOUNT_EXT_ID_FIELD;

    // logger
    private static Logger logger;
    /* *********
     * End of the section declaring
     * static variables that need to be initialized after loading test properties
     * *********
     */
    
    static {
        // initialize the static variables that are dependent on test properties.
        TEST_PROPS = loadTestProperties();
        TEST_FILES_DIR = getProperty("testfiles.dir");
        TEST_CONF_DIR = TEST_FILES_DIR + File.separator + "conf";
        TEST_PROPS.put(AppConfig.CLI_OPTION_CONFIG_DIR_PROP, TEST_CONF_DIR);
        TEST_DATA_DIR = TEST_FILES_DIR + File.separator + "data";
        TEST_STATUS_DIR = TEST_FILES_DIR + File.separator + "status";
        DEFAULT_ACCOUNT_EXT_ID_FIELD = getProperty("test.account.extid");
        
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put(AppConfig.CLI_OPTION_CONFIG_DIR_PROP, getTestConfDir());
        try {
            AppUtil.initializeAppConfig(AppUtil.convertCommandArgsMapToArgsArray(argsMap));
        } catch (FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger = LogManager.getLogger(TestBase.class);
    }

    private static Properties loadTestProperties() {
        final Properties p = new Properties();
        final URL url = TestBase.class.getClassLoader().getResource("test.properties");
        if (url == null) throw new IllegalStateException("Failed to locate test.properties.  Is it in the classpath?");
        try {
            final InputStream propStream = url.openStream();
            try {
                p.load(propStream);
            } finally {
                propStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test properties from resource: " + url, e);
        }
        return p;
    }

    protected static String getProperty(String testProperty) {
        return TEST_PROPS.getProperty(testProperty);
    }

    private static final String API_CLIENT_NAME = "DataLoaderTestBatch/" + Controller.APP_VERSION;

    protected static final String DEFAULT_CONTACT_EXT_ID_FIELD = "NumberId__c";

    protected static final String ACCOUNT_NUMBER_PREFIX = "ACCT";
    protected static final String ACCOUNT_WHERE_CLAUSE = "AccountNumber__c like '" + ACCOUNT_NUMBER_PREFIX + "%'";
    protected static final String CONTACT_TITLE_PREFIX = "CONTTL";
    protected static final String CONTACT_WHERE_CLAUSE = "Title like '" + CONTACT_TITLE_PREFIX + "%'";
    protected static final String TESTFIELD_FIELD_PREFIX = "testfield__";
    protected static final String TESTFIELD_WHERE_CLAUSE = "TestField__c like '" + TESTFIELD_FIELD_PREFIX + "%'";
    protected static final int SAVE_RECORD_LIMIT = 200;

    protected String baseName; // / base name of the test (without the "test")
    private Controller controller;
    String oldThreadName;
    PartnerConnectionForTest binding;
    
    @Before
    public void basicSetUp() throws Exception {
        File testStatusDir = new File(TEST_STATUS_DIR);
        if (!testStatusDir.exists()) testStatusDir.mkdirs();

        // reset binding
        if (this.binding != null) {
            this.binding.cleanup();
        }
        this.binding = null;
        setupTestName();
    }
    
    @After
    public void cleanup() {
        if (this.controller != null && this.binding != null) {
            this.binding.cleanup();
        }
    }

    private void setupTestName() {
        baseName = testName.getMethodName();
        if(baseName.startsWith("test")) {
            baseName = baseName.substring(4);
        }
        baseName = StringUtils.uncapitalize(baseName);
        baseName = INSIDE_BRACKETS_TEST_PARAMETERS.matcher(baseName).replaceAll("");

        // name the current thread. useful for test logging
        this.oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(testName.getMethodName());
    }

    protected void setupController(Map<String, String> configOverrideMap) {
        // configure the Controller to point to our testing config
        configOverrideMap.put(AppConfig.PROP_READ_ONLY_CONFIG_PROPERTIES, Boolean.TRUE.toString());
        if (!System.getProperties().contains(AppConfig.CLI_OPTION_CONFIG_DIR_PROP))
            System.setProperty(AppConfig.CLI_OPTION_CONFIG_DIR_PROP, getTestConfDir());

        try {
            controller = Controller.getInstance(configOverrideMap);
        } catch (Exception e) {
            fail("While initializing controller instance", e);
        }
    }

    @After
    public void resetThreadName() throws Exception {
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

    String apiVersionForTheSession = null;
    /**
     * @return PartnerConnection - binding to use to call the salesforce API
     */
    protected PartnerConnectionForTest getBinding() {
        if(binding != null) {
            return binding;
        }
        ConnectorConfig bindingConfig;
        
        if (this.apiVersionForTheSession == null) {
            String apiVersionToTry = PartnerClient.getCurrentAPIVersionInWSC();
            bindingConfig = getWSCConfig(apiVersionToTry);
            logger.info("Getting binding for URL: " + bindingConfig.getAuthEndpoint());
            binding = newConnection(bindingConfig, 0, 0);
            if (binding == null) {
                logger.error("Failed to invoke server APIs of version " + apiVersionToTry);
                apiVersionToTry = PartnerClient.getPreviousAPIVersionInWSC();
                bindingConfig = getWSCConfig(apiVersionToTry);
                logger.info("Getting binding for URL: " + bindingConfig.getAuthEndpoint());
                binding = newConnection(bindingConfig, 0, 0);
                if (binding == null) {
                    fail("Error logging in and getting a service binding for API version " + apiVersionToTry, new Exception());
                }
                this.apiVersionForTheSession = apiVersionToTry;
            }
        } else {
            bindingConfig = getWSCConfig(this.apiVersionForTheSession);
            logger.info("Getting binding for URL: " + bindingConfig.getAuthEndpoint());
            binding = newConnection(bindingConfig, 0, 3);
            if (binding == null) {
                fail("Error logging in and getting a service binding for API version " + this.apiVersionForTheSession, new Exception());
            }
        }
        return binding;
    }

    protected ConnectorConfig getWSCConfig(String apiVersionStr) {
        ConnectorConfig bindingConfig = new ConnectorConfig();
        bindingConfig.setUsername(getController().getAppConfig().getString(AppConfig.PROP_USERNAME));
        bindingConfig.setPassword(getController().getAppConfig().getString(AppConfig.PROP_PASSWORD));
        String configEndpoint = getController().getAppConfig().getAuthEndpointForCurrentEnv();
        if (!configEndpoint.equals("")) { //$NON-NLS-1$
            try {
                PartnerClient.setAPIVersionForTheSession(apiVersionStr);
                bindingConfig.setAuthEndpoint(configEndpoint + PartnerClient.getServicePath());
                bindingConfig.setServiceEndpoint(configEndpoint + PartnerClient.getServicePath()); // Partner SOAP service
                bindingConfig.setRestEndpoint(configEndpoint + BulkV1Client.getServicePath());  // REST service: Bulk v1       
                bindingConfig.setManualLogin(true);
                // set long timeout for tests with larger data sets
                bindingConfig.setReadTimeout(5 * 60 * 1000);
                if (getController().getAppConfig().getBoolean(AppConfig.PROP_DEBUG_MESSAGES)) {
                    bindingConfig.setTraceMessage(true);
                    bindingConfig.setPrettyPrintXml(true);
                    String filename = getController().getAppConfig().getString(AppConfig.PROP_DEBUG_MESSAGES_FILE);
                    if (!filename.isEmpty()) {
                        try {
                            bindingConfig.setTraceFile(filename);
                        } catch (FileNotFoundException e) {
                            logger.warn(Messages.getFormattedString("Client.errorMsgDebugFilename", filename));
                        }
                    }
                }
            } catch (Exception e) {
                Assert.fail("Error parsing endpoint URL: " + Connector.END_POINT + ", error: " + e.getMessage());
            }
        }
        return bindingConfig;
    }

    /**
     * @param bindingConfig
     * @return PartnerConnection
     * @throws com.sforce.ws.ConnectionException
     */
    private PartnerConnectionForTest newConnection(ConnectorConfig bindingConfig, int retries, int maxRetries) {
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
            return new PartnerConnectionForTest(newBinding);
        } catch (ConnectionException e) {
            // in case of exception try to get a connection again
            if (retries < maxRetries) {
                retries++;
                return newConnection(bindingConfig, retries, maxRetries);
            }
        }
        // make eclipse happy
        return null;
    }

    protected File getTestFile(String path) {
        return new File(TEST_FILES_DIR, path);
    }

    protected String getResourcePath(String path) {
        return getTestFile(path).getAbsolutePath();
    }

    protected static String getTestConfDir() {
        return TEST_CONF_DIR;
    }

    protected String getTestFilesDir() {
        return TEST_FILES_DIR;
    }

    protected String getTestDataDir() {
        return TEST_DATA_DIR;
    }

    protected String getTestStatusDir() {
        return TEST_STATUS_DIR;
    }

    /**
     * @param e
     */
    protected PartnerConnectionForTest checkBinding(int retries, ApiFault e) {
        logger.info("Retry#" + retries + " getting a binding after an error.  Code: " + e.getExceptionCode().toString()
                + ", detail: " + e.getExceptionMessage());
        if (retries < 3) // && (e.getExceptionCode() == ExceptionCode.INVALID_SESSION_ID ||
                         // e.getExceptionMessage().indexOf("Invalid Session ID") != -1)) {
            return getBinding();
        return null;
    }

    protected void fail(String m, Throwable t) {
        final StringWriter failMessage = new StringWriter();
        final PrintWriter pw = new PrintWriter(failMessage);
        pw.println(m);
        pw.print("Message: ");
        pw.println(t.getMessage());
        pw.println("Stack trace:");
        t.printStackTrace(pw);
        Assert.fail(String.valueOf(failMessage));
    }

    protected void fail(Throwable t) {
        final StringWriter stackTrace = new StringWriter();
        t.printStackTrace(new PrintWriter(stackTrace));
        fail("Unexpected exception of type " + t.getClass().getCanonicalName(), t);
    }
}
