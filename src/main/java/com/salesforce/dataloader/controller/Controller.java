/*
 * Copyright (c) 2012, salesforce.com, inc. All rights reserved. Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following conditions are met: Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither the name of salesforce.com, inc. nor the
 * names of its contributors may be used to endorse or promote products derived from this software without specific
 * prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.dataloader.controller;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Logger;

import com.salesforce.dataloader.action.IAction;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.*;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DataAccessObject;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.mapping.*;
import com.salesforce.dataloader.ui.LoaderWindow;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.ws.ConnectionException;

/**
 * The class that controls dataloader engine (config, salesforce communication, mapping, dao). For UI, this is the
 * controller for all the underlying data access.
 * 
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 6.0
 */
public class Controller {

    private static boolean isLogInitialized = false; // make sure log is initialized only once

    /** the system property name used to determine the config directory */
    public static final String CONFIG_DIR_PROP = "salesforce.config.dir";

    public static final String CONFIG_FILE = "config.properties"; //$NON-NLS-1$
    private static final String LAST_RUN_FILE_SUFFIX = "_lastRun.properties"; //$NON-NLS-1$
    private static final String LOG_STDOUT_FILE = "sdl_out.log"; //$NON-NLS-1$
    private static final String CONFIG_DIR = "conf"; //$NON-NLS-1$
    private static String APP_NAME; //$NON-NLS-1$
    public static String APP_VERSION; //$NON-NLS-1$
    public static String API_VERSION;
    private static String APP_VENDOR; //$NON-NLS-1$

    /**
     * <code>config</code> is an instance of configuration that's tied to this instance of controller in a multithreaded
     * environment
     */
    private Config config;
    private Mapper mapper;

    private DataAccessObjectFactory daoFactory;
    private DataAccessObject dao;
    private BulkClient bulkClient;
    private PartnerClient partnerClient;

    // logger
    private static Logger logger = Logger.getLogger(Controller.class);
    private String appPath;

    private Controller(String name, boolean isBatchMode) throws ControllerInitializationException {
        // load app version properties
        Properties versionProps = new Properties();
        try {
            versionProps.load(this.getClass().getClassLoader().getResourceAsStream("version.properties"));
        } catch (IOException e) {
            throw new ControllerInitializationException(e);
        }
        APP_NAME = versionProps.getProperty("dataloader.name");
        APP_VENDOR = versionProps.getProperty("dataloader.vendor");
        // FIXME clean this up, make static
        // dataloader version has 3 parts, salesforce app api version should match first two parts
        APP_VERSION = versionProps.getProperty("dataloader.version");
        String[] dataloaderVersion = APP_VERSION.split("\\.");
        API_VERSION = dataloaderVersion[0] + "." + dataloaderVersion[1];

        // if name is passed to controller, use it to create a unique run file name
        initConfig(name, isBatchMode);
    }

    public void setConfigDefaults() {
        config.setDefaults();
    }

