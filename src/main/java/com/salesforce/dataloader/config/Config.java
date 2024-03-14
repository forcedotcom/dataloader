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

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.exception.ConfigInitializationException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.security.EncryptionAesUtil;
import com.salesforce.dataloader.util.AppUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TimeZone;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Lexi Viripaeff
 * @since 6.0
 * 
 * **** README *****
 * 
 * Config class assimilates all properties including:
 * - properties specified in config.properties file
 * - properties specified in process-conf.xml file
 * - properties specified as command line arguments
 * - properties specified in Settings dialog - these get stored in config.properties file
 * - properties set during operation steps (including login) and execution of an operation
 * 
 * How properties are loaded, set in-memory, and saved to a file:
 * - Properties loaded from and saved to config.properties file
 *   All properties except those that are defined with the prefix "CLI_OPTION_"
 *   can be specified in config.properties file. 
 *   "Save" from Setup dialog saves modified properties in config.properties file.
 *   
 * - Properties loaded from and saved to <process name>_lastRun.properties and ui_lastRun.properties files
 *   Contains 2 properties: timestamp of the last run and number of records processed during
 *   last upload (insert, upsert, update, delete, hard delete) operation if the operation
 *   was performed using Partner API.
 * 
 * - Properties loaded from process-conf.xml file. Properties are not saved to process-conf.xml.
 *   Contains properties relevant to execute an operation in the batch mode.
 *   A property specified in process-conf.xml overrides the same property
 *   specified in config.properties. However, it DOES NOT overwrite any property
 *   in config.properties file.
 *   
 * - Properties loaded from command line arguments
 *   Some properties can only be specified through command line arguments. These
 *   properties are defined with the prefix "CLI_OPTION_". A property set
 *   in command line argument overrides the value set in other files.
 *  
 * - Properties set in Settings dialog and saved in config.properties file
 * 
 * - Properties set during actions and saved in config.properties/<operation | "ui">_lastRun.properties file.
 *   Some properties are set during actions such as login, specifying results folder for an operation, and so on.
 *   For example, "process.operation" property is set when user clicks on the operation button.
 *   These properties may not get saved depending on whether they are designated as "read-only" properties.
 *   
 * - Certain properties are "read-only" in that their value set during the app execution
 *   is never saved. 
 *   For example, "sfdc.password" can be specified in config.properties, which
 *   may be overridden by a different value when the user logs in. However, its 
 *   value specified in config.properties is not overridden when save() method is invoked.
 *
 */
public class Config {
    private static Logger logger;
    private static final String DECRYPTED_SUFFIX = ".decrypted";

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
    
    // Bulk v1 and v2 limits specified at https://developer.salesforce.com/docs/atlas.en-us.salesforce_app_limits_cheatsheet.meta/salesforce_app_limits_cheatsheet/salesforce_app_limits_platform_bulkapi.htm
    public static final int MAX_BULK_API_BATCH_BYTES = 10000000;
    public static final int MAX_BULK_API_BATCH_SIZE = 10000;
    public static final int MAX_BULKV2_API_JOB_BYTES = 150000000;
    public static final int MAX_BULKV2_API_JOB_SIZE = 150000000;
    public static final int DEFAULT_BULK_API_BATCH_SIZE = 2000;
    
    public static final long DEFAULT_BULK_API_CHECK_STATUS_INTERVAL = 5000L;
    public static final String DEFAULT_ENDPOINT_URL = "https://login.salesforce.com";
    public static final String LIGHTNING_ENDPOINT_URL_PART_VAL = "lightning.force.com";
    public static final String MYSF_ENDPOINT_URL_PART_VAL = "mysalesforce.com";
    public static final String OAUTH_PROD_ENVIRONMENT_VAL = "Production";
    public static final String OAUTH_SB_ENVIRONMENT_VAL = "Sandbox";

    public static final String OAUTH_PROD_SERVER_VAL = "https://login.salesforce.com/";
    public static final String OAUTH_SB_SERVER_VAL = "https://test.salesforce.com/";

    public static final String OAUTH_PROD_REDIRECTURI_VAL = "https://login.salesforce.com/services/oauth2/success";
    public static final String OAUTH_SB_REDIRECTURI_VAL = "https://test.salesforce.com/services/oauth2/success";

    public static final String OAUTH_BULK_CLIENTID_VAL = "DataLoaderBulkUI/";
    public static final String OAUTH_PARTNER_CLIENTID_VAL = "DataLoaderPartnerUI/";
    
    public static final int DEFAULT_BULK_QUERY_PK_CHUNK_SIZE = 100000;
    public static final int MAX_BULK_QUERY_PK_CHUNK_SIZE = 250000;

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
    public static final String ID_COLUMN_NAME_IN_BULKV2 = "sf__id";
    public static final String ERROR_COLUMN_NAME = "ERROR"; //$NON-NLS-1$
    public static final String STATUS_COLUMN_NAME = "STATUS"; //$NON-NLS-1$
    public static final String STATUS_COLUMN_NAME_IN_BULKV2 = "sf__Created"; //$NON-NLS-1$

    /**
     * The mapping from preference name to preference value (represented as strings).
     */
    private Properties properties;
    private Properties readOnlyProperties;
    private Map<String, String> parameterOverridesMap;

    /**
     * The default-default value for the respective types.
     */
    public static final boolean BOOLEAN_DEFAULT = false;
    public static final double DOUBLE_DEFAULT = 0.0;
    public static final float FLOAT_DEFAULT = 0.0f;
    public static final int INT_DEFAULT = 0;
    public static final long LONG_DEFAULT = 0L;
    public static final String STRING_DEFAULT = ""; //$NON-NLS-1$
    public static final Map<String, String> MAP_STRING_DEFAULT = new LinkedHashMap<>();

    /**
     * The Constants for the current Loader Keys
     */
    //
    // salesforce constants

    // Loader Preferences
    public static final String HIDE_WELCOME_SCREEN = "loader.hideWelcome";
    public static final String SHOW_LOADER_UPGRADE_SCREEN = "loader.ui.showUpgrade";

    // Delimiter settings
    public static final String CSV_DELIMITER_COMMA = "loader.csvComma";
    public static final String CSV_DELIMITER_TAB = "loader.csvTab";
    public static final String CSV_DELIMITER_OTHER = "loader.csvOther";
    public static final String CSV_DELIMITER_OTHER_VALUE = "loader.csvOtherValue";
    public static final String CSV_DELIMITER_FOR_QUERY_RESULTS = "loader.query.delimiter";
    public static final String BUFFER_UNPROCESSED_BULK_QUERY_RESULTS = "loader.bufferUnprocessedBulkQueryResults";
    public static final String INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS = "loader.query.includeBinaryData";
    public static final String CACHE_DESCRIBE_GLOBAL_RESULTS = "loader.cacheSObjectNamesAndFields";
    
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
    public static final String FORMAT_PHONE_FIELDS = "sfdc.formatPhoneFields";//$NON-NLS-1$
    public static final String BULK_API_ENABLED = "sfdc.useBulkApi";
    public static final String BULK_API_SERIAL_MODE = "sfdc.bulkApiSerialMode";
    public static final String BULK_API_CHECK_STATUS_INTERVAL = "sfdc.bulkApiCheckStatusInterval";
    public static final String BULK_API_ZIP_CONTENT = "sfdc.bulkApiZipContent";
    public static final String WIRE_OUTPUT = "sfdc.wireOutput";
    public static final String TIMEZONE = "sfdc.timezone";

