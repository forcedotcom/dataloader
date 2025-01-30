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
import com.salesforce.dataloader.client.BulkV1Client;
import com.salesforce.dataloader.client.BulkV2Client;
import com.salesforce.dataloader.client.ClientBase;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.client.CompositeRESTClient;
import com.salesforce.dataloader.client.ReferenceEntitiesDescribeMap;
import com.salesforce.dataloader.client.transport.HttpTransportImpl;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DataAccessObjectInterface;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.exception.ConfigInitializationException;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.exception.ParameterLoadException;
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
import com.salesforce.dataloader.util.DLLogManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class that controls dataloader engine (config, salesforce communication, mapping, dao). For
 * UI, this is the controller for all the underlying data access.
 *
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 6.0
 */
public class Controller {

    /**
     * the system property name used to determine the config folder
     */

    public static String APP_VERSION = ""; //$NON-NLS-1$
    public static String API_VERSION = "";
    
    /**
     * <code>config</code> is an instance of configuration that's tied to this instance of
     * controller in a multithreaded environment
     */
    private AppConfig appConfig;
    private Mapper mapper;

    private DataAccessObjectFactory daoFactory;
    private DataAccessObjectInterface dao;
    private BulkV1Client bulkV1Client;
    private BulkV2Client bulkV2Client;
    private PartnerClient partnerClient;
    private CompositeRESTClient restClient;
    private LoaderWindow loaderWindow;
    private boolean lastOperationSuccessful = true;

    // logger
    private static Logger logger = DLLogManager.getLogger(Controller.class);
    
    private IAction lastExecutedAction = null;
    
    static {
        Properties versionProps = new Properties();
        try {
            versionProps.load(Controller.class.getClassLoader().getResourceAsStream("com/salesforce/dataloader/version.properties"));
            APP_VERSION = versionProps.getProperty("dataloader.version");
        } catch (IOException e) {
            logger.error("Unable to read version.properties file from uber jar");
        }
    }

    private Controller(Map<String, String> argsMap) throws ControllerInitializationException {
        // if name is passed to controller, use it to create a unique run file name
        try {
            this.appConfig = AppConfig.getInstance(argsMap);
        } catch (Exception e) {
            logger.fatal("Controller: Exception happened in initializing AppConfig:", e);
            throw new ControllerInitializationException(e.getMessage());
        }

        if (daoFactory == null) {
            daoFactory = new DataAccessObjectFactory();
        }
        if (AppUtil.getAppRunMode() != AppUtil.APP_RUN_MODE.INSTALL) {
            getLatestDownloadableDataLoaderVersion();
        }
    }
    
    private static String latestDownloadableDataLoaderVersion = null;
    private static final String DL_DOWNLOADABLE_REGEX = "[0-9]+\\.[0-9]+\\.[0-9]+\\.zip";
    public synchronized String getLatestDownloadableDataLoaderVersion() {
        if (latestDownloadableDataLoaderVersion != null) {
            return latestDownloadableDataLoaderVersion;
        }
        try {
            ConnectorConfig connConfig = new ConnectorConfig();
            AppUtil.setConnectorConfigProxySettings(appConfig, connConfig);
            HttpTransportImpl clientTransport = HttpTransportImpl.getInstance();
            clientTransport.setConfig(connConfig);
            InputStream inputStream = clientTransport.httpGet(AppUtil.DATALOADER_DOWNLOAD_URL);

            String responseContent = new String(inputStream.readAllBytes());            
            Pattern htmlTagInRichTextPattern = Pattern.compile(DL_DOWNLOADABLE_REGEX);
            Matcher matcher = htmlTagInRichTextPattern.matcher(responseContent);
            String downloadableVersion = AppUtil.DATALOADER_VERSION;
            if (matcher.find()) {
                downloadableVersion = matcher.group();
                downloadableVersion = downloadableVersion.substring(0, downloadableVersion.lastIndexOf("."));
            }
            latestDownloadableDataLoaderVersion = downloadableVersion;
            inputStream.close();
            return downloadableVersion;
        } catch (Exception e) {
            logger.info("Unable to check for the latest available data loader version: " + e.getMessage());
            latestDownloadableDataLoaderVersion = AppUtil.DATALOADER_VERSION;
            return AppUtil.DATALOADER_VERSION;
        }
    }
    
    public synchronized void executeAction(ILoaderProgress monitor) throws DataAccessObjectException, OperationException {
        OperationInfo operation = this.appConfig.getOperationInfo();
        IAction action = operation.instantiateAction(this, monitor);
        logger.info(Messages.getFormattedString("Controller.executeStart", operation)); //$NON-NLS-1$
        logger.debug("API info for the operation:" + getAPIInfo());
        action.execute();
        this.getClient().getSession().performedSessionActivity(); // reset session activity timer
        this.lastExecutedAction = action;
    }
    
    public IAction getLastExecutedAction() {
        return this.lastExecutedAction;
    }
    
