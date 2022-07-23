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
package com.salesforce.dataloader.controller;

import com.salesforce.dataloader.action.IAction;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.BulkClient;
import com.salesforce.dataloader.client.BulkV2Client;
import com.salesforce.dataloader.client.ClientBase;
import com.salesforce.dataloader.client.DescribeRefObject;
import com.salesforce.dataloader.client.HttpClientTransport;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DataAccessObject;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.salesforce.dataloader.mapping.Mapper;
import com.salesforce.dataloader.mapping.SOQLMapper;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.ui.LoaderWindow;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.LimitInfo;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;

/**
 * The class that controls dataloader engine (config, salesforce communication, mapping, dao). For
 * UI, this is the controller for all the underlying data access.
 *
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 6.0
 */
public class Controller {

    private static boolean isLogInitialized = false; // make sure log is initialized only once
    private static boolean areStaticVarsInitialized = false; // make sure log is initialized only once

    /**
     * the system property name used to determine the config directory
     */

    public static final String SYS_PROP_LOG4J2_CONFIG_FILE = "log4j2.configurationFile";
    public static final String LOG_CONF_DEFAULT = "log-conf.xml";

    private static String APP_NAME; //$NON-NLS-1$
    public static String APP_VERSION; //$NON-NLS-1$
    public static String API_VERSION = null;
    private static String APP_VENDOR; //$NON-NLS-1$
    
    /**
     * <code>config</code> is an instance of configuration that's tied to this instance of
     * controller in a multithreaded environment
     */
    private Config config;
    private Mapper mapper;

    private DataAccessObjectFactory daoFactory;
    private DataAccessObject dao;
    private BulkClient bulkClient;
    private BulkV2Client bulkV2Client;
    private PartnerClient partnerClient;
    private LoaderWindow loaderWindow;
    private boolean lastOperationSuccessful = true;

    // logger
    private static Logger logger;

    private Controller(String lastRunFilePrefix, Map<String, String> argMap) throws ControllerInitializationException {
        // if name is passed to controller, use it to create a unique run file name
        initializeLog(argMap);
        try {
            this.config = Config.getInstance(lastRunFilePrefix, argMap);
        } catch (Exception e) {
            logger.error("Exception happened in initConfig:", e);
            throw new ControllerInitializationException(e.getMessage());
        }

        if (daoFactory == null) {
            daoFactory = new DataAccessObjectFactory();
        }
        HttpClientTransport.setReuseConnection(config.getBoolean(Config.REUSE_CLIENT_CONNECTION));
    }
    
    public static Map<String, String> getArgMapFromArgArray(String[] argArray){
        Map<String, String> argMap = new HashMap<>();
        if (argArray != null) {
            //Process name=value config setting
            Arrays.stream(argArray).forEach(arg ->
            {
                String[] nameValuePair = arg.split("=");
                if (nameValuePair.length == 2)
                    argMap.put(nameValuePair[0], nameValuePair[1]);
            });
        }
        return argMap;
    }

    private static synchronized void initStaticVariable() throws ControllerInitializationException {
        if (areStaticVarsInitialized) {
            return;
        }
        Properties versionProps = new Properties();
        try {
            versionProps.load(Controller.class.getClassLoader().getResourceAsStream("com/salesforce/dataloader/version.properties"));
        } catch (IOException e) {
            throw new ControllerInitializationException(e);
        }
        APP_NAME = versionProps.getProperty("dataloader.name");
        APP_VENDOR = versionProps.getProperty("dataloader.vendor");
        // FIXME clean this up, make static
        // dataloader version has 3 parts, salesforce app api version should match first two parts
        APP_VERSION = versionProps.getProperty("dataloader.version");
        areStaticVarsInitialized = true;
    }

    public synchronized void executeAction(ILoaderProgress monitor) throws DataAccessObjectException, OperationException {
        OperationInfo operation = this.config.getOperationInfo();
        IAction action = operation.instantiateAction(this, monitor);
        logger.info(Messages.getFormattedString("Controller.executeStart", operation)); //$NON-NLS-1$
        logger.debug("API info for the operation:" + getAPIInfo());
        action.execute();
    }
    
    public String getAPIInfo() {
        if (this.partnerClient == null) {
            return null;
        }
        String apiInfoStr = "\n    " + Labels.getFormattedString("Operation.apiVersion", partnerClient.getAPIVersion());
        LimitInfo apiLimitInfo = this.partnerClient.getAPILimitInfo();
        if (apiLimitInfo != null) {
            apiInfoStr = "\n    "
                    + Labels.getFormattedString("Operation.currentAPIUsage", apiLimitInfo.getCurrent())
                    + "\n    "
                    + Labels.getFormattedString("Operation.apiLimit", apiLimitInfo.getLimit())
                    + apiInfoStr;
        }
        return apiInfoStr;
    }