    public static final String OAUTH_PREFIX = "sfdc.oauth.";
    public static final String OAUTH_PARTIAL_BULK = "bulk";
    public static final String OAUTH_PARTIAL_PARTNER = "partner";
    public static final String OAUTH_PARTIAL_SERVER = "server";
    public static final String OAUTH_PARTIAL_CLIENTSECRET = "clientsecret";
    public static final String OAUTH_PARTIAL_CLIENTID = "clientid";
    public static final String OAUTH_PARTIAL_REDIRECTURI = "redirecturi";
    public static final String OAUTH_PARTIAL_BULK_CLIENTID = OAUTH_PARTIAL_BULK + "." + OAUTH_PARTIAL_CLIENTID;
    public static final String OAUTH_PARTIAL_PARTNER_CLIENTID = OAUTH_PARTIAL_PARTNER + "." + OAUTH_PARTIAL_CLIENTID;

    public static final String OAUTH_ENVIRONMENTS = OAUTH_PREFIX + "environments";
    public static final String OAUTH_ENVIRONMENT = OAUTH_PREFIX + "environment";
    public static final String OAUTH_ACCESSTOKEN = OAUTH_PREFIX + "accesstoken";
    public static final String OAUTH_REFRESHTOKEN = OAUTH_PREFIX + "refreshtoken";
    public static final String OAUTH_SERVER = OAUTH_PREFIX + OAUTH_PARTIAL_SERVER;
    public static final String OAUTH_CLIENTSECRET = OAUTH_PREFIX + OAUTH_PARTIAL_CLIENTSECRET;
    public static final String OAUTH_CLIENTID = OAUTH_PREFIX + OAUTH_PARTIAL_CLIENTID;
    public static final String OAUTH_REDIRECTURI = OAUTH_PREFIX + OAUTH_PARTIAL_REDIRECTURI;
    public static final String OAUTH_LOGIN_FROM_BROWSER = OAUTH_PREFIX + "loginfrombrowser";
    public static final String REUSE_CLIENT_CONNECTION = "sfdc.reuseClientConnection";
    public static final String RICH_TEXT_FIELD_REGEX = "sfdx.richtext.regex";
    
    // salesforce operation parameters
    public static final String INSERT_NULLS = "sfdc.insertNulls"; //$NON-NLS-1$
    public static final String ENTITY = "sfdc.entity"; //$NON-NLS-1$
    public static final String LOAD_BATCH_SIZE = "sfdc.loadBatchSize"; //$NON-NLS-1$
    public static final String ASSIGNMENT_RULE = "sfdc.assignmentRule"; //$NON-NLS-1$
    public static final String EXTERNAL_ID_FIELD = "sfdc.externalIdField"; //$NON-NLS-1$
    public static final String EXTRACT_REQUEST_SIZE = "sfdc.extractionRequestSize"; //$NON-NLS-1$
    public static final String EXTRACT_SOQL = "sfdc.extractionSOQL"; //$NON-NLS-1$
    public static final String SORT_EXTRACT_FIELDS = "sfdc.sortExtractionFields"; //$NON-NLS-1$
    public static final String LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT = "sfdc.load.preserveWhitespaceInRichText";

    //
    // process configuration (action parameters)
    //
    // process.name is used to load the DynaBean from process-conf.xml file 
    // with the same id as the value of process.name property.
    public static final String PROCESS_NAME = "process.name";
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
    public static final String OUTPUT_UNPROCESSED_RECORDS = "process.unprocessedRecords"; //$NON-NLS-1$
    public static final String LOAD_ROW_TO_START_AT = "process.loadRowToStartAt"; //$NON-NLS-1$
    public static final String INITIAL_LAST_RUN_DATE = "process.initialLastRunDate";
    public static final String ENCRYPTION_KEY_FILE = "process.encryptionKeyFile"; //$NON-NLS-1$
    public static final String PROCESS_THREAD_NAME = "process.thread.name";
    public static final String PROCESS_KEEP_ACCOUNT_TEAM = "process.keepAccountTeam";
    public static final String PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE = "process.batchMode.exitWithErrorOnFailedRows";

    // data access configuration (e.g., for CSV file, database, etc).
    public static final String DAO_TYPE = "dataAccess.type"; //$NON-NLS-1$
    public static final String DAO_NAME = "dataAccess.name"; //$NON-NLS-1$
    public static final String DAO_READ_BATCH_SIZE = "dataAccess.readBatchSize";
    public static final String DAO_WRITE_BATCH_SIZE = "dataAccess.writeBatchSize";
    public static final String DAO_SKIP_TOTAL_COUNT = "dataAccess.skipTotalCount";
    public static final String DAO_READ_PREPROCESSOR_SCRIPT = "dataAccess.read.preProcessorScript";
    public static final String DAO_WRITE_POSTPROCESSOR_SCRIPT = "dataAccess.write.postProcessorScript";

    /*
     * TODO: when batching is introduced to the DataAccess, these parameters will become useful
     *     public static final String DAO_REQUEST_SIZE = "dataAccess.extractionRequestSize";
     *     public static final String DAO_BATCH_SIZE = "dataAccess.batchSize";
     */
    public static final String READ_UTF8 = "dataAccess.readUTF8"; //$NON-NLS-1$
    public static final String WRITE_UTF8 = "dataAccess.writeUTF8"; //$NON-NLS-1$
    public static final String READ_CHARSET = "dataAccess.readCharset";
    
    public static final String API_VERSION_PROP="salesforce.api.version";

    
    /**
     *  ===============  PILOT config properties ========
     * - These properties are used for the features in pilot phase. These features are
     *   not supported. Also, they may not be complete.
     *   
     * - The property name is prefixed with "pilot". Remove the prefix and move the property
     *   declaration above this comment section when the feature is complete and supported.
     * 
     * - DO NOT EXPOSE THEM THROUGH THE UI.
     * ===================================================
     */
    public static final String PILOT_PROPERTY_PREFIX = "pilot.";
    