    public String getAPIInfo() {
        if (this.partnerClient == null) {
            return null;
        }
        String apiTypeStr = "SOAP API";
        if (appConfig.isBulkAPIEnabled()) {
            apiTypeStr = "Bulk API";
        }
        if (appConfig.isBulkV2APIEnabled()) {
            apiTypeStr = "Bulk API 2.0";
        }
        String[] args =  {apiTypeStr, PartnerClient.getAPIVersionForTheSession()};
        String apiInfoStr = Labels.getFormattedString("Operation.apiVersion", args);
        LimitInfo apiLimitInfo = this.partnerClient.getAPILimitInfo();
        if (apiLimitInfo != null) {
            apiInfoStr = Labels.getFormattedString("Operation.currentAPIUsage", apiLimitInfo.getCurrent())
                    + "\n"
                    + Labels.getFormattedString("Operation.apiLimit", apiLimitInfo.getLimit())
                    + "\n"
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
    
    public void setReferenceDescribes(Collection<String> sfFields) throws ConnectionException {
        validateSession();
        getPartnerClient().setFieldReferenceDescribes(sfFields);
    }

    private boolean connectIfSessionExists(ClientBase<?> clientToLogin) {
        if (!isLoggedIn()) return false;
        return clientToLogin.connect(getPartnerClient().getSession());
    }
    
    public static String getAPIVersion() {
        return ClientBase.getAPIVersionForTheSession();
    }
    
    public static int getAPIMajorVersion() {
        String apiFullVersion = Controller.getAPIVersion();
        int apiMajorVersion = 0;
        if (apiFullVersion != null) {
            String[] apiVersionParts = apiFullVersion.split("\\.");
            apiMajorVersion = Integer.parseInt(apiVersionParts[0]);
        }
        return apiMajorVersion;
    }

    public Map<String, DescribeGlobalSObjectResult> getEntityDescribes() {
        validateSession();
        return getPartnerClient().getDescribeGlobalResults();
    }

    public DescribeSObjectResult getFieldTypes() {
        validateSession();
        return getPartnerClient().getFieldTypes();
    }

    public ReferenceEntitiesDescribeMap getReferenceDescribes() {
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
        appConfig.setValue(AppConfig.PROP_DAO_NAME, daoNameStr);
        appConfig.setValue(AppConfig.PROP_DAO_TYPE, daoTypeStr);
        try {
            appConfig.getStringRequired(AppConfig.PROP_DAO_NAME); // verify required param exists: dao name
            dao = daoFactory.getDaoInstance(appConfig.getStringRequired(AppConfig.PROP_DAO_TYPE), appConfig);
            logger.info(Messages.getString("Process.checkingDao")); //$NON-NLS-1$
            dao.checkConnection();
        } catch (Exception e) {
            logger.fatal(Messages.getString("Controller.errorDAOCreate"), e); //$NON-NLS-1$
            throw new DataAccessObjectInitializationException(Messages.getString("Controller.errorDAOCreate"), e); //$NON-NLS-1$
        }
    }
    
    public void initializeOperation(String daoTypeStr, String daoNameStr, String sObjectName) throws MappingInitializationException {
        try {
            createDao(daoTypeStr, daoNameStr);
        } catch (DataAccessObjectInitializationException e) {
            throw new MappingInitializationException(e.getMessage());
        }
        appConfig.setValue(AppConfig.PROP_ENTITY, sObjectName);
        initializeMapping(); // initialize mapping before setting reference describes
        try {
            this.setFieldTypes();
            this.setReferenceDescribes();
        } catch (Exception e) {
            throw new MappingInitializationException(e);
        }
    }
    
    private void initializeMapping() throws MappingInitializationException {
        String mappingFile = appConfig.getString(AppConfig.PROP_MAPPING_FILE);
        if (mappingFile != null 
                && !mappingFile.isBlank() && !Files.exists(Path.of(mappingFile))) {
            throw new MappingInitializationException("Mapping file " + mappingFile + " does not exist");
        }
        if (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.UI) {
            mappingFile = null;  // Do not use mapping file value set in config.properties in the interactive (UI) mode
        }
        // Initialize mapping
        this.mapper = getAppConfig().getOperationInfo().isExtraction() ? 
                new SOQLMapper(getPartnerClient(), dao.getColumnNames(), getFieldTypes().getFields(), mappingFile) 
              : new LoadMapper(getPartnerClient(), dao.getColumnNames(), getFieldTypes().getFields(), mappingFile);

    }

    public void createAndShowGUI() throws ControllerInitializationException {
        // check config access for saving settings -- required in UI
        File configFile = new File(appConfig.getFilename());
        if (!configFile.canWrite()) {
            String errMsg = Messages.getFormattedString("Controller.errorConfigWritable", appConfig.getFilename());

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
        
    public void updateLoaderWindowTitleAndCacheUserInfoForTheSession() {
        if (isLoggedIn()) {
            try {
                ConnectorConfig sessionConfig = getPartnerClient().getConnection().getConfig();
                URL sessionURL = new URL(sessionConfig.getServiceEndpoint());
                String sessionHost = sessionURL.getHost();
                this.loaderWindow.updateTitle(sessionHost);
                return;
            } catch (MalformedURLException e) {
                logger.error(e.getMessage());
            }
        } else {
            this.loaderWindow.updateTitle(null);
        }
    }
    
    public LoaderWindow getLoaderWindow() {
        return this.loaderWindow;
    }

    public static synchronized Controller getInstance(Map<String, String> argsMap) throws ControllerInitializationException, ParameterLoadException, ConfigInitializationException {
        return new Controller(argsMap);
    }
    
    public synchronized boolean saveConfig() {
        try {
            appConfig.save();
        } catch (IOException e) {
            logger.fatal(Messages.getFormattedString("Controller.errorConfigSave", appConfig.getFilename()), e); //$NON-NLS-1$
            return false;
        } catch (GeneralSecurityException e) {
            logger.fatal(Messages.getFormattedString("Controller.errorConfigSave", appConfig.getFilename()), e); //$NON-NLS-1$
            return false;
        }
        return true;

    }
    
    public PartnerClient getPartnerClient() {
        if (this.partnerClient == null) this.partnerClient = new PartnerClient(this);
        return this.partnerClient;
    }

    public ClientBase<?> getClient() {
        if (this.appConfig.useBulkAPIForCurrentOperation()) {
            if (this.appConfig.isBulkV2APIEnabled()) {
                return getBulkV2Client();
            } else {
                return getBulkV1Client();
            }
        } else if (this.appConfig.isRESTAPIEnabled()) {
            return getRESTClient();
        }
        return getPartnerClient();
    }

    public BulkV1Client getBulkV1Client() {
        if (this.bulkV1Client == null) {
            this.bulkV1Client = new BulkV1Client(this);
            connectIfSessionExists(this.bulkV1Client);
        }
        return this.bulkV1Client;
    }
    
    public BulkV2Client getBulkV2Client() {
        if (this.bulkV2Client == null) {
            this.bulkV2Client = new BulkV2Client(this);
            connectIfSessionExists(this.bulkV2Client);
        }
        return this.bulkV2Client;
    }
    
    public CompositeRESTClient getRESTClient() {
        if (this.restClient == null) {
            this.restClient = new CompositeRESTClient(this);
            connectIfSessionExists(this.restClient);
        }
        return this.restClient;
    }/**
     * @return Instance of configuration
     */
    public AppConfig getAppConfig() {
        return appConfig;
    }

    public DataAccessObjectInterface getDao() {
        return dao;
    }

    public Mapper getMapper() {
        return this.mapper;
    }

    public void setStatusFiles(String statusDirName, boolean createDir, boolean generateFiles)
            throws ProcessInitializationException {
        File statusDir = new File(statusDirName);
        // if status folder unspecified, create one based on config path
        if (statusDirName == null || statusDirName.length() == 0) {
            statusDir = new File(new File(AppConfig.getConfigurationsDir()), "../status");
            statusDirName = statusDir.getAbsolutePath();
        }
        // it's an error if folder files exists but not a folder
        // or if folder doesn't exist and cannot be created (determined by caller)
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
        String successPath = appConfig.getString(AppConfig.PROP_OUTPUT_SUCCESS);
        if (generateFiles || successPath == null || successPath.length() == 0) {
            successPath = new File(statusDir, "success" + getFormattedCurrentTimestamp() + ".csv").getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String errorPath = appConfig.getString(AppConfig.PROP_OUTPUT_ERROR);
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

        appConfig.setValue(AppConfig.PROP_OUTPUT_STATUS_DIR, statusDirName);
        appConfig.setValue(AppConfig.PROP_OUTPUT_SUCCESS, successPath);
        appConfig.setValue(AppConfig.PROP_OUTPUT_ERROR, errorPath);
        // TODO for unprocessed records
        // config.setValue(Config.OUTPUT_UNPROCESSED_RECORDS, unprocessedRecordsPath);
    }
        
    public String getFormattedCurrentTimestamp() {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MMddyyhhmmssSSS"); //$NON-NLS-1$
        return format.format(currentTime);
    }

    private void validateFile(String filePath) throws IOException {
        File file = new File(filePath);
        // finally make sure the output isn't the data access file
        String daoName = appConfig.getString(AppConfig.PROP_DAO_NAME);

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
        this.bulkV1Client = null;
        this.partnerClient = null;
        this.bulkV2Client = null;
        this.restClient = null;
        appConfig.setValue(AppConfig.PROP_OAUTH_ACCESSTOKEN, "");
    }

    public boolean attachmentsEnabled() {
        if (getAppConfig().isBulkV2APIEnabled()) {
            return false;
        } else {
            return getAppConfig().getBoolean(AppConfig.PROP_BULK_API_ZIP_CONTENT);
        }
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