    private void validateSession() {
        getPartnerClient().validateSession();
    }

    public void setFieldTypes() throws ConnectionException {
        validateSession();
        getPartnerClient().setFieldTypes();
    }

    public void setReferenceDescribes() throws ConnectionException {
        validateSession();
        getPartnerClient().setFieldReferenceDescribes();
    }

    private boolean loginIfSessionExists(ClientBase<?> clientToLogin) {
        if (!isLoggedIn()) return false;
        return clientToLogin.connect(getPartnerClient().getSession());
    }

    public boolean loginIfSessionExists() {
        return loginIfSessionExists(getClient());
    }

    public boolean setEntityDescribes() throws ConnectionException {
        validateSession();
        return getPartnerClient().setEntityDescribes();
    }
    
    public static void setAPIVersion(String apiVersionStr) {
        API_VERSION = apiVersionStr;
    }
    
    public static String getAPIVersion() {
        return API_VERSION;
    }

    public Map<String, DescribeGlobalSObjectResult> getEntityDescribes() {
        validateSession();
        return getPartnerClient().getDescribeGlobalResults();
    }

    public DescribeSObjectResult getFieldTypes() {
        validateSession();
        return getPartnerClient().getFieldTypes();
    }

    public Map<String, DescribeRefObject> getReferenceDescribes() {
        validateSession();
        return getPartnerClient().getReferenceDescribes();
    }

    public boolean login() throws ConnectionException {
        return login(getClient());
    }

    private boolean login(ClientBase<?> clientToLogin) throws ConnectionException {
        boolean loggedIn = isLoggedIn();
        if (!loggedIn) loggedIn = getPartnerClient().connect();
        return loggedIn && clientToLogin.connect(getPartnerClient().getSession());
    }

    public boolean isLoggedIn() {
        return getPartnerClient().isLoggedIn();
    }

    private void createDao(String daoTypeStr, String daoNameStr) throws DataAccessObjectInitializationException {
        config.setValue(Config.DAO_NAME, daoNameStr);
        config.setValue(Config.DAO_TYPE, daoTypeStr);
        this.saveConfig();

        try {
            config.getStringRequired(Config.DAO_NAME); // verify required param exists: dao name
            dao = daoFactory.getDaoInstance(config.getStringRequired(Config.DAO_TYPE), config);
            logger.info(Messages.getString("Process.checkingDao")); //$NON-NLS-1$
            dao.checkConnection();
        } catch (Exception e) {
            logger.fatal(Messages.getString("Controller.errorDAOCreate"), e); //$NON-NLS-1$
            throw new DataAccessObjectInitializationException(Messages.getString("Controller.errorDAOCreate"), e); //$NON-NLS-1$
        }
    }
    
    public void createMapper(String daoTypeStr, String daoNameStr, String sObjectName) throws MappingInitializationException {
        try {
            createDao(daoTypeStr, daoNameStr);
        } catch (DataAccessObjectInitializationException e) {
            throw new MappingInitializationException(e.getMessage());
        }
        config.setValue(Config.ENTITY, sObjectName);
        this.saveConfig();
        try {
            this.setFieldTypes();
            this.setReferenceDescribes();
        } catch (Exception e) {
            throw new MappingInitializationException(e);
        }
        String mappingFile = config.getString(Config.MAPPING_FILE);
        this.mapper = getConfig().getOperationInfo().isExtraction() ? new SOQLMapper(getPartnerClient(),
                dao.getColumnNames(), getFieldTypes().getFields(), mappingFile) : new LoadMapper(getPartnerClient(), dao.getColumnNames(),
                getFieldTypes().getFields(), mappingFile);

    }

    public void createAndShowGUI() throws ControllerInitializationException {
        // check config access for saving settings -- required in UI
        File configFile = new File(config.getFilename());
        if (!configFile.canWrite()) {
            String errMsg = Messages.getFormattedString("Controller.errorConfigWritable", config.getFilename());

            String currentWorkingDir = System.getProperty("user.dir");
            if (currentWorkingDir.startsWith("/Volumes")) {
                //user is trying to launch dataloader from the dmg. this is not supported
                errMsg = Messages.getString("Controller.errorConfigWritableDmg");
            }

            logger.fatal(errMsg);
            throw new ControllerInitializationException(errMsg);
        }
        // start the loader UI
        this.loaderWindow = new LoaderWindow(this);
        this.loaderWindow.run();
        saveConfig();
    }
    