    /*
    public static final String ENABLE_BULK_QUERY_PK_CHUNKING = PILOT_PROPERTY_PREFIX + "sfdc.enableBulkQueryPKChunking";
    public static final String BULK_QUERY_PK_CHUNK_SIZE =  PILOT_PROPERTY_PREFIX + "sfdc.bulkQueryPKChunkSize";
    public static final String BULK_QUERY_PK_CHUNK_START_ROW = PILOT_PROPERTY_PREFIX + "sfdc.bulkQueryChunkStartRow";
    */
    public static final String DUPLICATE_RULE_ALLOW_SAVE = PILOT_PROPERTY_PREFIX + "sfdc.duplicateRule.allowSave"; //$NON-NLS-1$
    public static final String DUPLICATE_RULE_INCLUDE_RECORD_DETAILS = PILOT_PROPERTY_PREFIX + "sfdc.duplicateRule.includeRecordDetails"; //$NON-NLS-1$
    public static final String DUPLICATE_RULE_RUN_AS_CURRENT_USER = PILOT_PROPERTY_PREFIX + "sfdc.duplicateRule.runAsCurrentUser"; //$NON-NLS-1$
    public static final String BULKV2_API_ENABLED = PILOT_PROPERTY_PREFIX + "sfdc.useBulkV2Api";
    public static final String LIMIT_OUTPUT_TO_QUERY_FIELDS = "loader.query.limitOutputToQueryFields";
    /*
     * ===============================
     * End of config properties
     * ===============================
     */
    
    /**
     * Indicates whether a value as been changed
     */
    private boolean dirty = false;

    /**
     * The file name used by the <code>load</code> method to load a property file. This filename is
     * used to save the properties file when <code>save</code> is called.
     */
    private String filename;
    /**
     * The <code>lastRun</code> is for last run statistics file
     */
    private LastRun lastRun;
    /**
     * <code>encrypter</code> is a utility used internally in the config for reading/writing
     * encrypted values. Right now, the list of encrypted values is known to this class only.
     */
    private final EncryptionAesUtil encrypter = new EncryptionAesUtil();

    private final String configDir;
    
    /**
     * <code>dateFormatter</code> will be used for getting dates in/out of the configuration
     * file(s)
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
    public static final String BULK_API_ENCODING = StandardCharsets.UTF_8.name();
    public static final String CONFIG_FILE = "config.properties"; //$NON-NLS-1$
    
    /*
     * command line options. Not stored in config.properties file.
     * ************
     * Option names MUST start with the prefix "CLI_OPTION_"
     * ************
     */
    public static final String CLI_OPTION_RUN_MODE = "run.mode";
    public static final String RUN_MODE_UI_VAL = "ui";
    public static final String RUN_MODE_BATCH_VAL = "batch";
    public static final String RUN_MODE_INSTALL_VAL = "install";
    public static final String RUN_MODE_ENCRYPT_VAL = "encrypt";
    
    public static final String SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV = "process.bulk.saveServerLoadAndRawResultsInCSV";
    public static final String PROCESS_BULK_CACHE_DATA_FROM_DAO = "process.bulk.cacheDataFromDao";
    public static final String READ_ONLY_CONFIG_PROPERTIES = "config.properties.readonly";
    public static final String WIZARD_WIDTH = "sfdc.ui.wizard.width";
    public static final String WIZARD_HEIGHT = "sfdc.ui.wizard.height";
    public static final String WIZARD_CLOSE_ON_FINISH = "sfdc.ui.wizard.closeOnFinish";
    public static final int DEFAULT_WIZARD_WIDTH = 600;
    public static final int DEFAULT_WIZARD_HEIGHT = 700;
    
    private static final String LAST_RUN_FILE_SUFFIX = "_lastRun.properties"; //$NON-NLS-1$
    // Following properties are read-only, i.e. they are not overridden during save() to config.properties
    // - These properties are not set in Advanced Settings dialog.
    // - Make sure to list all sensitive properties such as password because these properties are not saved.
    private static final String[] READ_ONLY_PROPERTY_NAMES = {
            PASSWORD,
            PROXY_PASSWORD,
            OAUTH_ACCESSTOKEN,
            OAUTH_REFRESHTOKEN,
            EXTERNAL_ID_FIELD,
            MAPPING_FILE,
            EXTRACT_SOQL,
            OUTPUT_SUCCESS,
            OUTPUT_ERROR,
            DAO_NAME,
            DAO_TYPE,
            ENTITY,
            OPERATION,
            DEBUG_MESSAGES,
            DEBUG_MESSAGES_FILE,
            WIRE_OUTPUT,
            PROCESS_THREAD_NAME,
            PROCESS_BULK_CACHE_DATA_FROM_DAO,
            PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE,
            SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV,
            API_VERSION_PROP,
            READ_CHARSET,
            READ_ONLY_CONFIG_PROPERTIES,
            RICH_TEXT_FIELD_REGEX,
            DAO_READ_PREPROCESSOR_SCRIPT,
            DAO_WRITE_POSTPROCESSOR_SCRIPT
    };
    
    /**
     * Creates an empty config that loads from and saves to the a file. <p> Use the methods
     * <code>load()</code> and <code>save()</code> to load and store this preference store. </p>
     *
     * @param filename the file name
     * @see #load()
     * @see #save()
     */
    private Config(String filename, Map<String, String> overridesMap) throws ConfigInitializationException, IOException {
        properties = new LinkedProperties();
        this.filename = filename;
        
        File configFile = new File(this.filename);
        this.configDir = configFile.getParentFile().getAbsolutePath();
        
        // initialize with defaults 
        // 
        this.setDefaults();
        
        // load from config.properties file
        this.load();
        
        // load parameter overrides after loading from config.properties
        // parameter overrides are from two places:
        // 1. process-conf.properties for CLI mode
        // 2. command line options for both CLI and UI modes
        this.loadParameterOverrides(overridesMap);
        
        // last run gets initialized after loading config and overrides
        // since config params are needed for initializing last run.
        initializeLastRun(getLastRunPrefix());
        
        // Properties initialization completed. Configure OAuth environment next
        setOAuthEnvironment(getString(OAUTH_ENVIRONMENT));
    }
    
    private String getLastRunPrefix() {
        String lastRunFilePrefix = getString(Config.PROCESS_NAME);
        if (lastRunFilePrefix == null || lastRunFilePrefix.isBlank()) {
            lastRunFilePrefix = getString(Config.ENTITY) + getString(Config.OPERATION);
        }
        if (lastRunFilePrefix == null || lastRunFilePrefix.isBlank()) {
            lastRunFilePrefix = RUN_MODE_UI_VAL;
        }
        return lastRunFilePrefix;
    }
    