    public synchronized void executeAction(ILoaderProgress monitor) throws DataAccessObjectException, OperationException {
        OperationInfo operation = this.config.getOperationInfo();
        IAction action = operation.instantiateAction(this, monitor);
        logger.info(Messages.getFormattedString("Controller.executeStart", operation)); //$NON-NLS-1$
        action.execute();
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

    public void createDao() throws DataAccessObjectInitializationException {
        try {
            config.getStringRequired(Config.DAO_NAME); // verify required param exists: dao name
            dao = daoFactory.getDaoInstance(config.getStringRequired(Config.DAO_TYPE), config);
        } catch (Exception e) {
            logger.fatal(Messages.getString("Controller.errorDAOCreate"), e); //$NON-NLS-1$
            throw new DataAccessObjectInitializationException(Messages.getString("Controller.errorDAOCreate"), e); //$NON-NLS-1$
        }
    }

    public void createMapper() throws MappingInitializationException {
        String mappingFile = config.getString(Config.MAPPING_FILE);
        this.mapper = getConfig().getOperationInfo().isExtraction() ? new SOQLMapper(getPartnerClient(),
                dao.getColumnNames(), mappingFile) : new LoadMapper(getPartnerClient(), dao.getColumnNames(),
                mappingFile);
    }

    public void createAndShowGUI() throws ControllerInitializationException {
        // check config access for saving settings -- required in UI
        File configFile = new File(config.getFilename());
        if (!configFile.canWrite()) {
            String errMsg = Messages.getFormattedString("Controller.errorConfigWritable", config.getFilename());
            logger.fatal(errMsg);
            throw new ControllerInitializationException(errMsg);
        }
        // start the loader UI
        new LoaderWindow(this).run();
        saveConfig();
    }

    public static Controller getInstance(String name, boolean isBatchMode) throws ControllerInitializationException {
        return new Controller(name, isBatchMode);
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
     * Copies a file
     * 
     * @param file
     *            the file to copy from
     * @param destFile
     *            the file to copy to
     */
    private void copyFile(File file, File destFile) {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {

            // reading and writing 8 KB at a time
            int bufferSize = 8 * 1024;

            byte[] buf = new byte[bufferSize];

            in = new BufferedInputStream(new FileInputStream(file), bufferSize);
            int r = 0;

            out = new BufferedOutputStream(new FileOutputStream(destFile), bufferSize);

            while ((r = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, r);
            }
            out.flush();

        } catch (IOException ioe) {
            System.out.println("Cannot copy file " + file.getAbsolutePath());
        } finally {
            if (in != null) try {
                in.close();
            } catch (Exception e) {}
            if (out != null) try {
                out.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Get the current config.properties and load it into the config bean.
     * 
     * @param isBatchMode
     * @throws ControllerInitializationException
     */
    protected void initConfig(String name, boolean isBatchMode) throws ControllerInitializationException {

        // see if we are ui based
        String appdataDir = System.getProperty("appdata.dir");
        String configPath;
        String lastRunFileName = name + LAST_RUN_FILE_SUFFIX;
        if (appdataDir != null && appdataDir.length() > 0) {

            String appVendorDir = new File(appdataDir, APP_VENDOR).getAbsolutePath();
            // the application directory is versioned to support multiple versions of the loader running in parallel
            File appDir = new File(appVendorDir, getProductName());
            appPath = appDir.getAbsolutePath();
            if (!appDir.exists() || !appDir.isDirectory()) {
                if (!appDir.mkdirs()) {
                    System.out.println("Unable to create configuration directory");
                }
            }

            // look for config directory under user home directory
            String oldConfigDir = new File(System.getProperty("user.dir"), CONFIG_DIR).getAbsolutePath();

            // check if the files exist
            File configFile = new File(appDir, CONFIG_FILE);
            configPath = configFile.getAbsolutePath();
            if (!configFile.exists()) {
                File oldConfig = new File(oldConfigDir, CONFIG_FILE);
                if (!oldConfig.exists()) {
                    System.out.println("Cannot find default configuration file");
                } else {
                    copyFile(oldConfig, configFile);
                }
            }

        } else {
            // nope we are running commandline

            // FIXME PLEASE
            String configDirPath = getConfigDir();
            logger.debug("config dir: " + configDirPath);
            // if configDir is null, set it to target, which is the maven build output directory
            // TODO: FIXME
            if (configDirPath == null) {
                configDirPath = "target";
                logger.warn("config dir was null, so config dir has been set to " + configDirPath);
            }

            // TODO: we should log creating new files/directories
            // TODO: why did we add this?
            File configDir = new File(configDirPath);
            if (!(configDir.isDirectory() || configDir.mkdirs())) {
                throw new ControllerInitializationException("could not create configuration directory: " + configDir);
            } else {
                logger.info("config dir created at " + configDir.getAbsolutePath());
            }

            // let the File class combine the dir and file names
            File configFile = new File(configDirPath, CONFIG_FILE);

            // TODO: why did we add this?
            if (!configFile.exists()) try {
                configFile.createNewFile();
                configFile.setWritable(true);
                configFile.setReadable(true);
                logger.info("config file created at " + configFile.getAbsolutePath());
            } catch (IOException e) {
                throw new ControllerInitializationException(e);
            }

            this.appPath = configDirPath;
            configPath = configFile.getAbsolutePath();
        }

        initLog();

        try {
            config = new Config(getAppPath(), configPath, lastRunFileName);
            // set default before actual values are loaded
            config.setDefaults();
            config.setBatchMode(isBatchMode);
            config.load();
            logger.info(Messages.getMessage(getClass(), "configInit")); //$NON-NLS-1$
        } catch (IOException e) {
            throw new ControllerInitializationException(Messages.getMessage(getClass(), "errorConfigLoad", configPath),
                    e);
        } catch (ProcessInitializationException e) {
            throw new ControllerInitializationException(Messages.getMessage(getClass(), "errorConfigLoad", configPath),
                    e);
        }

        if (daoFactory == null) {
            daoFactory = new DataAccessObjectFactory();
        }
    }

    /**
     * @return product name
     */
    private String getProductName() {
        String productName = APP_NAME + " " + APP_VERSION;
        return productName;
    }

    /**
     * @throws FactoryConfigurationError
     */
    static synchronized public void initLog() throws FactoryConfigurationError {
        // init the log if not initialized already
        if (Controller.isLogInitialized) { return; }
        try {
            String logFilename = new File(System.getProperty("java.io.tmpdir"), LOG_STDOUT_FILE).getAbsolutePath();
            if (logFilename != null && logFilename.length() > 0) {
                System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFilename)), true));
                System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFilename)), true));
            }
            logger.info(Messages.getString("Controller.logInit")); //$NON-NLS-1$
        } catch (Exception ex) {
            System.out.println(Messages.getString("Controller.errorLogInit")); //$NON-NLS-1$
            System.out.println(ex.toString());
            System.exit(1);
        }
        Controller.isLogInitialized = true;
    }

    public static String getConfigDir() {
        return System.getProperty(CONFIG_DIR_PROP);
    }

    public PartnerClient getPartnerClient() {
        if (this.partnerClient == null) this.partnerClient = new PartnerClient(this);
        return this.partnerClient;
    }

    private ClientBase getClient() {
        return this.config.useBulkAPIForCurrentOperation() ? getBulkClient() : getPartnerClient();
    }

    public BulkClient getBulkClient() {
        if (this.bulkClient == null) {
            this.bulkClient = new BulkClient(this);
            loginIfSessionExists(this.bulkClient);
        }
        return this.bulkClient;
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

    public String getAppPath() {
        return appPath;
    }

    public Mapper getMapper() {
        return this.mapper;
    }

    public void setStatusFiles(String statusDirName, boolean createDir, boolean generateFiles)
            throws ProcessInitializationException {
        File statusDir = new File(statusDirName);
        // if status directory unspecified, create one based on config path
        if (statusDirName == null || statusDirName.length() == 0) {
            statusDir = new File(new File(appPath), "../status");
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
                if (!statusDir.mkdirs()) { throw new ProcessInitializationException(Messages.getFormattedString(
                        "Controller.errorCreatingOutputDir", statusDirName)); }
            }
        }

        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MMddyyhhmmssSSS"); //$NON-NLS-1$
        String timestamp = format.format(currentTime);

        // if status files are not specified, generate the files automatically
        String successPath = config.getString(Config.OUTPUT_SUCCESS);
        if (generateFiles || successPath == null || successPath.length() == 0) {
            successPath = new File(statusDir, "success" + timestamp + ".csv").getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String errorPath = config.getString(Config.OUTPUT_ERROR);
        if (generateFiles || errorPath == null || errorPath.length() == 0) {
            errorPath = new File(statusDir, "error" + timestamp + ".csv").getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // next validate the error and success csv
        try {
            validateFile(successPath);
            validateFile(errorPath);
        } catch (IOException e) {
            throw new ProcessInitializationException(e.getMessage(), e);
        }

        config.setValue(Config.OUTPUT_STATUS_DIR, statusDirName);
        config.setValue(Config.OUTPUT_SUCCESS, successPath);
        config.setValue(Config.OUTPUT_ERROR, errorPath);
    }

    private void validateFile(String filePath) throws IOException {
        File file = new File(filePath);
        // finally make sure the output isn't the data access file
        String daoName = config.getString(Config.DAO_NAME);

        // if it doesn't exist and we can create it, its valid
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) { throw new IOException(Messages.getMessage(getClass(),
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
        this.mapper = null;
    }
}