    public void updateLoaderWindowTitle() {
        if (isLoggedIn()) {
            try {
                ConnectorConfig sessionConfig = getPartnerClient().getClient().getConfig();
                URL sessionURL = new URL(sessionConfig.getServiceEndpoint());
                String sessionHost = sessionURL.getHost();
                this.loaderWindow.updateTitle(sessionHost);
                return;
            } catch (MalformedURLException e) {
                logger.error(e.getMessage());
            }
        }
        this.loaderWindow.updateTitle(null);
    }

    public static synchronized Controller getInstance(String lastRunFilePrefix, String[] args) throws ControllerInitializationException {
        return getInstance(lastRunFilePrefix, getArgMapFromArgArray(args));
    }

    public static synchronized Controller getInstance(String lastRunFilePrefix, Map<String, String> argMap) throws ControllerInitializationException {
        initializeLog(argMap);
        return new Controller(lastRunFilePrefix, argMap);
    }
    
    public synchronized boolean saveConfig() {
        try {
            config.save();
        } catch (IOException e) {
            logger.fatal(Messages.getFormattedString("Controller.errorConfigSave", config.getFilename()), e); //$NON-NLS-1$
            return false;
        } catch (GeneralSecurityException e) {
            logger.fatal(Messages.getFormattedString("Controller.errorConfigSave", config.getFilename()), e); //$NON-NLS-1$
            return false;
        }
        return true;

    }

    /**
     * @return product name
     */
    private static String getProductName() {
        return APP_NAME + " " + APP_VERSION;
    }