    private void initializeLastRun(String lastRunFileNamePrefix) {
        if (lastRunFileNamePrefix == null || lastRunFileNamePrefix.isBlank()) {
            lastRunFileNamePrefix = getString(Config.CLI_OPTION_RUN_MODE);
        }
        String lastRunFileName = lastRunFileNamePrefix + LAST_RUN_FILE_SUFFIX;
        String lastRunDir = getString(Config.LAST_RUN_OUTPUT_DIR);
        if (lastRunDir == null || lastRunDir.length() == 0) {
            lastRunDir = this.configDir;
        }

        this.lastRun = new LastRun(lastRunFileName, lastRunDir, getBoolean(Config.ENABLE_LAST_RUN_OUTPUT));
        // Need to initialize last run date if it's present neither in config or override
        lastRun.setDefault(LastRun.LAST_RUN_DATE, getString(INITIAL_LAST_RUN_DATE));

        try {
            this.lastRun.load();
        } catch (IOException e) {
            logger.warn(Messages.getFormattedString("LastRun.errorLoading", new String[]{
                    this.lastRun.getFullPath(), e.getMessage()}), e);
        }        
    }

    private boolean useBulkApiByDefault() {
        return false;
    }

    /**
     * This sets the current defaults.
     */
    private void setDefaults() {
        setDefaultValue(HIDE_WELCOME_SCREEN, true);
        setDefaultValue(SHOW_LOADER_UPGRADE_SCREEN, true);

        setDefaultValue(CSV_DELIMITER_COMMA, true);
        setDefaultValue(CSV_DELIMITER_TAB, true);
        setDefaultValue(CSV_DELIMITER_OTHER, false);
        setDefaultValue(CSV_DELIMITER_OTHER_VALUE, "-");
        setDefaultValue(CSV_DELIMITER_FOR_QUERY_RESULTS, AppUtil.COMMA);

        setDefaultValue(ENDPOINT, DEFAULT_ENDPOINT_URL);
        setDefaultValue(LOAD_BATCH_SIZE, useBulkApiByDefault() ? DEFAULT_BULK_API_BATCH_SIZE : DEFAULT_LOAD_BATCH_SIZE);
        setDefaultValue(LOAD_ROW_TO_START_AT, 0);
        setDefaultValue(TIMEOUT_SECS, DEFAULT_TIMEOUT_SECS);
        setDefaultValue(CONNECTION_TIMEOUT_SECS, DEFAULT_CONNECTION_TIMEOUT_SECS);
        setDefaultValue(ENABLE_RETRIES, true);
        setDefaultValue(MAX_RETRIES, DEFAULT_MAX_RETRIES);
        setDefaultValue(MIN_RETRY_SLEEP_SECS, DEFAULT_MIN_RETRY_SECS);
        setDefaultValue(ASSIGNMENT_RULE, ""); //$NON-NLS-1$
        setDefaultValue(INSERT_NULLS, false);
        setDefaultValue(ENABLE_EXTRACT_STATUS_OUTPUT, false);
        setDefaultValue(ENABLE_LAST_RUN_OUTPUT, true);
        setDefaultValue(RESET_URL_ON_LOGIN, true);
        setDefaultValue(EXTRACT_REQUEST_SIZE, DEFAULT_EXTRACT_REQUEST_SIZE);
        setDefaultValue(SORT_EXTRACT_FIELDS, true);
        setDefaultValue(DAO_WRITE_BATCH_SIZE, DEFAULT_DAO_WRITE_BATCH_SIZE);
        setDefaultValue(DAO_READ_BATCH_SIZE, DEFAULT_DAO_READ_BATCH_SIZE);
        setDefaultValue(TRUNCATE_FIELDS, true);
        setDefaultValue(FORMAT_PHONE_FIELDS, false);
        // TODO: When we're ready, make Bulk API turned on by default.
        setDefaultValue(BULK_API_ENABLED, useBulkApiByDefault());
        setDefaultValue(BULK_API_SERIAL_MODE, false);
        setDefaultValue(BULK_API_ZIP_CONTENT, false);
        setDefaultValue(BULK_API_CHECK_STATUS_INTERVAL, DEFAULT_BULK_API_CHECK_STATUS_INTERVAL);
        setDefaultValue(WIRE_OUTPUT, false);
        setDefaultValue(DEBUG_MESSAGES, false);
        setDefaultValue(TIMEZONE, TimeZone.getDefault().getID());
        //sfdcInternal settings
        setDefaultValue(SFDC_INTERNAL, false);
        setDefaultValue(SFDC_INTERNAL_IS_SESSION_ID_LOGIN, false);
        setDefaultValue(SFDC_INTERNAL_SESSION_ID, (String) null);

        //oauth settings
        setDefaultValue(OAUTH_SERVER, DEFAULT_ENDPOINT_URL);
        setDefaultValue(OAUTH_REDIRECTURI, DEFAULT_ENDPOINT_URL);
        setDefaultValue(OAUTH_ENVIRONMENT, OAUTH_PROD_ENVIRONMENT_VAL);
        setDefaultValue(OAUTH_ENVIRONMENTS, OAUTH_PROD_ENVIRONMENT_VAL + AppUtil.COMMA + OAUTH_SB_ENVIRONMENT_VAL);

        /* sfdc.oauth.<env>.<bulk | partner>.clientid = DataLoaderBulkUI | DataLoaderPartnerUI */
        setDefaultValue(OAUTH_PREFIX + OAUTH_PROD_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_BULK_CLIENTID, OAUTH_BULK_CLIENTID_VAL);
        setDefaultValue(OAUTH_PREFIX + OAUTH_PROD_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_PARTNER_CLIENTID, OAUTH_PARTNER_CLIENTID_VAL);

        setDefaultValue(OAUTH_PREFIX + OAUTH_SB_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_BULK_CLIENTID, OAUTH_BULK_CLIENTID_VAL);
        setDefaultValue(OAUTH_PREFIX + OAUTH_SB_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_PARTNER_CLIENTID, OAUTH_PARTNER_CLIENTID_VAL);

        /* production server and redirecturi, sandbox server and redirecturi */
        setDefaultValue(OAUTH_PREFIX + OAUTH_PROD_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_SERVER, OAUTH_PROD_SERVER_VAL);
        setDefaultValue(OAUTH_PREFIX + OAUTH_PROD_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_REDIRECTURI, OAUTH_PROD_REDIRECTURI_VAL);

        setDefaultValue(OAUTH_PREFIX + OAUTH_SB_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_SERVER, OAUTH_SB_SERVER_VAL);
        setDefaultValue(OAUTH_PREFIX + OAUTH_SB_ENVIRONMENT_VAL + "." + OAUTH_PARTIAL_REDIRECTURI, OAUTH_SB_REDIRECTURI_VAL);
        setDefaultValue(REUSE_CLIENT_CONNECTION, true);
        /*
        setDefaultValue(ENABLE_BULK_QUERY_PK_CHUNKING, false);
        setDefaultValue(BULK_QUERY_PK_CHUNK_SIZE, DEFAULT_BULK_QUERY_PK_CHUNK_SIZE);
        setDefaultValue(BULK_QUERY_PK_CHUNK_START_ROW, "");
        */
        setDefaultValue(DUPLICATE_RULE_ALLOW_SAVE, false);
        setDefaultValue(DUPLICATE_RULE_INCLUDE_RECORD_DETAILS, false);
        setDefaultValue(DUPLICATE_RULE_RUN_AS_CURRENT_USER, false);
        setDefaultValue(BUFFER_UNPROCESSED_BULK_QUERY_RESULTS, false);
        setDefaultValue(BULKV2_API_ENABLED, false);
        setDefaultValue(OAUTH_LOGIN_FROM_BROWSER, true);
        setDefaultValue(LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT, true);
        setDefaultValue(Config.CLI_OPTION_RUN_MODE, Config.RUN_MODE_UI_VAL);
        setDefaultValue(SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV, false);
        setDefaultValue(PROCESS_BULK_CACHE_DATA_FROM_DAO, true);
        setDefaultValue(PROCESS_KEEP_ACCOUNT_TEAM, false);
        setDefaultValue(WIZARD_WIDTH, DEFAULT_WIZARD_WIDTH);
        setDefaultValue(WIZARD_HEIGHT, DEFAULT_WIZARD_HEIGHT);
        setDefaultValue(DAO_READ_PREPROCESSOR_SCRIPT, "");
        setDefaultValue(DAO_WRITE_POSTPROCESSOR_SCRIPT, "");
        setDefaultValue(LIMIT_OUTPUT_TO_QUERY_FIELDS, true);
        setDefaultValue(WIZARD_CLOSE_ON_FINISH, true);
        setDefaultValue(CACHE_DESCRIBE_GLOBAL_RESULTS, true);
        setDefaultValue(PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE, false);
        setDefaultValue(INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS, false);
    }

