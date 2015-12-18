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

package com.salesforce.dataloader.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.exception.ConfigInitializationException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.security.EncryptionUtil;

/**
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class Config {
    private static Logger logger = Logger.getLogger(Config.class);

    /**
     * Default values for specific parameters
     */
    public static final int DEFAULT_EXTRACT_REQUEST_SIZE = 500;
    public static final int DEFAULT_MIN_RETRY_SECS = 2;
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int MAX_RETRIES_LIMIT = 10;
    public static final int DEFAULT_CONNECTION_TIMEOUT_SECS = 60;
    public static final int DEFAULT_TIMEOUT_SECS = 540;
    public static final int DEFAULT_LOAD_BATCH_SIZE = 200;
    public static final int DEFAULT_DAO_WRITE_BATCH_SIZE = 500;
    public static final int DEFAULT_DAO_READ_BATCH_SIZE = 200;
    public static final int MAX_LOAD_BATCH_SIZE = 200;
    public static final int MAX_DAO_READ_BATCH_SIZE = 200;
    public static final int MAX_DAO_WRITE_BATCH_SIZE = 2000;
    public static final int MAX_BULK_API_BATCH_BYTES = 10000000;
    public static final int MAX_BULK_API_BATCH_SIZE = 10000;
    public static final int DEFAULT_BULK_API_BATCH_SIZE = 2000;
    public static final long DEFAULT_BULK_API_CHECK_STATUS_INTERVAL = 5000L;
	public static final String DEFAULT_ENDPOINT_URL = "https://login.salesforce.com";
	
	/*
	 * Issue #59 - Dataloader will not read all the database rows to get a total count
	 * when skipTotalCount = "false"
	 * 
	 * The default is "true"
	 */
	public static final Boolean DEFAULT_SKIP_TOTAL_COUNT = true;
    /**
     * Constants that were made not configurable by choice
     */
    public static final String ID_COLUMN_NAME = "ID"; //$NON-NLS-1$
    public static final String ERROR_COLUMN_NAME = "ERROR"; //$NON-NLS-1$
    public static final String STATUS_COLUMN_NAME = "STATUS"; //$NON-NLS-1$

    /**
     * The mapping from preference name to preference value (represented as strings).
     */
    private final Properties properties;

    /**
     * The default-default value for the respective types.
     */
    public static final boolean BOOLEAN_DEFAULT = false;
    public static final double DOUBLE_DEFAULT = 0.0;
    public static final float FLOAT_DEFAULT = 0.0f;
    public static final int INT_DEFAULT = 0;
    public static final long LONG_DEFAULT = 0L;
    public static final String STRING_DEFAULT = ""; //$NON-NLS-1$
    public static final Map<String,String> MAP_STRING_DEFAULT = new LinkedHashMap<>();

    /**
     * The Constants for the current Loader Keys
     */
    //
    // salesforce constants
    
    // Loader Preferences
    public static final String HIDE_WELCOME_SCREEN = "loader.hideWelcome";

    //Special Internal Configs
    public static final String SFDC_INTERNAL = "sfdcInternal"; //$NON-NLS-1$
    public static final String SFDC_INTERNAL_IS_SESSION_ID_LOGIN = "sfdcInternal.isSessionIdLogin"; //$NON-NLS-1$
    public static final String SFDC_INTERNAL_SESSION_ID = "sfdcInternal.sessionId"; //$NON-NLS-1$

    // salesforce client connectivity
    public static final String USERNAME = "sfdc.username"; //$NON-NLS-1$
    public static final String PASSWORD = "sfdc.password"; //$NON-NLS-1$
    public static final String ENDPOINT = "sfdc.endpoint"; //$NON-NLS-1$
    public static final String PROXY_HOST = "sfdc.proxyHost"; //$NON-NLS-1$
    public static final String PROXY_PORT = "sfdc.proxyPort"; //$NON-NLS-1$
    public static final String PROXY_USERNAME = "sfdc.proxyUsername"; //$NON-NLS-1$
    public static final String PROXY_PASSWORD = "sfdc.proxyPassword"; //$NON-NLS-1$
    public static final String PROXY_NTLM_DOMAIN = "sfdc.proxyNtlmDomain"; //$NON-NLS-1$
    public static final String TIMEOUT_SECS = "sfdc.timeoutSecs"; //$NON-NLS-1$
    public static final String CONNECTION_TIMEOUT_SECS = "sfdc.connectionTimeoutSecs"; //$NON-NLS-1$
    public static final String NO_COMPRESSION = "sfdc.noCompression"; //$NON-NLS-1$
    public static final String ENABLE_RETRIES = "sfdc.enableRetries"; //$NON-NLS-1$
    public static final String MAX_RETRIES = "sfdc.maxRetries"; //$NON-NLS-1$
    public static final String MIN_RETRY_SLEEP_SECS = "sfdc.minRetrySleepSecs"; //$NON-NLS-1$
    public static final String DEBUG_MESSAGES = "sfdc.debugMessages"; //$NON-NLS-1$
    public static final String DEBUG_MESSAGES_FILE = "sfdc.debugMessagesFile"; //$NON-NLS-1$
    public static final String RESET_URL_ON_LOGIN = "sfdc.resetUrlOnLogin"; //$NON-NLS-1$
    public static final String TRUNCATE_FIELDS = "sfdc.truncateFields";//$NON-NLS-1$
    public static final String BULK_API_ENABLED  = "sfdc.useBulkApi";
    public static final String BULK_API_SERIAL_MODE = "sfdc.bulkApiSerialMode";
    public static final String BULK_API_CHECK_STATUS_INTERVAL = "sfdc.bulkApiCheckStatusInterval";
    public static final String BULK_API_ZIP_CONTENT = "sfdc.bulkApiZipContent";
    public static final String WIRE_OUTPUT  = "sfdc.wireOutput";
    public static final String TIMEZONE = "sfdc.timezone";

    public static final String OAUTH_PARTIAL_SERVER = "server";
    public static final String OAUTH_PARTIAL_CLIENTSECRET = "clientsecret";
    public static final String OAUTH_PARTIAL_CLIENTID = "clientid";
    public static final String OAUTH_PARTIAL_REDIRECTURI = "redirecturi";
    public static final String OAUTH_ENVIRONMENTS = "sfdc.oauth.environments";
    public static final String OAUTH_ENVIRONMENT = "sfdc.oauth.environment";
    public static final String OAUTH_ACCESSTOKEN = "sfdc.oauth.accesstoken";
    public static final String OAUTH_REFRESHTOKEN = "sfdc.oauth.refreshtoken";
    public static final String OAUTH_SERVER = "sfdc.oauth." + OAUTH_PARTIAL_SERVER;
    public static final String OAUTH_CLIENTSECRET = "sfdc.oauth." + OAUTH_PARTIAL_CLIENTSECRET;
    public static final String OAUTH_CLIENTID = "sfdc.oauth." + OAUTH_PARTIAL_CLIENTID;
    public static final String OAUTH_REDIRECTURI = "sfdc.oauth." + OAUTH_PARTIAL_REDIRECTURI;

    // salesforce operation parameters
    public static final String INSERT_NULLS = "sfdc.insertNulls"; //$NON-NLS-1$
    public static final String ENTITY = "sfdc.entity"; //$NON-NLS-1$
    public static final String LOAD_BATCH_SIZE = "sfdc.loadBatchSize"; //$NON-NLS-1$
    public static final String ASSIGNMENT_RULE = "sfdc.assignmentRule"; //$NON-NLS-1$
    public static final String EXTERNAL_ID_FIELD = "sfdc.externalIdField"; //$NON-NLS-1$
    public static final String EXTRACT_REQUEST_SIZE = "sfdc.extractionRequestSize"; //$NON-NLS-1$
    public static final String EXTRACT_SOQL = "sfdc.extractionSOQL"; //$NON-NLS-1$

    //
    // process configuration (action parameters)
    //
    public static final String OPERATION = "process.operation"; //$NON-NLS-1$
    public static final String MAPPING_FILE = "process.mappingFile"; //$NON-NLS-1$
    public static final String EURO_DATES = "process.useEuropeanDates"; //$NON-NLS-1$

    // process configuration
    public static final String OUTPUT_STATUS_DIR = "process.statusOutputDirectory"; //$NON-NLS-1$
    public static final String OUTPUT_SUCCESS = "process.outputSuccess"; //$NON-NLS-1$
    public static final String ENABLE_EXTRACT_STATUS_OUTPUT = "process.enableExtractStatusOutput"; //$NON-NLS-1$
    public static final String ENABLE_LAST_RUN_OUTPUT = "process.enableLastRunOutput"; //$NON-NLS-1$
    public static final String LAST_RUN_OUTPUT_DIR = "process.lastRunOutputDirectory"; //$NON-NLS-1$
    public static final String OUTPUT_ERROR = "process.outputError"; //$NON-NLS-1$
    public static final String LOAD_ROW_TO_START_AT = "process.loadRowToStartAt"; //$NON-NLS-1$
    public static final String INITIAL_LAST_RUN_DATE = "process.initialLastRunDate";
    public static final String ENCRYPTION_KEY_FILE = "process.encryptionKeyFile"; //$NON-NLS-1$

    // data access configuration (e.g., for CSV file, database, etc).
    public static final String DAO_TYPE = "dataAccess.type"; //$NON-NLS-1$
    public static final String DAO_NAME = "dataAccess.name"; //$NON-NLS-1$
    public static final String DAO_READ_BATCH_SIZE = "dataAccess.readBatchSize";
    public static final String DAO_WRITE_BATCH_SIZE = "dataAccess.writeBatchSize";
    public static final String DAO_SKIP_TOTAL_COUNT = "dataAccess.skipTotalCount";

    /*
     * TODO: when batching is introduced to the DataAccess, these parameters will become useful
     *     public static final String DAO_REQUEST_SIZE = "dataAccess.extractionRequestSize";
     *     public static final String DAO_BATCH_SIZE = "dataAccess.batchSize";
     */
    public static final String READ_UTF8 = "dataAccess.readUTF8"; //$NON-NLS-1$
    public static final String WRITE_UTF8 = "dataAccess.writeUTF8"; //$NON-NLS-1$

    /**
     * Indicates whether a value as been changed
     */
    private boolean dirty = false;

    /**
     * The file name used by the <code>load</code> method to load a property file. This filename is used to save the
     * properties file when <code>save</code> is called.
     */
    private String filename;
    /**
     * The <code>lastRun</code> is for last run statistics file
     */
    private final LastRun lastRun;
    /**
     * <code>encrypter</code> is a utility used internally in the config for reading/writing encrypted values. Right
     * now, the list of encrypted values is known to this class only.
     */
    private final EncryptionUtil encrypter = new EncryptionUtil();

    private boolean isBatchMode = false;

    private final String configDir;

    /**
     * <code>dateFormatter</code> will be used for getting dates in/out of the configuration file(s)
     */
    public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * The string representation used for <code>true</code>(<code>"true"</code>).
     */
    public static final String TRUE = "true"; //$NON-NLS-1$

    /**
     * The string representation used for <code>false</code>(<code>"false"</code>).
     */
    public static final String FALSE = "false"; //$NON-NLS-1$

    /**
     * communications with bulk api always use UTF8
     */
    public static final String BULK_API_ENCODING = "UTF-8";

    /**
     * Creates an empty config that loads from and saves to the a file.
     * <p>
     * Use the methods <code>load()</code> and <code>save()</code> to load and store this preference store.
     * </p>
     *
     * @param filename
     *            the file name
     * @throws ConfigInitializationException
     * @see #load()
     * @see #save()
     */
    public Config(String configDir, String filename, String lastRunFileName) throws ConfigInitializationException {
        properties = new LinkedProperties();
        this.configDir = configDir;
        this.filename = filename;
        // last run gets initialized a little later since config params are needed for that
        this.lastRun = new LastRun(lastRunFileName);
    }

    /**
     * Initialize last run directory and file. This works hand in hand with Config constructor and the load. The config
     * needs to be loaded before this. In case of UI, config is loaded once, in case of command line, config is loaded
     * and then overrides are loaded.
     */
    public void initLastRunFile() {
        if(getBoolean(Config.ENABLE_LAST_RUN_OUTPUT)) {
            String lastRunDir = getString(Config.LAST_RUN_OUTPUT_DIR);
            if(lastRunDir == null || lastRunDir.length() == 0) {
                lastRunDir = this.configDir;
            }
            this.lastRun.init(lastRunDir, true);
            try {
                this.lastRun.load();
            } catch (IOException e) {
                logger.warn(Messages.getFormattedString("LastRun.errorLoading", new String[] {
                        this.lastRun.getFullPath(), e.getMessage() }), e);
            }
        }
    }

    private boolean useBulkApiByDefault() {
        return false;
    }

    /**
     * This sets the current defaults.
     */
    public void setDefaults() {
		setValue(HIDE_WELCOME_SCREEN, true);
		setValue(ENDPOINT, DEFAULT_ENDPOINT_URL);
        setValue(LOAD_BATCH_SIZE, useBulkApiByDefault() ? DEFAULT_BULK_API_BATCH_SIZE : DEFAULT_LOAD_BATCH_SIZE);
        setValue(LOAD_ROW_TO_START_AT, 0);
        setValue(TIMEOUT_SECS, DEFAULT_TIMEOUT_SECS);
        setValue(CONNECTION_TIMEOUT_SECS, DEFAULT_CONNECTION_TIMEOUT_SECS);
        setValue(ENABLE_RETRIES, true);
        setValue(MAX_RETRIES, DEFAULT_MAX_RETRIES);
        setValue(MIN_RETRY_SLEEP_SECS, DEFAULT_MIN_RETRY_SECS);
        setValue(ASSIGNMENT_RULE, ""); //$NON-NLS-1$
        setValue(INSERT_NULLS, false);
        setValue(ENABLE_EXTRACT_STATUS_OUTPUT, false);
        setValue(ENABLE_LAST_RUN_OUTPUT, true);
        setValue(RESET_URL_ON_LOGIN, true);
        setValue(EXTRACT_REQUEST_SIZE, DEFAULT_EXTRACT_REQUEST_SIZE);
        setValue(DAO_WRITE_BATCH_SIZE, DEFAULT_DAO_WRITE_BATCH_SIZE);
        setValue(DAO_READ_BATCH_SIZE, DEFAULT_DAO_READ_BATCH_SIZE);
        setValue(TRUNCATE_FIELDS, true);
        // TODO: When we're ready, make Bulk API turned on by default.
        setValue(BULK_API_ENABLED, useBulkApiByDefault());
        setValue(BULK_API_SERIAL_MODE, false);
        setValue(BULK_API_ZIP_CONTENT, false);
        setValue(BULK_API_CHECK_STATUS_INTERVAL, DEFAULT_BULK_API_CHECK_STATUS_INTERVAL);
        setValue(WIRE_OUTPUT,false);
        setValue(TIMEZONE, TimeZone.getDefault().getID());
        //sfdcInternal settings
        setValue(SFDC_INTERNAL, false);
        setValue(SFDC_INTERNAL_IS_SESSION_ID_LOGIN, false);
        setValue(SFDC_INTERNAL_SESSION_ID, (String) null);

        //oauth settings
        setValue(OAUTH_SERVER, DEFAULT_ENDPOINT_URL);
        setValue(OAUTH_REDIRECTURI, DEFAULT_ENDPOINT_URL);
        setValue(OAUTH_ENVIRONMENTS, STRING_DEFAULT);
        setValue(OAUTH_ENVIRONMENT, STRING_DEFAULT);
    }

    /**
     * Returns true if the name is a key in the config
     *
     * @param name
     *            the name of the key
     */
    public boolean contains(String name) {
        return properties.containsKey(name) || lastRun.hasParameter(name) && lastRun.containsKey(name);
    }

    /**
     * Gets boolean for a given name
     *
     * @param name
     * @return boolean
     */
    public boolean getBoolean(String name) {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return BOOLEAN_DEFAULT;
        if (value.equals(Config.TRUE)) return true;
        return false;
    }

    /**
     * Gets double for a given name.
     *
     * @param name
     * @return double
     * @throws ParameterLoadException
     */

    public double getDouble(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return DOUBLE_DEFAULT;
        double ival = DOUBLE_DEFAULT;
        try {
            ival = new Double(value).doubleValue();
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { name,
                    Double.class.getName() });
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets float for a given name.
     *
     * @param name
     * @return float
     * @throws ParameterLoadException
     */
    public float getFloat(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return FLOAT_DEFAULT;
        float ival = FLOAT_DEFAULT;
        try {
            ival = new Float(value).floatValue();
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { name,
                    Float.class.getName() });
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets int for a given name.
     *
     * @param name
     * @return int
     * @throws ParameterLoadException
     */
    public int getInt(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return INT_DEFAULT;
        int ival = 0;
        try {
            ival = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { name,
                    Integer.class.getName() });
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets long for a given name.
     *
     * @param name
     * @return long
     * @throws ParameterLoadException
     */
    public long getLong(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return LONG_DEFAULT;
        long ival = LONG_DEFAULT;
        try {
            ival = Long.parseLong(value);
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { name,
                    Long.class.getName() });
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets  required string param for a given name.  If not found, throws an exception
     *
     * @param name
     * @return string
     * @throws ParameterLoadException
     */
    public String getStringRequired(String name) throws ParameterLoadException {
        String value = getString(name);
        if(value == null || value.length() == 0) {
            String errMsg = Messages.getFormattedString("Config.errorNoRequiredParameter", name); //$NON-NLS-1$
            logger.fatal(errMsg);
            throw new ParameterLoadException(errMsg);
        }
        return value;
    }

    /**
     * Gets string for a given name.
     *
     * @param name
     * @return string
     */
    public String getString(String name) {
        String value = getParamValue(name);
        if (value == null) return STRING_DEFAULT;
        return value;
    }


    public ArrayList<String> getStrings(String name) {
        String values = getString(name);
        ArrayList<String> list = new ArrayList<>();
        if (values != null && !values.trim().isEmpty()){
            Collections.addAll(list, values.trim().split(","));
        }
        return list;
    }

    /**
     * Gets an enum value for a given config name.
     * 
     * @param enumClass
     * @param name
     */
    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String name) {
        return Enum.valueOf(enumClass, getString(name));
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(getString(TIMEZONE));
    }

    /**
     * Gets path to a config file given the config file property name
     *
     * @param configFileProperty
     *            property containing a config filename
     * @return Config filename path based on config property value. Config file is assumed to reside in the global
     *         config directory
     */
    public String getConfigFilename(String configFileProperty) {
        String value = getParamValue(configFileProperty);
        if (value == null) return null;
        return constructConfigFilePath(new File(value).getName());
    }

    public String getLastRunFilename() {
        return this.lastRun == null ? null : this.lastRun.getFullPath();
    }


    /**
     * Constructs config file path based on the configuration directory and the passed in config filename
     *
     * @param configFilename
     *            Config filename that resides in the config directory
     * @return Full path to the config file
     */
    public String constructConfigFilePath(String configFilename) {
        File configPathFile = new File(this.filename).getParentFile();
        return new File(configPathFile, configFilename).getAbsolutePath();
    }

    /**
     * @param name
     * @return Date
     * @throws ParameterLoadException
     */
    public Date getDate(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return Calendar.getInstance().getTime();
        Date dval = null;
        try {
            dval = DATE_FORMATTER.parse(value);
        } catch (ParseException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { name,
                    Date.class.getName() });
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return dval;
    }

    /**
     * Get map from a string param value. String format of map is key1=val1,key2=val2,...
     * @param name
     * @return Map
     * @throws ParameterLoadException
     */
    public Map<String,String> getMap(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return MAP_STRING_DEFAULT;
        Map<String,String> mval = new HashMap<String,String>();
        String[] pairs = value.split(",");
        for(String pair : pairs) {
            String[] nameValue = pair.split("=");
            if(nameValue.length != 2) {
                String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { name,
                        Map.class.getName() });
                logger.warn(errMsg);
                throw new ParameterLoadException(errMsg);
            }
            mval.put(nameValue[0],nameValue[1]);
        }
        return mval;
    }

    /**
     * @param name
     * @return parameter value
     */
    private String getParamValue(String name) {
        String value;
        if(lastRun.hasParameter(name)) {
            value = lastRun.getProperty(name);
        } else {
            value = properties != null ? properties.getProperty(name) : null;
        }
        return value;
    }

    /**
     * Prints the contents of this preference store to the given print stream.
     *
     * @param out
     *            the print stream
     */
    public void list(PrintStream out) {
        properties.list(out);
        lastRun.list(out);
    }

    /**
     * Prints the contents of this preference store to the given print writer.
     *
     * @param out
     *            the print writer
     */
    public void list(PrintWriter out) {
        properties.list(out);
        lastRun.list(out);
    }

    /**
     * Loads this preference store from the file established in the constructor <code>Config(java.lang.String)</code>
     * (or by <code>setFileName</code>). Default preference values are not affected.
     *
     * @exception java.io.IOException
     *                if there is a problem loading this store
     * @throws IOException
     * @throws ConfigInitializationException
     */
    public void load() throws IOException, ConfigInitializationException {
        if (filename == null) {
            logger.fatal(Messages.getString("Config.fileMissing"));
            throw new IOException(Messages.getString("Config.fileMissing")); //$NON-NLS-1$
        }
        FileInputStream in = new FileInputStream(filename);
        try {
            load(in);
        } finally {
            in.close();
        }
    }

    /**
     * Loads this preference store from the given input stream. Default preference values are not affected.
     *
     * @param in
     *            the input stream
     * @throws ConfigInitializationException
     *             If there's a problem loading the parameters
     * @throws IOException
     *             IF there's an I/O problem loading parameter from file
     */
    private void load(InputStream in) throws ConfigInitializationException, IOException {
        try {
            properties.load(in);
        } catch (IOException e) {
            logger.fatal(Messages.getFormattedString("Config.errorPropertiesLoad", e.getMessage()));
            throw e;
        }
        // paramter post-processing
        postLoad(properties);

        dirty = false;
    }

    /**
     * Post process parameters. Right now, only decrypts encrypted values in the map
     *
     * @param values
     *            Values to be post-processed
     * @throws ConfigInitializationException
     */
    @SuppressWarnings("unchecked")
    private void postLoad(Map values) throws ConfigInitializationException {
        Map<String, String> propMap = values;

        // initialize encryption
        initEncryption(propMap);

        // decrypt encrypted values
        if (propMap.containsKey(PASSWORD)) {
            String propValue = decryptProperty(encrypter, propMap, PASSWORD, isBatchMode());
            if(propValue == null) propValue = "";
            propMap.put(PASSWORD, propValue);
        }
        if (propMap.containsKey(PROXY_PASSWORD)) {
            String propValue = decryptProperty(encrypter, propMap, PROXY_PASSWORD, isBatchMode());
            if(propValue == null) propValue = "";
            propMap.put(PROXY_PASSWORD, propValue);
        }
    }

    /**
     * Load config parameter override values. The main use case is loading of overrides from external config file
     *
     * @param configOverrideMap
     * @throws ParameterLoadException
     * @throws ConfigInitializationException
     */
    public void loadParameterOverrides(Map<String, String> configOverrideMap) throws ParameterLoadException,
    ConfigInitializationException {
        // Need to initialize last run date if it's present neither in config or override
        if (configOverrideMap.containsKey(INITIAL_LAST_RUN_DATE)) {
            lastRun.setDefault(LastRun.LAST_RUN_DATE, configOverrideMap.get(INITIAL_LAST_RUN_DATE));
        }

        // make sure to post process the args to be loaded
        postLoad(configOverrideMap);

        // replace values in the Config
        putValue(configOverrideMap);

        // make sure that last run file gets the latest configuration
        initLastRunFile();
    }

    /**
     * Decrypt property with propName using the encrypter. If decryption succeeds, return the decrypted value
     *
     * @param propMap
     * @param propName
     * @return decrypted property value
     * @throws ParameterLoadException
     */
    static private String decryptProperty(EncryptionUtil encrypter, Map<String, String> propMap, String propName, boolean isBatch)
            throws ParameterLoadException {
        String propValue = propMap.get(propName);
        if (propValue != null && propValue.length() > 0) {
            try {
                return encrypter.decryptString(propValue);
            } catch (GeneralSecurityException e) {
                // if running in the UI, we can ignore encryption errors
                if(isBatch) {
                    String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { propName,
                            String.class.getName() });
                    logger.error(errMsg, e);
                    throw new ParameterLoadException(errMsg, e);
                } else {
                    return null;
                }
            } catch (Exception e) {
                String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[] { propName,
                        String.class.getName() });
                logger.error(errMsg, e);
                throw new ParameterLoadException(errMsg, e);
            }
        }
        return propValue;
    }

    /**
     * @throws ConfigInitializationException
     */
    private void initEncryption(Map<String, String> values) throws ConfigInitializationException {
        // initialize encrypter
        String keyFile = values.get(ENCRYPTION_KEY_FILE);
        if (keyFile != null && keyFile.length() != 0) {
            try {
                encrypter.setCipherKeyFromFilePath(keyFile);
            } catch (IOException e) {
                String errMsg = Messages.getFormattedString("Config.errorSecurityInit", new String[] { keyFile,
                        e.getMessage() });
                logger.error(errMsg);
                throw new ConfigInitializationException(errMsg);
            }
        }
    }

    /**
     * Returns whether the config needs saving
     *
     * @return boolean
     */
    public boolean needsSaving() {
        return dirty;
    }

    /**
     * Returns an enumeration of all preferences known to this config
     *
     * @return an array of preference names
     */
    public String[] preferenceNames() {
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<?> en = properties.propertyNames();
        while (en.hasMoreElements()) {
            list.add((String)en.nextElement());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Puts a set of values from a map into config
     *
     * @param values
     *            Map of overriding values
     * @throws ParameterLoadException
     * @throws ConfigInitializationException
     */
    public void putValue(Map<String, String> values) throws ParameterLoadException, ConfigInitializationException {
        for (String key : values.keySet()) {
            putValue(key, values.get(key));
        }
    }

    /**
     * Puts a value into the config
     *
     * @param name
     * @param value
     */
    public void putValue(String name, String value) {
        String oldValue = getString(name);
        if (oldValue == null || !oldValue.equals(value)) {
            setValue(name, value);
            dirty = true;
        }
    }

    /**
     * Saves the preferences to the file from which they were originally loaded.
     *
     * @exception java.io.IOException
     *                if there is a problem saving this store
     * @throws GeneralSecurityException
     */
    public void save() throws IOException, GeneralSecurityException {
        if (filename == null) {
            throw new IOException(Messages.getString("Config.fileMissing")); //$NON-NLS-1$
        }

        // Secure password code prevents the saving of passwords
        // no great way to do this,
        String oldPassword = encryptProperty(PASSWORD);
        String oldProxyPassword = encryptProperty(PROXY_PASSWORD);

        String oauthAccessToken = getString(OAUTH_ACCESSTOKEN);
        String oauthRefreshToken = getString(OAUTH_REFRESHTOKEN);
        putValue(OAUTH_ACCESSTOKEN, "");
        putValue(OAUTH_REFRESHTOKEN, "");


        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            save(out, "Loader Config"); //$NON-NLS-1$
        } finally {
            if (out != null) {
                out.close();
            }
            // restore original property values
            putValue(PASSWORD, oldPassword);
            putValue(PROXY_PASSWORD, oldProxyPassword);
            putValue(OAUTH_ACCESSTOKEN, oauthAccessToken);
            putValue(OAUTH_REFRESHTOKEN, oauthRefreshToken);

        }
        // save last run statistics
        lastRun.save();
    }


    /**
     * Save statistics from the last run
     *
     * @throws IOException
     */
    public void saveLastRun() throws IOException {
        lastRun.save();
    }

    /**
     * @param propName
     *            name of the property
     * @return old value of the property
     * @throws GeneralSecurityException
     */
    private String encryptProperty(String propName) throws GeneralSecurityException {
        String oldValue = getString(propName);
        if (oldValue != null && oldValue.length() > 0) {
            putValue(propName, encrypter.encryptString(oldValue));
        }
        return oldValue;
    }

    /**
     * Saves this config to the given output stream. The given string is inserted as header information.
     *
     * @param out
     *            the output stream
     * @param header
     *            the header
     * @exception java.io.IOException
     *                if there is a problem saving this store
     */
    private void save(OutputStream out, String header) throws IOException {
        properties.store(out, header);
        dirty = false;
    }

    public void setValue(String name, Map<String,String> valueMap) {
        StringBuilder sb = new StringBuilder();
        for(String key : valueMap.keySet()) {
            // add comma for subsequent entries
            if(sb.length() != 0) {
                sb.append(",");
            }
            sb.append(key + "=" + valueMap.get(key));
        }
        putValue(name, sb.toString());
    }

    /**
     * Sets the name of the file used when loading and storing this config
     *
     * @param name
     *            the file name
     * @see #load()
     * @see #save()
     */
    public void setFilename(String name) {
        filename = name;
    }

    /**
     * @return the current configuration filename
     */
    public String getFilename() {
        return filename;
    }


    /**
     * Sets a double
     *
     * @param name
     * @param value
     */
    public void setValue(String name, double value) {
        setProperty(name, Double.toString(value));
    }

    /**
     * Sets a float
     *
     * @param name
     * @param value
     */
    public void setValue(String name, float value) {
        setProperty(name, Float.toString(value));
    }

    /**
     * Sets an int
     *
     * @param name
     * @param value
     */
    public void setValue(String name, int value) {
        setProperty(name, Integer.toString(value));
    }

    /**
     * Sets a long
     *
     * @param name
     * @param value
     */
    public void setValue(String name, long value) {
        setProperty(name, Long.toString(value));
    }

    /**
     * Sets a string
     *
     * @param name
     * @param values
     */
    public void setValue(String name, String... values) {
        if (values != null && values.length > 1) {
            StringJoiner joiner = new StringJoiner(",");
            for (String value : values) {
                joiner.add(value);
            }
            setProperty(name, joiner.toString());
        } else if (values != null && values.length > 0){
            setProperty(name, values[0]);
        } else {
            setProperty(name, null);
        }

    }

    /**
     * Sets a boolean
     *
     * @param name
     * @param value
     */
    public void setValue(String name, boolean value) {
        setProperty(name, Boolean.toString(value));
    }

    public void setValue(String name, Date value) {
        setProperty(name, DATE_FORMATTER.format(value));
    }

    /**
     * @param name
     * @param newValue
     */
    private void setProperty(String name, String newValue) {
        final String oldValue = getString(name);
        final boolean paramChanged = (oldValue == null || oldValue.length() == 0) ? (newValue != null && newValue
                .length() > 0) : !oldValue.equals(newValue);
                if (paramChanged) {
                    this.dirty = true;
                    configChanged(name, oldValue, newValue);
                    if (lastRun.hasParameter(name)) {
                        lastRun.put(name, newValue);
                    } else {
                        properties.put(name, newValue);
                    }

                }
    }

    public boolean isBatchMode() {
        return isBatchMode;
    }

    public void setBatchMode(boolean isBatchMode) {
        this.isBatchMode = isBatchMode;
    }

    public int getLoadBatchSize() {
        boolean bulkApi = isBulkAPIEnabled();
        int bs = -1;
        try {
            bs = getInt(LOAD_BATCH_SIZE);
        } catch (ParameterLoadException e) {}
        int maxBatchSize = bulkApi ? MAX_BULK_API_BATCH_SIZE : MAX_LOAD_BATCH_SIZE;
        return bs > maxBatchSize ? maxBatchSize : bs > 0 ? bs : getDefaultBatchSize(bulkApi);
    }

    public int getDefaultBatchSize(boolean bulkApi) {
        return bulkApi ? DEFAULT_BULK_API_BATCH_SIZE : DEFAULT_LOAD_BATCH_SIZE;
    }

    public boolean useBulkAPIForCurrentOperation() {
        return isBulkAPIEnabled() && isBulkApiOperation();
    }

    public boolean isBulkAPIEnabled() {
        return getBoolean(BULK_API_ENABLED);
    }

    private boolean isBulkApiOperation() {
        return getOperationInfo().bulkAPIEnabled();
    }

    public OperationInfo getOperationInfo() {
        return getEnum(OperationInfo.class, OPERATION);
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public String getCsvWriteEncoding() {
        if (Charset.defaultCharset().equals(UTF8) || getBoolean(WRITE_UTF8)) return UTF8.name();
        return Charset.defaultCharset().name();
    }

    private final List<ConfigListener> listeners = new ArrayList<ConfigListener>();

    public synchronized void addListener(ConfigListener l) {
        listeners.add(l);
    }

    private synchronized void configChanged(String key, String oldValue, String newValue) {
        for (ConfigListener l : this.listeners) {
            l.configValueChanged(key, oldValue, newValue);
        }
    }

    public String getOAuthEnvironmentString(String environmentName, String name){
        return getString("sfdc.oauth." + environmentName + "." + name);
    }

    public void setOAuthEnvironmentString(String environmentName, String name, String... values){
        setValue("sfdc.oauth." + environmentName + "." + name, values);
    }

    public void setOAuthEnvironment(String environment) {
        setValue(OAUTH_ENVIRONMENT, environment);
        setValue(OAUTH_SERVER, getOAuthEnvironmentString(environment, OAUTH_PARTIAL_SERVER));
        setValue(OAUTH_CLIENTID, getOAuthEnvironmentString(environment, OAUTH_PARTIAL_CLIENTID));
        setValue(OAUTH_CLIENTSECRET, getOAuthEnvironmentString(environment, OAUTH_PARTIAL_CLIENTSECRET));
        setValue(OAUTH_REDIRECTURI, getOAuthEnvironmentString(environment, OAUTH_PARTIAL_REDIRECTURI));
    }

    public static interface ConfigListener {
        void configValueChanged(String key, String oldValue, String newValue);
    }

}