    private static synchronized void _doInitializeLog() throws FactoryConfigurationError {
        // check the environment variable for log4j
        String log4jConfigFilePath = System.getenv("LOG4J_CONFIGURATION_FILE");
        if (log4jConfigFilePath == null || log4jConfigFilePath.isEmpty()) {
            // check the system property for log4j2
            log4jConfigFilePath = System.getProperty(SYS_PROP_LOG4J2_CONFIG_FILE);
        }
        
        if (log4jConfigFilePath == null || log4jConfigFilePath.isEmpty()) { // use the default
            log4jConfigFilePath = Paths.get(AppUtil.getConfigurationsDir(), LOG_CONF_DEFAULT).toString();
        }
       
        Path p = Paths.get(log4jConfigFilePath);
        File logConfFile;
        if (p.isAbsolute()) {
            logConfFile = Paths.get(log4jConfigFilePath).toFile();
        } else {
            logConfFile = Paths.get(System.getProperty("user.dir"), log4jConfigFilePath).toFile();
        }

        String log4jConfigFileAbsolutePath =  logConfFile.getAbsolutePath();
        if (logConfFile.exists()) {
            System.setProperty(SYS_PROP_LOG4J2_CONFIG_FILE, log4jConfigFileAbsolutePath);
        } else { // extract log-conf.xml from the jar file
            AppUtil.extractFromJar("/" + LOG_CONF_DEFAULT, logConfFile);
        }
        
        // Uncomment code block to check that logger is using the config file
        /*
         * 

        logger = LogManager.getLogger(Controller.class);

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
        logger = LogManager.getLogger(Controller.class);

        logger.info(Messages.getString("Controller.logInit")); //$NON-NLS-1$
    }
    
    public synchronized static void initializeLog(Map<String, String> argMap) {
        if (Controller.isLogInitialized) {
            return;
        }
        try {
            initStaticVariable();
        } catch (ControllerInitializationException ex) {
            System.out.println("Controller.initializeLog(): Unable to initialize Controller static vars: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        // Need to initialize the configurations directory before initializing logger
        // because the default log-conf.xml resides in the configuration directory.
        AppUtil.setConfigurationsDir(argMap);

        // initialize logger
        try {
            _doInitializeLog();
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
        } finally {
            Controller.isLogInitialized = true;
        }
    }
    
    public PartnerClient getPartnerClient() {
        if (this.partnerClient == null) this.partnerClient = new PartnerClient(this);
        return this.partnerClient;
    }

    private ClientBase<?> getClient() {
        return this.config.useBulkAPIForCurrentOperation() ? getBulkClient() : getPartnerClient();
    }

    public BulkClient getBulkClient() {
        if (this.bulkClient == null) {
            this.bulkClient = new BulkClient(this);
            loginIfSessionExists(this.bulkClient);
        }
        return this.bulkClient;
    }
    
    public BulkV2Client getBulkV2Client() {
        if (this.bulkV2Client == null) {
            this.bulkV2Client = new BulkV2Client(this);
            loginIfSessionExists(this.bulkV2Client);
        }
        return this.bulkV2Client;
    }
    
    /**
     * @return Instance of configuration
     */
    public Config getConfig() {
        return config;
    }

    public DataAccessObject getDao() {
        return dao;
    }

    public void setLoaderConfig(Config config_) {
        config = config_;
    }

    public Mapper getMapper() {
        return this.mapper;
    }

    public void setStatusFiles(String statusDirName, boolean createDir, boolean generateFiles)
            throws ProcessInitializationException {
        File statusDir = new File(statusDirName);
        // if status directory unspecified, create one based on config path
        if (statusDirName == null || statusDirName.length() == 0) {
            statusDir = new File(new File(AppUtil.getConfigurationsDir()), "../status");
            statusDirName = statusDir.getAbsolutePath();
        }
        // it's an error if directory files exists but not a directory
        // or if directory doesn't exist and cannot be created (determined by caller)
        if (statusDir.exists() && !statusDir.isDirectory()) {
            throw new ProcessInitializationException(Messages.getFormattedString("Controller.invalidOutputDir",
                    statusDirName));
        } else if (!statusDir.exists()) {
            if (!createDir) {
                throw new ProcessInitializationException(Messages.getFormattedString("Controller.invalidOutputDir",
                        statusDirName));
            } else {
                if (!statusDir.mkdirs()) {
                    throw new ProcessInitializationException(Messages.getFormattedString(
                            "Config.errorCreatingOutputDir", statusDirName));
                }
            }
        }
        // if status files are not specified, generate the files automatically
        String successPath = config.getString(Config.OUTPUT_SUCCESS);
        if (generateFiles || successPath == null || successPath.length() == 0) {
            successPath = new File(statusDir, "success" + getFormattedCurrentTimestamp() + ".csv").getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String errorPath = config.getString(Config.OUTPUT_ERROR);
        if (generateFiles || errorPath == null || errorPath.length() == 0) {
            errorPath = new File(statusDir, "error" + getFormattedCurrentTimestamp() + ".csv").getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        /*
         * TODO: Bulk V2 has the endpoint to download unprocessed records from the submitted
         * job. Uncomment the following lines to download them.
        String unprocessedRecordsPath = config.getString(Config.OUTPUT_UNPROCESSED_RECORDS);
        if (generateFiles || unprocessedRecordsPath == null || unprocessedRecordsPath.length() == 0) {
        	unprocessedRecordsPath = new File(statusDir, "unprocessedRecords" + timestamp + ".csv").getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        */

        // next validate the error and success csv
        try {
            validateFile(successPath);
            validateFile(errorPath);
            // TODO for unprocessed records
            // validateFile(unprocessedRecordsPath);
        } catch (IOException e) {
            throw new ProcessInitializationException(e.getMessage(), e);
        }

        config.setValue(Config.OUTPUT_STATUS_DIR, statusDirName);
        config.setValue(Config.OUTPUT_SUCCESS, successPath);
        config.setValue(Config.OUTPUT_ERROR, errorPath);
        // TODO for unprocessed records
        // config.setValue(Config.OUTPUT_UNPROCESSED_RECORDS, unprocessedRecordsPath);
    }
    
    private final Date currentTime = new Date();
    private final SimpleDateFormat format = new SimpleDateFormat("MMddyyhhmmssSSS"); //$NON-NLS-1$
    private final String formattedCurrentTimestamp = format.format(currentTime);
    
    public String getFormattedCurrentTimestamp() {
        return formattedCurrentTimestamp;
    }

    private void validateFile(String filePath) throws IOException {
        File file = new File(filePath);
        // finally make sure the output isn't the data access file
        String daoName = config.getString(Config.DAO_NAME);

        // if it doesn't exist and we can create it, its valid
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException(Messages.getMessage(getClass(),
                            "errorFileCreate", filePath)); //$NON-NLS-1$
                }
            } catch (IOException iox) {
                throw new IOException(Messages.getMessage(getClass(), "errorFileCreate", filePath));
            }
        } else if (!file.canWrite())
            // if it does exist and cannot be written to
            throw new IOException(Messages.getMessage(getClass(), "errorFileWrite") + filePath);
        else if (filePath.equals(daoName))
            throw new IOException(Messages.getMessage(getClass(), "errorSameFile", daoName, filePath));
    }

    public void logout() {
        if (this.partnerClient != null) this.partnerClient.logout();
        this.bulkClient = null;
        this.partnerClient = null;
    }

    public boolean attachmentsEnabled() {
        return !getConfig().useBulkAPIForCurrentOperation() || getConfig().getBoolean(Config.BULK_API_ZIP_CONTENT);
    }

    public void clearMapper() {
        if (this.dao != null) {
            this.dao.close();
            this.dao = null;
        }
        this.mapper = null;
    }
    
    public void setLastOperationSuccessful(boolean successful) {
        this.lastOperationSuccessful = successful;
    }
    
    public boolean isLastOperationSuccessful() {
        return this.lastOperationSuccessful;
    }
}