    /**
     * Returns true if the name is a key in the config
     *
     * @param name the name of the key
     */
    public boolean contains(String name) {
        return properties.containsKey(name) || lastRun.hasParameter(name) && lastRun.containsKey(name);
    }

    /**
     * Gets boolean for a given name
     *
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
     * @return double
     */

    public double getDouble(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return DOUBLE_DEFAULT;
        double ival;
        try {
            ival = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{name,
                    Double.class.getName()});
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets float for a given name.
     *
     * @return float
     */
    public float getFloat(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return FLOAT_DEFAULT;
        float ival = FLOAT_DEFAULT;
        try {
            ival = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{name,
                    Float.class.getName()});
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets int for a given name.
     *
     * @return int
     */
    public int getInt(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return INT_DEFAULT;
        int ival = 0;
        try {
            ival = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{name,
                    Integer.class.getName()});
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets long for a given name.
     *
     * @return long
     */
    public long getLong(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return LONG_DEFAULT;
        long ival = LONG_DEFAULT;
        try {
            ival = Long.parseLong(value);
        } catch (NumberFormatException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{name,
                    Long.class.getName()});
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return ival;
    }

    /**
     * Gets  required string param for a given name.  If not found, throws an exception
     *
     * @return string
     */
    public String getStringRequired(String name) throws ParameterLoadException {
        String value = getString(name);
        if (value == null || value.length() == 0) {
            String errMsg = Messages.getFormattedString("Config.errorNoRequiredParameter", name); //$NON-NLS-1$
            logger.fatal(errMsg);
            throw new ParameterLoadException(errMsg);
        }
        return value;
    }

    /**
     * Gets string for a given name.
     *
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
        if (values != null && !values.trim().isEmpty()) {
            Collections.addAll(list, values.trim().split(AppUtil.COMMA));
        }
        return list;
    }

    /**
     * Gets an enum value for a given config name.
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
     * @param configFileProperty property containing a config filename
     * @return Config filename path based on config property value. Config file is assumed to reside
     * in the global config folder
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
     * Constructs config file path based on the configuration folder and the passed in config
     * filename
     *
     * @param configFilename Config filename that resides in the config folder
     * @return Full path to the config file
     */
    public String constructConfigFilePath(String configFilename) {
        File configPathFile = new File(this.filename).getParentFile();
        return new File(configPathFile, configFilename).getAbsolutePath();
    }

    /**
     * @return Date
     */
    public Date getDate(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return Calendar.getInstance().getTime();
        Date dval = null;
        try {
            dval = DATE_FORMATTER.parse(value);
        } catch (ParseException e) {
            String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{name,
                    Date.class.getName()});
            logger.warn(errMsg, e);
            throw new ParameterLoadException(e.getMessage(), e);
        }
        return dval;
    }

    /**
     * Get map from a string param value. String format of map is key1=val1,key2=val2,...
     *
     * @return Map
     */
    public Map<String, String> getMap(String name) throws ParameterLoadException {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return MAP_STRING_DEFAULT;
        Map<String, String> mval = new HashMap<String, String>();
        String[] pairs = value.split(AppUtil.COMMA);
        for (String pair : pairs) {
            String[] nameValue = pair.split("=");
            if (nameValue.length != 2) {
                String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{name,
                        Map.class.getName()});
                logger.warn(errMsg);
                throw new ParameterLoadException(errMsg);
            }
            mval.put(nameValue[0], nameValue[1]);
        }
        return mval;
    }

    /**
     * @return parameter value
     */
    
    private String getParamValue(String name) {
        String propValue;

        if (lastRun != null && lastRun.hasParameter(name)) {
            propValue = lastRun.getProperty(name);
        } else {
            propValue = properties != null ? properties.getProperty(name) : null;
        }
        
        // check if a property's value is configured when it used to be a pilot property
        // if value not set.
        if (propValue == null && !name.startsWith(PILOT_PROPERTY_PREFIX)) { 
            String pilotName = PILOT_PROPERTY_PREFIX + name;
            String pilotValue = getParamValue(pilotName);
            if (pilotValue != null && !pilotValue.isEmpty()) {
                // if picking up the value from a pilot property that is no longer in pilot,
                // set the value for the new property to be the same as the value of the pilot property
                doSetPropertyAndUpdateConfig(name, propValue, pilotValue);
            }
            propValue = pilotValue;
        }
        
        return propValue;
    }

    /**
     * Prints the contents of this preference store to the given print stream.
     *
     * @param out the print stream
     */
    public void list(PrintStream out) {
        properties.list(out);
        lastRun.list(out);
    }

    /**
     * Prints the contents of this preference store to the given print writer.
     *
     * @param out the print writer
     */
    public void list(PrintWriter out) {
        properties.list(out);
        lastRun.list(out);
    }

    /**
     * Loads this preference store from the file established in the constructor
     * <code>Config(java.lang.String)</code> (or by <code>setFileName</code>). Default preference
     * values are not affected.
     *
     * @throws java.io.IOException if there is a problem loading this store
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
     * Loads this preference store from the given input stream. Default preference values are not
     * affected.
     *
     * @param in the input stream
     * @throws ConfigInitializationException If there's a problem loading the parameters
     * @throws IOException                   IF there's an I/O problem loading parameter from file
     */
    private void load(InputStream in) throws ConfigInitializationException, IOException {
        try {
            properties.load(in);
        } catch (IOException e) {
            logger.fatal(Messages.getFormattedString("Config.errorPropertiesLoad", e.getMessage()));
            throw e;
        }
        // paramter post-processing
        postLoad(properties, true);

        dirty = false;
    }
    
    private void restoreReadOnlyProperty(String propertyName) {
        String preservedPropertyValue = (String)this.readOnlyProperties.get(propertyName);
        if (preservedPropertyValue == null) {
            this.properties.remove(propertyName);
            return;
        }
        this.properties.put(propertyName, preservedPropertyValue);
    }
    
    private void preserveReadOnlyProperty(Map<?, ?> propMap, String propertyName) {
        if (this.readOnlyProperties == null) {
            this.readOnlyProperties = new Properties();
        }
        String propertyValueToPreserve = (String) propMap.get(propertyName);
        if (propertyValueToPreserve == null) {
            return;
        }
        this.readOnlyProperties.put(propertyName, propertyValueToPreserve);
    }

    /**
     * Post process parameters. Right now, only decrypts encrypted values in the map
     *
     * @param values Values to be post-processed
     */
    @SuppressWarnings("unchecked")
    private void postLoad(Map<?, ?> propMap, boolean isConfigFilePropsMap) throws ConfigInitializationException {

        if (isConfigFilePropsMap) {
            for (String propertyName : READ_ONLY_PROPERTY_NAMES) {
                preserveReadOnlyProperty(propMap, propertyName);
            }
        }
        // initialize encryption
        initEncryption((Map<String, String>) propMap);

        // decrypt encrypted values
        decryptPasswordProperty(propMap, PASSWORD);
        decryptPasswordProperty(propMap, PROXY_PASSWORD);
        decryptPasswordProperty(propMap, OAUTH_ACCESSTOKEN);
        decryptPasswordProperty(propMap, OAUTH_REFRESHTOKEN);
        
        // Do not load unsupported properties and CLI options even if they are specified in config.properties file
        if (isConfigFilePropsMap) {
            removeCLIOptionsFromProperties();
            removeUnsupportedProperties();
        }
    }

    private void decryptPasswordProperty(Map<?, ?> values, String propertyName) throws ConfigInitializationException {
        @SuppressWarnings("unchecked")
        Map<String, String> propMap = (Map<String, String>)values;
        // initialize encryption
        if (propMap != null && propMap.containsKey(propertyName)) {
            if (propMap.containsKey(propertyName + DECRYPTED_SUFFIX)) {
                String decryptedPropValue = propMap.get(propertyName + DECRYPTED_SUFFIX);
                String propValueToBeDecrypted = propMap.get(propertyName);
                if (decryptedPropValue != null 
                        && propValueToBeDecrypted != null
                        && decryptedPropValue.equals(propValueToBeDecrypted)) {
                    return; // do not decrypt an already decrypted value
                }
            }
            String propValue = decryptProperty(encrypter, propMap, propertyName, isBatchMode());
            if (propValue == null) propValue = "";
            propMap.put(propertyName, propValue);
            
            // cache decrypted value
            propMap.put(propertyName + DECRYPTED_SUFFIX, propValue);
        }
    }

    /**
     * Load config parameter override values. The main use case is loading of overrides from
     * external config file
     */
    public void loadParameterOverrides(Map<String, String> configOverrideMap) throws ParameterLoadException,
            ConfigInitializationException {
        this.parameterOverridesMap = configOverrideMap;
        // make sure to post-process the args to be loaded
        postLoad(configOverrideMap, false);

        // replace values in the Config
        putValue(configOverrideMap);
    }

    /**
     * Decrypt property with propName using the encrypter. If decryption succeeds, return the
     * decrypted value
     *
     * @return decrypted property value
     */
    static private String decryptProperty(EncryptionAesUtil encrypter, Map<String, String> propMap, String propName, boolean isBatch)
            throws ParameterLoadException {
        String propValue = propMap.get(propName);
        if (propValue != null && propValue.length() > 0) {
            try {
                return encrypter.decryptMsg(propValue);
            } catch (GeneralSecurityException e) {
                // if running in the UI, we can ignore encryption errors
                if (isBatch) {
                    String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{propName,
                            String.class.getName()});
                    logger.error(errMsg, e);
                    throw new ParameterLoadException(errMsg, e);
                } else {
                    return null;
                }
            } catch (Exception e) {
                String errMsg = Messages.getFormattedString("Config.errorParameterLoad", new String[]{propName,
                        String.class.getName()});
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
        if (values == null) {
            return;
        }
        // initialize encrypter
        String keyFile = values.get(ENCRYPTION_KEY_FILE);
        if (keyFile != null && keyFile.length() != 0) {
            try {
                encrypter.setCipherKeyFromFilePath(keyFile);
            } catch (Exception e) {
                String errMsg = Messages.getFormattedString("Config.errorSecurityInit", new String[]{keyFile,
                        e.getMessage()});
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
            list.add((String) en.nextElement());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Puts a set of values from a map into config
     *
     * @param values Map of overriding values
     */
    public void putValue(Map<String, String> values) throws ParameterLoadException, ConfigInitializationException {
        if (values == null) {
            return;
        }
        for (String key : values.keySet()) {
            putValue(key, values.get(key));
        }
    }

    /**
     * Puts a value into the config
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
     * @throws java.io.IOException if there is a problem saving this store
     */
    public void save() throws IOException, GeneralSecurityException {
        if (getString(Config.CLI_OPTION_RUN_MODE).equalsIgnoreCase(Config.RUN_MODE_BATCH_VAL)
           || getBoolean(READ_ONLY_CONFIG_PROPERTIES)) {
            return; // do not save any updates to config.properties file
        }
        if (filename == null) {
            throw new IOException(Messages.getString("Config.fileMissing")); //$NON-NLS-1$
        }

        Properties inMemoryProperties = new Properties();
        inMemoryProperties.putAll(this.properties);
        
        // do not save properties set through parameter overrides
        if (this.parameterOverridesMap != null) {
            for (String propertyName : this.parameterOverridesMap.keySet()) {
                this.properties.remove(propertyName);
            }
        }
        // keep the property values for the read-only properties as retrieved from config.properties
        for (String propertyName : READ_ONLY_PROPERTY_NAMES) {
            this.restoreReadOnlyProperty(propertyName);
        }
        
        removeUnsupportedProperties();
        removeDecryptedProperties();
        removeCLIOptionsFromProperties();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            save(out, "Loader Config"); //$NON-NLS-1$
        } finally {
            if (out != null) {
                out.close();
            }
            // restore original property values
            properties = inMemoryProperties;
        }
        // save last run statistics
        lastRun.save();
    }

    private void removeUnsupportedProperties() {
        // do not save a value for enabling Bulk V2 
        //this.properties.remove(BULKV2_API_ENABLED);
    }
    
    private void removeDecryptedProperties() {
        this.properties.remove(PASSWORD + DECRYPTED_SUFFIX);
        this.properties.remove(PROXY_PASSWORD + DECRYPTED_SUFFIX);
        this.properties.remove(OAUTH_ACCESSTOKEN + DECRYPTED_SUFFIX);
        this.properties.remove(OAUTH_REFRESHTOKEN + DECRYPTED_SUFFIX);
    }
    
    private void removeCLIOptionsFromProperties() {
        Set<String> keys = this.properties.stringPropertyNames();
        Field[] allFields = Config.class.getDeclaredFields();
        for (Field field : allFields) {
            if (field.getName().startsWith("CLI_OPTION_")) {
                String fieldVal = null;
                try {
                    fieldVal = (String)field.get(null);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (fieldVal == null) {
                    continue;
                }
                for (String key : keys) {
                    if (key.equalsIgnoreCase(fieldVal)) {
                        this.properties.remove(key);
                    }
                }
            }
        }
    }
    
    /**
     * Save statistics from the last run
     */
    public void saveLastRun() throws IOException {
        lastRun.save();
    }

    /**
     * Saves this config to the given output stream. The given string is inserted as header
     * information.
     *
     * @param out    the output stream
     * @param header the header
     * @throws java.io.IOException if there is a problem saving this store
     */
    private void save(OutputStream out, String header) throws IOException {
        properties.store(out, header);
        dirty = false;
    }

    public void setValue(String name, Map<String, String> valueMap) {
        StringBuilder sb = new StringBuilder();
        for (String key : valueMap.keySet()) {
            // add comma for subsequent entries
            if (sb.length() != 0) {
                sb.append(AppUtil.COMMA);
            }
            sb.append(key + "=" + valueMap.get(key));
        }
        putValue(name, sb.toString());
    }

    /**
     * Sets the name of the file used when loading and storing this config
     *
     * @param name the file name
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

    /*
     * Sets a date
     */
    public void setValue(String name, Date value) {
        setValue(name, value, false);
    }
    
    private void setValue(String name, Date value, boolean skipIfAlreadySet) {
        setProperty(name, DATE_FORMATTER.format(value), skipIfAlreadySet);
    }

    /**
     * Sets a list of String values
     */
    public void setValue(String name, String... values) {
        setValue(name, false, values);
    }
    
    private void setValue(String name,  boolean skipIfAlreadySet, String... values) {
        if (values != null && values.length > 1) {
            StringJoiner joiner = new StringJoiner(AppUtil.COMMA);
            for (String value : values) {
                joiner.add(value);
            }
            setProperty(name, joiner.toString(), skipIfAlreadySet);
        } else if (values != null && values.length > 0) {
            setProperty(name, values[0], skipIfAlreadySet);
        } else {
            setProperty(name, null, skipIfAlreadySet);
        }

    }

    /**
     * Sets a value other than a date or a list of String values
     */
    public <T> void setValue(String name, T value) {
        setValue(name, value, false);
    }

    
    private <T> void setDefaultValue(String name, T value) {
        setValue(name, value, true);
    }
    
    private <T> void setValue(String name, T value, boolean skipIfAlreadySet) {
        if (value != null) {
            setProperty(name, value.toString(), skipIfAlreadySet);
        }
    }
    
    /**
     * @param name
     * @param newValue
     */
    private void setProperty(String name, String newValue, boolean skipIfAlreadySet) {
        final String oldValue = getString(name);
        if (skipIfAlreadySet && oldValue != null && !oldValue.isEmpty()) {
            // do not override the old value
            return;
        }
        final boolean paramChanged = (oldValue == null || oldValue.length() == 0) ? (newValue != null && newValue
                .length() > 0) : !oldValue.equals(newValue);
        if (paramChanged) {
            doSetPropertyAndUpdateConfig(name, oldValue, newValue);
        }
    }
    
    private void doSetPropertyAndUpdateConfig(String name, String oldValue, String newValue) {
        this.dirty = true;
        configChanged(name, oldValue, newValue);
        if (lastRun != null && lastRun.hasParameter(name)) {
            lastRun.put(name, newValue);
        } else {
            properties.put(name, newValue);
        }
    }

    public boolean isBatchMode() {
        return (Config.RUN_MODE_BATCH_VAL.equalsIgnoreCase(getString(Config.CLI_OPTION_RUN_MODE)));
    }

    public int getLoadBatchSize() {
        boolean bulkApi = isBulkAPIEnabled();
        boolean bulkV2Api = this.isBulkV2APIEnabled();
        
        if (bulkApi && bulkV2Api) {
            return MAX_BULKV2_API_JOB_SIZE;
        }
        
        int bs = -1;
        try {
            bs = getInt(LOAD_BATCH_SIZE);
        } catch (ParameterLoadException e) {
        }
        int maxBatchSize = bulkApi ? MAX_BULK_API_BATCH_SIZE : MAX_LOAD_BATCH_SIZE;
        return bs > maxBatchSize ? maxBatchSize : bs > 0 ? bs : getDefaultBatchSize(bulkApi, bulkV2Api);
    }

    public int getDefaultBatchSize(boolean bulkApi, boolean bulkV2Api) {
        if (bulkApi && bulkV2Api) {
            return MAX_BULKV2_API_JOB_SIZE;
        }
        return bulkApi ? DEFAULT_BULK_API_BATCH_SIZE : DEFAULT_LOAD_BATCH_SIZE;
    }

    public boolean useBulkAPIForCurrentOperation() {
        return isBulkAPIEnabled() && isBulkApiOperation();
    }

    public boolean isBulkAPIEnabled() {
        return getBoolean(BULK_API_ENABLED);
    }
    
    public boolean isBulkV2APIEnabled() {
        return isBulkAPIEnabled() && getBoolean(BULKV2_API_ENABLED);
    }

    private boolean isBulkApiOperation() {
        return getOperationInfo().bulkAPIEnabled();
    }

    public OperationInfo getOperationInfo() {
        return getEnum(OperationInfo.class, OPERATION);
    }

    public String getCsvEncoding(boolean isWrite) {
        // charset is for CSV read unless isWrite is set to true
        String configProperty = READ_UTF8;
        if (isWrite) {
            configProperty = WRITE_UTF8;
            logger.debug("Getting charset for writing to CSV");
        } else {
            logger.debug("Getting charset for reading from CSV");
        }
        if (getBoolean(configProperty)) {
            logger.debug("Using UTF8 charset because '" 
                    +  configProperty
                    +"' is set to true");
            return StandardCharsets.UTF_8.name();
        }
        if (!isWrite) {
            String charset = getString(READ_CHARSET);
            if (charset != null && !charset.isEmpty()) {
                return charset;
            }
        }
        String charset = getDefaultCharsetForCsvReadWrite();
        logger.debug("Using charset " + charset);
        return charset;
    }
    
    private static String defaultCharsetForCsvReadWrite = null;
    private synchronized static String getDefaultCharsetForCsvReadWrite() {
        if (defaultCharsetForCsvReadWrite != null) {
            return defaultCharsetForCsvReadWrite;
        }
        String fileEncodingStr = System.getProperty("file.encoding");
        if (fileEncodingStr != null && !fileEncodingStr.isBlank()) {
            for (String charsetName : Charset.availableCharsets().keySet()) {
                if (fileEncodingStr.equalsIgnoreCase(charsetName)) {
                    logger.debug("Setting the default charset for CSV read and write to the value of file.encoding system property: " + fileEncodingStr);
                    defaultCharsetForCsvReadWrite = charsetName; 
                    return charsetName;
                }
            }
            logger.debug("Unable to find the charset '"
                    + fileEncodingStr
                    + "' specified in file.encoding system property among available charsets for the Java VM." );
        }
        logger.debug("Using JVM default charset as the default charset for CSV read and write : " + Charset.defaultCharset().name());
        defaultCharsetForCsvReadWrite = Charset.defaultCharset().name();
        return defaultCharsetForCsvReadWrite;
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

    public String getOAuthEnvironmentString(String environmentName, String name) {
        return getString(OAUTH_PREFIX + environmentName + "." + name);
    }

    public void setOAuthEnvironmentString(String environmentName, String name, String... values) {
        setValue(OAUTH_PREFIX + environmentName + "." + name, values);
    }

    public void setOAuthEnvironment(String environment) {
        String clientId;
        if (environment == null || environment.isBlank()) {
            environment = OAUTH_PROD_ENVIRONMENT_VAL;
        }
        String[] envArray = getString(OAUTH_ENVIRONMENTS).split(",");
        boolean isEnvMatch = false;
        for (String env : envArray) {
            env = env.strip();
            if (env.equalsIgnoreCase(environment)) {
                isEnvMatch = true;
            }
        }
        if (!isEnvMatch) {
            environment = OAUTH_PROD_ENVIRONMENT_VAL;
        }
        if (getBoolean(BULK_API_ENABLED)) {
            clientId = getOAuthEnvironmentString(environment, OAUTH_PARTIAL_BULK_CLIENTID);
        } else {
            clientId = getOAuthEnvironmentString(environment, OAUTH_PARTIAL_PARTNER_CLIENTID);
        }
        if (clientId == null || clientId.isEmpty()) {
            clientId = getOAuthEnvironmentString(environment, OAUTH_PARTIAL_CLIENTID);
        }
        setValue(OAUTH_ENVIRONMENT, environment);
        setValue(OAUTH_SERVER, getOAuthEnvironmentString(environment, OAUTH_PARTIAL_SERVER));
        setValue(OAUTH_CLIENTID, clientId);
        setValue(OAUTH_CLIENTSECRET, getOAuthEnvironmentString(environment, OAUTH_PARTIAL_CLIENTSECRET));
        setValue(OAUTH_REDIRECTURI, getOAuthEnvironmentString(environment, OAUTH_PARTIAL_REDIRECTURI));
    }
    
    /**
     * Create folder provided from the parameter
     *
     * @param dirPath - folder to be created
     * @return True if folder was created successfully or folder already existed False if
     * folder was failed to create
     */
    private static boolean createDir(File dirPath) {
        boolean isSuccessful = true;
        if (!dirPath.exists() || !dirPath.isDirectory()) {
            isSuccessful = dirPath.mkdirs();
            if (isSuccessful) {
                logger.info("Created config folder: " + dirPath);
            } else {
                logger.info("Unable to create config folder: " + dirPath);
            }
        } else {
            logger.info("Config folder already exists: " + dirPath);
        }
        return isSuccessful;
    }

    /**
     * Get the current config.properties and load it into the config bean.
     * @throws ConfigInitializationException 
     * @throws IOException 
     * @throws FactoryConfigurationError 
     */
    public static synchronized Config getInstance(Map<String, String> argMap) throws ConfigInitializationException, FactoryConfigurationError, IOException { 
        AppUtil.initializeAppConfig(AppUtil.convertCommandArgsMapToArgsArray(argMap));
        logger = LogManager.getLogger(Config.class);

        String configurationsDirPath = AppUtil.getConfigurationsDir();
        File configurationsDir;
        final String DEFAULT_CONFIG_FILE = "defaultConfig.properties"; //$NON-NLS-1$

        configurationsDir = new File(configurationsDirPath);
 
        // Create dir if it doesn't exist
        boolean isMkdirSuccessfulOrExisting = createDir(configurationsDir);
        if (!isMkdirSuccessfulOrExisting) {
            String errorMsg = Messages.getMessage(Config.class, "errorCreatingOutputDir", configurationsDirPath);
            logger.error(errorMsg);
            throw new ConfigInitializationException(errorMsg);
        }

        // check if the config file exists
        File configFile = new File(configurationsDir.getAbsolutePath(), CONFIG_FILE);

        String configFilePath = configFile.getAbsolutePath();
        logger.info("Looking for file in config path: " + configFilePath);
        if (!configFile.exists()) {

            File defaultConfigFile = new File(configurationsDir, DEFAULT_CONFIG_FILE);
            logger.debug("Looking for file in config file " + defaultConfigFile.getAbsolutePath());
            // If default config exists, copy the default to user config
            // If doesn't exist, create a blank user config

            if (defaultConfigFile.exists()) {
                try {
                    // Copy default config to user config
                    logger.info(String.format("User config file does not exist in '%s' Default config file is copied from '%s'",
                            configFilePath, defaultConfigFile.getAbsolutePath()));
                    Files.copy(defaultConfigFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    String errorMsg = String.format("Failed to copy '%s' to '%s'", defaultConfigFile.getAbsolutePath(), configFile);
                    logger.warn(errorMsg, e);
                    throw new ConfigInitializationException(errorMsg, e);
                }
            } else {
                // extract from the jar
                try {
                    AppUtil.extractFromJar("/" + CONFIG_FILE, configFile);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.error("Unable to extract " + CONFIG_FILE + " from jar " + e.getMessage());
                }
            }
            configFile.setWritable(true);
            configFile.setReadable(true);
        } else {
            logger.info("User config is found in " + configFile.getAbsolutePath());
        }

        Config config = null;
        try {
            config = new Config(configFilePath, argMap);
            currentConfig = config;
            logger.info(Messages.getMessage(Config.class, "configInit")); //$NON-NLS-1$
        } catch (IOException | ProcessInitializationException e) {
            throw new ConfigInitializationException(Messages.getMessage(Config.class, "errorConfigLoad", configFilePath), e);
        }
        return config;
    }
    
    private static Config currentConfig = null;
    public static Config getCurrentConfig() {
        return currentConfig;
    }
    
    public static interface ConfigListener {
        void configValueChanged(String key, String oldValue, String newValue);
    }

}
