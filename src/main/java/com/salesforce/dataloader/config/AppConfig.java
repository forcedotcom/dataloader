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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TimeZone;

import javax.xml.parsers.FactoryConfigurationError;

import com.salesforce.dataloader.util.DLLogManager;
import com.salesforce.dataloader.util.LoggingUtil;

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
public class AppConfig {
    private static Logger logger = DLLogManager.getLogger(AppConfig.class);
    private static final String DECRYPTED_SUFFIX = ".decrypted";

    /**
     * Default values for specific parameters
     */
    public static final int DEFAULT_EXPORT_BATCH_SIZE = 500;
    public static final int MAX_EXPORT_BATCH_SIZE = 2000;
    public static final int MIN_EXPORT_BATCH_SIZE = 200;
    public static final int DEFAULT_MIN_RETRY_SECS = 2;
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int MAX_RETRIES_LIMIT = 10;
    public static final int DEFAULT_CONNECTION_TIMEOUT_SECS = 60;
    public static final int DEFAULT_TIMEOUT_SECS = 540;
    public static final int DEFAULT_NUM_ROWS_LOAD_BATCH = 200;
    public static final int DEFAULT_DAO_WRITE_BATCH_SIZE = 500;
    public static final int DEFAULT_DAO_READ_BATCH_SIZE = 200;
    public static final int MAX_NUM_ROWS_SOAP_API_IMPORT_BATCH = 200;
    public static final int MAX_DAO_READ_BATCH_SIZE = 200;
    public static final int MAX_DAO_WRITE_BATCH_SIZE = 2000;
    public static final int MAX_SOAP_API_IMPORT_BATCH_BYTES = 50000000;
    public static final int MAX_REST_API_IMPORT_BATCH_BYTES = 50000000;

    // Bulk v1 and v2 limits specified at https://developer.salesforce.com/docs/atlas.en-us.salesforce_app_limits_cheatsheet.meta/salesforce_app_limits_cheatsheet/salesforce_app_limits_platform_bulkapi.htm
    public static final int MAX_BULK_API_IMPORT_BATCH_BYTES = 10000000;
    public static final int MAX_NUM_ROWS_BULK_API_IMPORT_BATCH = 10000;
    public static final int DEFAULT_MAX_BULKV2_API_IMPORT_JOB_BYTES = 150000000;
    private static int currentMaxBulkv2APIImportJobBytes = DEFAULT_MAX_BULKV2_API_IMPORT_JOB_BYTES;
    public static final int MAX_NUM_ROWS_BULKV2_API_IMPORT_JOB = 150000000;
    public static final int DEFAULT_NUM_ROWS_BULK_API_IMPORT_BATCH = 2000;
    
    public static final long DEFAULT_BULK_API_CHECK_STATUS_INTERVAL = 5000L;
    public static final String DEFAULT_ENDPOINT_URL_PROD = "https://login.salesforce.com/";
    public static final String DEFAULT_ENDPOINT_URL_SANDBOX = "https://test.salesforce.com/";
    public static final String LIGHTNING_ENDPOINT_URL_PART_VAL = "lightning.force.com";
    public static final String MYSF_ENDPOINT_URL_PART_VAL = "mysalesforce.com";
    public static final String SERVER_PROD_ENVIRONMENT_VAL = "Production";
    public static final String SERVER_SB_ENVIRONMENT_VAL = "Sandbox";

    public static final String OAUTH_PROD_REDIRECTURI_VAL = "https://login.salesforce.com/services/oauth2/success";
    public static final String OAUTH_SB_REDIRECTURI_VAL = "https://test.salesforce.com/services/oauth2/success";

    public static final String BULK_CLIENTID_VAL = "DataLoaderBulkUI/";
    public static final String PARTNER_CLIENTID_VAL = "DataLoaderPartnerUI/";
    
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
    private Properties loadedProperties = new LinkedProperties();
    private Properties readOnlyPropertiesFromPropertiesFile = new LinkedProperties();
    private final Properties defaultProperties;
    private final boolean saveAllProps;
    private Map<String,ConfigPropertyMetadata> configPropsMetadataMap = ConfigPropertyMetadata.getPropertiesMap();
    
    private Map<String, String> overridesMap;

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

    //
    // Configurable properties that can only be specified as CLI options
    //
    public static final String CLI_OPTION_INSTALLATION_CREATE_MACOS_APPS_FOLDER_SHORTCUT_PROP = "salesforce.installation.shortcut.macos.appsfolder";
    public static final String CLI_OPTION_INSTALLATION_CREATE_WINDOWS_START_MENU_SHORTCUT_PROP = "salesforce.installation.shortcut.windows.startmenu";
    public static final String CLI_OPTION_INSTALLATION_CREATE_DESKTOP_SHORTCUT_PROP = "salesforce.installation.shortcut.desktop";
    public static final String CLI_OPTION_INSTALLATION_FOLDER_PROP = "salesforce.installation.dir";
    public static final String CLI_OPTION_RUN_MODE = "run.mode";
    public static final String CLI_OPTION_CONFIG_DIR_PROP = "salesforce.config.dir";

    // following command line options are set in UI mode when invoking java runtime
    // for the 2nd iteration
    public static final String CLI_OPTION_SYSTEM_PROXY_PORT = "sfdc.system.proxyPort";
    public static final String CLI_OPTION_SYSTEM_PROXY_HOST = "sfdc.system.proxyHost";
    public static final String CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH = "swt.nativelib.inpath";

    // =======================
    // Configurable properties specified in config.properties file
    // =======================
    // Loader Preferences
    public static final String PROP_SAVE_ALL_PROPS = "salesforce.saveAllSettings";
    public static final String PROP_GMT_FOR_DATE_FIELD_VALUE = "datefield.usegmt";
    public static final String PROP_HIDE_WELCOME_SCREEN = "loader.hideWelcome";
    public static final String PROP_SHOW_LOADER_UPGRADE_SCREEN = "loader.ui.showUpgrade";

    // Delimiter settings
    public static final String PROP_CSV_DELIMITER_COMMA = "loader.csvComma";
    public static final String PROP_CSV_DELIMITER_TAB = "loader.csvTab";
    public static final String PROP_CSV_DELIMITER_OTHER = "loader.csvOther";
    public static final String PROP_CSV_DELIMITER_OTHER_VALUE = "loader.csvOtherValue";
    public static final String PROP_CSV_DELIMITER_FOR_QUERY_RESULTS = "loader.query.delimiter";
    public static final String PROP_BUFFER_UNPROCESSED_BULK_QUERY_RESULTS = "loader.bufferUnprocessedBulkQueryResults";
    public static final String PROP_INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS = "loader.query.includeBinaryData";
    public static final String PROP_CACHE_DESCRIBE_GLOBAL_RESULTS = "loader.cacheSObjectNamesAndFields";
    
    //Special Internal Configs
    public static final String PROP_SFDC_INTERNAL = "sfdcInternal"; //$NON-NLS-1$
    public static final String PROP_SFDC_INTERNAL_IS_SESSION_ID_LOGIN = "sfdcInternal.isSessionIdLogin"; //$NON-NLS-1$
    public static final String PROP_SFDC_INTERNAL_SESSION_ID = "sfdcInternal.sessionId"; //$NON-NLS-1$

    // salesforce client connectivity
    public static final String PROP_USERNAME = "sfdc.username"; //$NON-NLS-1$
    public static final String PROP_PASSWORD = "sfdc.password"; //$NON-NLS-1$
    public static final String PROP_AUTH_ENDPOINT_LEGACY = "sfdc.endpoint";
    public static final String PROP_AUTH_ENDPOINT_PROD = "sfdc.endpoint." + SERVER_PROD_ENVIRONMENT_VAL; //$NON-NLS-1$
    public static final String PROP_AUTH_ENDPOINT_SANDBOX = "sfdc.endpoint." + SERVER_SB_ENVIRONMENT_VAL; //$NON-NLS-1$
    public static final String PROP_PROXY_HOST = "sfdc.proxyHost"; //$NON-NLS-1$
    public static final String PROP_PROXY_PORT = "sfdc.proxyPort"; //$NON-NLS-1$
    public static final String PROP_PROXY_USERNAME = "sfdc.proxyUsername"; //$NON-NLS-1$
    public static final String PROP_PROXY_PASSWORD = "sfdc.proxyPassword"; //$NON-NLS-1$
    public static final String PROP_PROXY_NTLM_DOMAIN = "sfdc.proxyNtlmDomain"; //$NON-NLS-1$
    public static final String PROP_TIMEOUT_SECS = "sfdc.timeoutSecs"; //$NON-NLS-1$
    public static final String PROP_CONNECTION_TIMEOUT_SECS = "sfdc.connectionTimeoutSecs"; //$NON-NLS-1$
    public static final String PROP_NO_COMPRESSION = "sfdc.noCompression"; //$NON-NLS-1$
    public static final String PROP_ENABLE_RETRIES = "sfdc.enableRetries"; //$NON-NLS-1$
    public static final String PROP_MAX_RETRIES = "sfdc.maxRetries"; //$NON-NLS-1$
    public static final String PROP_MIN_RETRY_SLEEP_SECS = "sfdc.minRetrySleepSecs"; //$NON-NLS-1$
    public static final String PROP_DEBUG_MESSAGES = "sfdc.debugMessages"; //$NON-NLS-1$
    public static final String PROP_DEBUG_MESSAGES_FILE = "sfdc.debugMessagesFile"; //$NON-NLS-1$
    public static final String PROP_RESET_URL_ON_LOGIN = "sfdc.resetUrlOnLogin"; //$NON-NLS-1$
    public static final String PROP_TRUNCATE_FIELDS = "sfdc.truncateFields";//$NON-NLS-1$
    public static final String PROP_FORMAT_PHONE_FIELDS = "sfdc.formatPhoneFields";//$NON-NLS-1$

    public static final String PROP_BULK_API_ENABLED = "sfdc.useBulkApi";
    public static final String PROP_BULK_API_SERIAL_MODE = "sfdc.bulkApiSerialMode";
    public static final String PROP_BULK_API_CHECK_STATUS_INTERVAL = "sfdc.bulkApiCheckStatusInterval";
    public static final String PROP_BULK_API_ZIP_CONTENT = "sfdc.bulkApiZipContent";
    public static final String PROP_BULKV2_API_ENABLED = "sfdc.useBulkV2Api";
    public static final String PROP_UPDATE_WITH_EXTERNALID = "sfdc.updateWithExternalId";
    public static final String PROP_DELETE_WITH_EXTERNALID = "sfdc.deleteWithExternalId";

    public static final String PROP_WIRE_OUTPUT = "sfdc.wireOutput";
    public static final String PROP_TIMEZONE = "sfdc.timezone";

    public static final String OAUTH_PREFIX = "sfdc.oauth.";
    public static final String BULK_LITERAL = "bulk";
    public static final String PARTNER_LITERAL = "partner";
    public static final String CLIENTSECRET_LITERAL = "clientsecret";
    public static final String CLIENTID_LITERAL = "clientid";
    public static final String REDIRECTURI_LITERAL = "redirecturi";
    public static final String BULK_CLIENTID_LITERAL = BULK_LITERAL + "." + CLIENTID_LITERAL;
    public static final String PARTNER_CLIENTID_LITERAL = PARTNER_LITERAL + "." + CLIENTID_LITERAL;

    public static final String PROP_SERVER_ENVIRONMENTS = OAUTH_PREFIX + "environments";
    public static final String PROP_SELECTED_SERVER_ENVIRONMENT = OAUTH_PREFIX + "environment";
    public static final String PROP_OAUTH_ACCESSTOKEN = OAUTH_PREFIX + "accesstoken";
    public static final String PROP_OAUTH_REFRESHTOKEN = OAUTH_PREFIX + "refreshtoken";
    public static final String PROP_OAUTH_LOGIN_FROM_BROWSER = OAUTH_PREFIX + "loginfrombrowser";
    public static final String PROP_CLIENTID_PROD_PARTNER = OAUTH_PREFIX 
            + SERVER_PROD_ENVIRONMENT_VAL 
            + "." +  PARTNER_CLIENTID_LITERAL;
    public static final String PROP_CLIENTID_PROD_BULK = OAUTH_PREFIX 
            + SERVER_PROD_ENVIRONMENT_VAL 
            + "." +  BULK_CLIENTID_LITERAL;
    public static final String PROP_CLIENTID_SANDBOX_PARTNER = OAUTH_PREFIX 
            + SERVER_SB_ENVIRONMENT_VAL 
            + "." +  PARTNER_CLIENTID_LITERAL;
    public static final String PROP_CLIENTID_SANDBOX_BULK = OAUTH_PREFIX 
            + SERVER_SB_ENVIRONMENT_VAL 
            + "." +  BULK_CLIENTID_LITERAL;
    public static final String PROP_OAUTH_CLIENTSECRET_PROD = OAUTH_PREFIX 
            + SERVER_PROD_ENVIRONMENT_VAL 
            + "." +  CLIENTSECRET_LITERAL;
    public static final String PROP_OAUTH_CLIENTSECRET_SB = OAUTH_PREFIX 
            + SERVER_SB_ENVIRONMENT_VAL 
            + "." +  CLIENTSECRET_LITERAL;
    public static final String PROP_OAUTH_REDIRECT_URI_PROD = OAUTH_PREFIX 
            + SERVER_PROD_ENVIRONMENT_VAL 
            + "." +  REDIRECTURI_LITERAL;
    public static final String PROP_OAUTH_REDIRECT_URI_SB = OAUTH_PREFIX 
            + SERVER_SB_ENVIRONMENT_VAL 
            + "." +  REDIRECTURI_LITERAL;
    public static final String OAUTH_REDIRECT_URI_SUFFIX = "services/oauth2/success";
    public static final String PROP_REUSE_CLIENT_CONNECTION = "sfdc.reuseClientConnection";
    public static final String PROP_RICH_TEXT_FIELD_REGEX = "sfdx.richtext.regex";
    
    // salesforce operation parameters
    public static final String PROP_INSERT_NULLS = "sfdc.insertNulls"; //$NON-NLS-1$
    public static final String PROP_ENTITY = "sfdc.entity"; //$NON-NLS-1$
    public static final String PROP_IMPORT_BATCH_SIZE = "sfdc.loadBatchSize"; //$NON-NLS-1$
    public static final String PROP_ASSIGNMENT_RULE = "sfdc.assignmentRule"; //$NON-NLS-1$
    public static final String PROP_IDLOOKUP_FIELD = "sfdc.externalIdField"; //$NON-NLS-1$
    public static final String PROP_EXPORT_BATCH_SIZE = "sfdc.extractionRequestSize"; //$NON-NLS-1$
    public static final String PROP_EXTRACT_SOQL = "sfdc.extractionSOQL"; //$NON-NLS-1$
    public static final String PROP_SORT_EXTRACT_FIELDS = "sfdc.sortExtractionFields"; //$NON-NLS-1$
    public static final String PROP_EXTRACT_ALL_CAPS_HEADERS="sfdc.extraction.allCapsHeaders";
    public static final String PROP_EXTRACT_CSV_OUTPUT_BOM="sfdc.extraction.outputByteOrderMark";
    public static final String PROP_LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT = "sfdc.load.preserveWhitespaceInRichText";
    public static final String PROP_LOAD_REMOVE_LEADING_TRAILING_WHITESPACE_IN_IDLOOKUP_FIELD="sfdc.load.removeLeadingTrailingWhitepaceInIdLookupField";

    //
    // process configuration (action parameters)
    //
    // process.name is used to load the DynaBean from process-conf.xml file 
    // with the same id as the value of process.name property.
    public static final String PROP_PROCESS_NAME = "process.name";
    public static final String PROP_OPERATION = "process.operation"; //$NON-NLS-1$
    public static final String PROP_MAPPING_FILE = "process.mappingFile"; //$NON-NLS-1$
    public static final String PROP_EURO_DATES = "process.useEuropeanDates"; //$NON-NLS-1$

    // process configuration
    public static final String PROP_OUTPUT_STATUS_DIR = "process.statusOutputDirectory"; //$NON-NLS-1$
    public static final String PROP_OUTPUT_SUCCESS = "process.outputSuccess"; //$NON-NLS-1$
    public static final String PROP_ENABLE_EXTRACT_STATUS_OUTPUT = "process.enableExtractStatusOutput"; //$NON-NLS-1$
    public static final String PROP_ENABLE_LAST_RUN_OUTPUT = "process.enableLastRunOutput"; //$NON-NLS-1$
    public static final String PROP_LAST_RUN_OUTPUT_DIR = "process.lastRunOutputDirectory"; //$NON-NLS-1$
    public static final String PROP_OUTPUT_ERROR = "process.outputError"; //$NON-NLS-1$
    public static final String PROP_LOAD_ROW_TO_START_AT = "process.loadRowToStartAt"; //$NON-NLS-1$
    public static final String PROP_INITIAL_LAST_RUN_DATE = "process.initialLastRunDate";
    public static final String PROP_ENCRYPTION_KEY_FILE = "process.encryptionKeyFile"; //$NON-NLS-1$
    public static final String PROP_PROCESS_THREAD_NAME = "process.thread.name";
    public static final String PROP_PROCESS_KEEP_ACCOUNT_TEAM = "process.keepAccountTeam";
    public static final String PROP_PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE = "process.batchMode.exitWithErrorOnFailedRows";

    // data access configuration (e.g., for CSV file, database, etc).
    public static final String PROP_DAO_TYPE = "dataAccess.type"; //$NON-NLS-1$
    public static final String PROP_DAO_NAME = "dataAccess.name"; //$NON-NLS-1$
    public static final String PROP_DAO_READ_BATCH_SIZE = "dataAccess.readBatchSize";
    public static final String PROP_DAO_WRITE_BATCH_SIZE = "dataAccess.writeBatchSize";
    public static final String PROP_DAO_SKIP_TOTAL_COUNT = "dataAccess.skipTotalCount";

    /*
     * TODO: when batching is introduced to the DataAccess, these parameters will become useful
     *     public static final String DAO_REQUEST_SIZE = "dataAccess.extractionRequestSize";
     *     public static final String DAO_BATCH_SIZE = "dataAccess.batchSize";
     */
    public static final String PROP_READ_UTF8 = "dataAccess.readUTF8"; //$NON-NLS-1$
    public static final String PROP_WRITE_UTF8 = "dataAccess.writeUTF8"; //$NON-NLS-1$
    public static final String PROP_READ_CHARSET = "dataAccess.readCharset";
    public static final String PROP_WRITE_CHARSET = "dataAccess.writeCharset";

    public static final String PROP_API_VERSION="salesforce.api.version";
    public static final String PROP_OAUTH_INSTANCE_URL="salesforce.oauth.instanceURL";
    public static final String PROP_USE_LEGACY_HTTP_GET="sfdc.useLegacyHttpGet";
    public static final String PROP_USE_SYSTEM_PROPS_FOR_HTTP_CLIENT="sfdc.useSysPropsForHttpClient";
    public static final String PROP_LIMIT_OUTPUT_TO_QUERY_FIELDS = "loader.query.limitOutputToQueryFields";

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
    public static final String PROP_DUPLICATE_RULE_ALLOW_SAVE = PILOT_PROPERTY_PREFIX + "sfdc.duplicateRule.allowSave"; //$NON-NLS-1$
    public static final String PROP_DUPLICATE_RULE_INCLUDE_RECORD_DETAILS = PILOT_PROPERTY_PREFIX + "sfdc.duplicateRule.includeRecordDetails"; //$NON-NLS-1$
    public static final String PROP_DUPLICATE_RULE_RUN_AS_CURRENT_USER = PILOT_PROPERTY_PREFIX + "sfdc.duplicateRule.runAsCurrentUser"; //$NON-NLS-1$
/*
    public static final String PROP_DAO_READ_PREPROCESSOR_SCRIPT = PILOT_PROPERTY_PREFIX + "dataAccess.read.preProcessorScript";
    public static final String PROP_DAO_WRITE_POSTPROCESSOR_SCRIPT = PILOT_PROPERTY_PREFIX + "dataAccess.write.postProcessorScript";
*/
    /*
     * ===============================
     * End of config properties
     * ===============================
     */
    
    /**
     * Indicates whether a value as been changed
     */
    private boolean inMemoryPropValuesHaveChanged = false;

    /**
     * The file name used by the <code>load</code> method to load a property file. This filename is
     * used to save the properties file when <code>save</code> is called.
     */
    private String filename;
    /**
     * The <code>lastRun</code> is for last run statistics file
     */
    private LastRunProperties lastRunProperties;
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
    public static final String DEFAULT_RICHTEXT_REGEX = "<(?=[a-zA-Z/])(\"[^\"]*\"|'[^']*'|[^'\">])*>";

    
    /*
     * command line options. Not stored in config.properties file.
     * ************
     * Option names MUST start with the prefix "CLI_OPTION_"
     * ************
     */
    public static final String RUN_MODE_UI_VAL = "ui";
    public static final String RUN_MODE_BATCH_VAL = "batch";
    public static final String RUN_MODE_INSTALL_VAL = "install";
    public static final String RUN_MODE_ENCRYPT_VAL = "encrypt";
    
    public static final String PROP_SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV = "process.bulk.saveServerLoadAndRawResultsInCSV";
    public static final String PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO = "process.bulk.cacheDataFromDao";
    public static final String PROP_READ_ONLY_CONFIG_PROPERTIES = "config.properties.readonly";
    public static final String PROP_WIZARD_WIDTH = "sfdc.ui.wizard.width";
    public static final String PROP_WIZARD_HEIGHT = "sfdc.ui.wizard.height";
    public static final String PROP_WIZARD_X_OFFSET = "sfdc.ui.wizard.xoffset";
    public static final String PROP_WIZARD_Y_OFFSET = "sfdc.ui.wizard.yoffset";
    public static final String PROP_WIZARD_CLOSE_ON_FINISH = "sfdc.ui.wizard.closeOnFinish";
    public static final String PROP_WIZARD_POPULATE_RESULTS_FOLDER_WITH_PREVIOUS_OP_RESULTS_FOLDER = "sfdc.ui.wizard.finishStep.prepopulateWithPreviousOpResultsFolder";
    public static final String DIALOG_BOUNDS_PREFIX = "sfdc.ui.dialog.";
    public static final String DIALOG_WIDTH_SUFFIX = ".width";
    public static final String DIALOG_HEIGHT_SUFFIX = ".height";
    public static final int DEFAULT_WIZARD_WIDTH = 600;
    public static final int DEFAULT_WIZARD_HEIGHT = 600;
    public static final int DEFAULT_WIZARD_X_OFFSET = 50;
    public static final int DEFAULT_WIZARD_Y_OFFSET = 50;
    public static final int DIALOG_X_OFFSET = 50;
    public static final int DIALOG_Y_OFFSET = 50;
    private static final String CONFIG_DIR_DEFAULT_VALUE = "configs";
    private static String configurationsDir = null;

    private static final String LAST_RUN_FILE_SUFFIX = "_lastRun.properties"; //$NON-NLS-1$
    // Following properties are read-only, i.e. they are not overridden during save() to config.properties
    // - These properties are not set in Advanced Settings dialog.
    // - Make sure to list all sensitive properties such as password because these properties are not saved.
    private static final String[] READ_ONLY_PROPERTY_NAMES = {
            PROP_PASSWORD,
            PROP_IDLOOKUP_FIELD,
            PROP_MAPPING_FILE,
            PROP_EXTRACT_SOQL,
            PROP_OUTPUT_SUCCESS,
            PROP_OUTPUT_ERROR,
            PROP_DAO_NAME,
            PROP_DAO_TYPE,
            PROP_ENTITY,
            PROP_OPERATION,
            PROP_DEBUG_MESSAGES,
            PROP_DEBUG_MESSAGES_FILE,
            PROP_WIRE_OUTPUT,
            PROP_PROCESS_THREAD_NAME,
            PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO,
            PROP_PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE,
            PROP_SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV,
            PROP_API_VERSION,
            PROP_READ_CHARSET,
            PROP_READ_ONLY_CONFIG_PROPERTIES,
            PROP_RICH_TEXT_FIELD_REGEX,
            /*
            PROP_DAO_READ_PREPROCESSOR_SCRIPT,
            PROP_DAO_WRITE_POSTPROCESSOR_SCRIPT,
            */
            PROP_DELETE_WITH_EXTERNALID,
            PROP_OAUTH_ACCESSTOKEN,
            PROP_OAUTH_REFRESHTOKEN,
            PROP_OAUTH_INSTANCE_URL,
            PROP_OAUTH_REDIRECT_URI_PROD,
            PROP_OAUTH_REDIRECT_URI_SB,
            PROP_OAUTH_CLIENTSECRET_PROD,
            PROP_OAUTH_CLIENTSECRET_SB,
            PROP_RESET_URL_ON_LOGIN,
            PROP_USE_LEGACY_HTTP_GET,
            PROP_USE_SYSTEM_PROPS_FOR_HTTP_CLIENT,
            PROP_AUTH_ENDPOINT_LEGACY,
            PROP_DAO_WRITE_BATCH_SIZE,
            PROP_BUFFER_UNPROCESSED_BULK_QUERY_RESULTS,
            PROP_ENABLE_LAST_RUN_OUTPUT,
            PROP_ENCRYPTION_KEY_FILE,
            PROP_LAST_RUN_OUTPUT_DIR,
            PROP_PROCESS_NAME,
            PROP_BULK_API_CHECK_STATUS_INTERVAL,
            PROP_CONNECTION_TIMEOUT_SECS,
            PROP_ENABLE_RETRIES,
            PROP_LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT,
            PROP_MAX_RETRIES,
            PROP_MIN_RETRY_SLEEP_SECS,
            PROP_REUSE_CLIENT_CONNECTION,
            CLI_OPTION_RUN_MODE,
            AppConfig.CLI_OPTION_CONFIG_DIR_PROP,
            AppConfig.PROP_GMT_FOR_DATE_FIELD_VALUE,
            AppConfig.CLI_OPTION_INSTALLATION_CREATE_DESKTOP_SHORTCUT_PROP,
            AppConfig.CLI_OPTION_INSTALLATION_CREATE_MACOS_APPS_FOLDER_SHORTCUT_PROP,
            AppConfig.CLI_OPTION_INSTALLATION_CREATE_WINDOWS_START_MENU_SHORTCUT_PROP,
            AppConfig.CLI_OPTION_INSTALLATION_FOLDER_PROP,
            AppConfig.PROP_SAVE_ALL_PROPS,
            AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH,
            AppConfig.CLI_OPTION_SYSTEM_PROXY_HOST,
            AppConfig.CLI_OPTION_SYSTEM_PROXY_PORT,
    };
    
    // Properties that are not published as their are either not saved or are read-only
    // for edge use-cases.
    private static final String[] UNPUBLISHED_PROPERTY_NAMES = {
            PROP_SFDC_INTERNAL,
            PROP_SFDC_INTERNAL_IS_SESSION_ID_LOGIN,
            PROP_SFDC_INTERNAL_SESSION_ID,
            PROP_OAUTH_ACCESSTOKEN,
            PROP_OAUTH_REFRESHTOKEN,
            PROP_OAUTH_INSTANCE_URL,
            PROP_DELETE_WITH_EXTERNALID,
            PROP_PROCESS_THREAD_NAME,
            PROP_USE_LEGACY_HTTP_GET,
            PROP_SERVER_ENVIRONMENTS,
            PROP_SELECTED_SERVER_ENVIRONMENT,
            PROP_DAO_SKIP_TOTAL_COUNT,
            AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH,
            AppConfig.CLI_OPTION_INSTALLATION_FOLDER_PROP,
            AppConfig.CLI_OPTION_SYSTEM_PROXY_HOST,
            AppConfig.CLI_OPTION_SYSTEM_PROXY_PORT,
    };
    
    private static final String[] ENCRYPTED_PROPERTY_NAMES = {
            PROP_PASSWORD,
            PROP_PROXY_PASSWORD,
            PROP_OAUTH_ACCESSTOKEN,
            PROP_OAUTH_REFRESHTOKEN,
            PROP_OAUTH_CLIENTSECRET_PROD,
            PROP_OAUTH_CLIENTSECRET_SB,
    };
    
    /**
     * Creates an empty config that loads from and saves to the a file. <p> Use the methods
     * <code>load()</code> and <code>save()</code> to load and store this preference store. </p>
     *
     * @param filename the file name
     * @see #load()
     * @see #save()
     */
    private AppConfig(String filename, Map<String, String> commandLineOptionsMap) throws ConfigInitializationException, IOException {
        loadedProperties = new LinkedProperties();
        this.filename = filename;
        
        File configFile = new File(this.filename);
        this.configDir = configFile.getParentFile().getAbsolutePath();
        
        // initialize with defaults 
        // 
        this.setDefaults(commandLineOptionsMap);
        this.defaultProperties = new LinkedProperties();
        this.defaultProperties.putAll(this.loadedProperties);
        
        // load from config.properties file
        this.load();
        
        // load parameter overrides after loading from config.properties
        // parameter overrides are from two places:
        // 1. process-conf.properties for CLI mode
        // 2. command line options for both CLI and UI modes
        this.loadPropertyOverrides(commandLineOptionsMap);
        saveAllProps = getBoolean(AppConfig.PROP_SAVE_ALL_PROPS);
        
        // last run gets initialized after loading config and overrides
        // since config params are needed for initializing last run.
        initializeLastRun(getLastRunPrefix());
        
        // Properties initialization completed. Configure OAuth environment next
        setServerEnvironment(getString(PROP_SELECTED_SERVER_ENVIRONMENT));
        ConfigPropertyMetadata.generateCSV(this);
    }
    
    private String getLastRunPrefix() {
        String lastRunFilePrefix  = RUN_MODE_UI_VAL;
        if (isBatchMode()) {
            lastRunFilePrefix = getString(AppConfig.PROP_PROCESS_NAME);
            if (lastRunFilePrefix == null || lastRunFilePrefix.isBlank()) {
                lastRunFilePrefix = getString(AppConfig.PROP_ENTITY) + getString(AppConfig.PROP_OPERATION);
            }
            if (lastRunFilePrefix == null || lastRunFilePrefix.isBlank()) {
                lastRunFilePrefix = RUN_MODE_BATCH_VAL;
            }
        }
        return lastRunFilePrefix;
    }
    
    private void initializeLastRun(String lastRunFileNamePrefix) {
        if (lastRunFileNamePrefix == null || lastRunFileNamePrefix.isBlank()) {
            lastRunFileNamePrefix = getString(AppConfig.CLI_OPTION_RUN_MODE);
        }
        String lastRunFileName = lastRunFileNamePrefix + LAST_RUN_FILE_SUFFIX;
        String lastRunDir = getString(AppConfig.PROP_LAST_RUN_OUTPUT_DIR);
        if (lastRunDir == null || lastRunDir.length() == 0) {
            lastRunDir = this.configDir;
        }

        this.lastRunProperties = new LastRunProperties(lastRunFileName, lastRunDir, getBoolean(AppConfig.PROP_ENABLE_LAST_RUN_OUTPUT));
        // Need to initialize last run date if it's present neither in config or override
        lastRunProperties.setDefault(LastRunProperties.LAST_RUN_DATE, getString(PROP_INITIAL_LAST_RUN_DATE));

        try {
            this.lastRunProperties.load();
        } catch (IOException e) {
            logger.warn(Messages.getFormattedString("LastRun.errorLoading", new String[]{
                    this.lastRunProperties.getFullPath(), e.getMessage()}), e);
        }
    }

    private boolean useBulkApiByDefault() {
        return false;
    }

    /**
     * This sets the current defaults.
     */
    private void setDefaults(Map<String, String> cliOptionsMap) {
        setDefaultValue(PROP_HIDE_WELCOME_SCREEN, true);
        setDefaultValue(PROP_SHOW_LOADER_UPGRADE_SCREEN, true);

        setDefaultValue(PROP_CSV_DELIMITER_COMMA, true);
        setDefaultValue(PROP_CSV_DELIMITER_TAB, true);
        setDefaultValue(PROP_CSV_DELIMITER_OTHER, false);
        setDefaultValue(PROP_CSV_DELIMITER_OTHER_VALUE, "");
        setDefaultValue(PROP_CSV_DELIMITER_FOR_QUERY_RESULTS, AppUtil.COMMA);

        setDefaultValue(PROP_AUTH_ENDPOINT_PROD, DEFAULT_ENDPOINT_URL_PROD);
        setDefaultValue(PROP_AUTH_ENDPOINT_SANDBOX, DEFAULT_ENDPOINT_URL_SANDBOX);

        setDefaultValue(PROP_IMPORT_BATCH_SIZE, useBulkApiByDefault() ? DEFAULT_NUM_ROWS_BULK_API_IMPORT_BATCH : DEFAULT_NUM_ROWS_LOAD_BATCH);
        setDefaultValue(PROP_LOAD_ROW_TO_START_AT, 0);
        setDefaultValue(PROP_TIMEOUT_SECS, DEFAULT_TIMEOUT_SECS);
        setDefaultValue(PROP_CONNECTION_TIMEOUT_SECS, DEFAULT_CONNECTION_TIMEOUT_SECS);
        setDefaultValue(PROP_ENABLE_RETRIES, true);
        setDefaultValue(PROP_MAX_RETRIES, DEFAULT_MAX_RETRIES);
        setDefaultValue(PROP_MIN_RETRY_SLEEP_SECS, DEFAULT_MIN_RETRY_SECS);
        setDefaultValue(PROP_ASSIGNMENT_RULE, ""); //$NON-NLS-1$
        setDefaultValue(PROP_INSERT_NULLS, false);
        setDefaultValue(PROP_ENABLE_EXTRACT_STATUS_OUTPUT, false);
        setDefaultValue(PROP_ENABLE_LAST_RUN_OUTPUT, true);
        setDefaultValue(PROP_RESET_URL_ON_LOGIN, true);
        setDefaultValue(PROP_EXPORT_BATCH_SIZE, DEFAULT_EXPORT_BATCH_SIZE);
        setDefaultValue(PROP_SORT_EXTRACT_FIELDS, true);
        setDefaultValue(PROP_DAO_WRITE_BATCH_SIZE, DEFAULT_DAO_WRITE_BATCH_SIZE);
        setDefaultValue(PROP_DAO_READ_BATCH_SIZE, DEFAULT_DAO_READ_BATCH_SIZE);
        setDefaultValue(PROP_TRUNCATE_FIELDS, true);
        setDefaultValue(PROP_FORMAT_PHONE_FIELDS, false);
        // TODO: When we're ready, make Bulk API turned on by default.
        setDefaultValue(PROP_BULK_API_ENABLED, useBulkApiByDefault());
        setDefaultValue(PROP_BULK_API_SERIAL_MODE, false);
        setDefaultValue(PROP_BULK_API_ZIP_CONTENT, false);
        setDefaultValue(PROP_BULK_API_CHECK_STATUS_INTERVAL, DEFAULT_BULK_API_CHECK_STATUS_INTERVAL);
        setDefaultValue(PROP_WIRE_OUTPUT, false);
        setDefaultValue(PROP_DEBUG_MESSAGES, false);
        setDefaultValue(PROP_TIMEZONE, TimeZone.getDefault().getID());
        //sfdcInternal settings
        setDefaultValue(PROP_SFDC_INTERNAL, false);
        setDefaultValue(PROP_SFDC_INTERNAL_IS_SESSION_ID_LOGIN, false);
        setDefaultValue(PROP_SFDC_INTERNAL_SESSION_ID, (String) null);

        //oauth settings
        setDefaultValue(PROP_SELECTED_SERVER_ENVIRONMENT, SERVER_PROD_ENVIRONMENT_VAL);
        setDefaultValue(PROP_SERVER_ENVIRONMENTS, SERVER_PROD_ENVIRONMENT_VAL + AppUtil.COMMA + SERVER_SB_ENVIRONMENT_VAL);

        /* sfdc.oauth.<env>.<bulk | partner>.clientid = DataLoaderBulkUI | DataLoaderPartnerUI */
        setDefaultValue(PROP_CLIENTID_PROD_BULK, BULK_CLIENTID_VAL);
        setDefaultValue(PROP_CLIENTID_PROD_PARTNER, PARTNER_CLIENTID_VAL);

        setDefaultValue(PROP_CLIENTID_SANDBOX_BULK, BULK_CLIENTID_VAL);
        setDefaultValue(PROP_CLIENTID_SANDBOX_PARTNER, PARTNER_CLIENTID_VAL);

        setDefaultValue(PROP_OPERATION, "insert");
        setDefaultValue(PROP_REUSE_CLIENT_CONNECTION, true);
        /*
        setDefaultValue(ENABLE_BULK_QUERY_PK_CHUNKING, false);
        setDefaultValue(BULK_QUERY_PK_CHUNK_SIZE, DEFAULT_BULK_QUERY_PK_CHUNK_SIZE);
        setDefaultValue(BULK_QUERY_PK_CHUNK_START_ROW, "");
        */
        setDefaultValue(PROP_DUPLICATE_RULE_ALLOW_SAVE, false);
        setDefaultValue(PROP_DUPLICATE_RULE_INCLUDE_RECORD_DETAILS, false);
        setDefaultValue(PROP_DUPLICATE_RULE_RUN_AS_CURRENT_USER, false);
        setDefaultValue(PROP_BUFFER_UNPROCESSED_BULK_QUERY_RESULTS, false);
        setDefaultValue(PROP_BULKV2_API_ENABLED, false);
        setDefaultValue(PROP_UPDATE_WITH_EXTERNALID, false);
        setDefaultValue(PROP_DELETE_WITH_EXTERNALID, false);
        setDefaultValue(PROP_OAUTH_LOGIN_FROM_BROWSER, true);
        setDefaultValue(PROP_LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT, true);
        setDefaultValue(CLI_OPTION_RUN_MODE, RUN_MODE_UI_VAL);
        setDefaultValue(PROP_SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV, false);
        setDefaultValue(PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO, true);
        setDefaultValue(PROP_PROCESS_KEEP_ACCOUNT_TEAM, false);
        setDefaultValue(PROP_WIZARD_WIDTH, DEFAULT_WIZARD_WIDTH);
        setDefaultValue(PROP_WIZARD_HEIGHT, DEFAULT_WIZARD_HEIGHT);
        /*
        setDefaultValue(PROP_DAO_READ_PREPROCESSOR_SCRIPT, "");
        setDefaultValue(PROP_DAO_WRITE_POSTPROCESSOR_SCRIPT, "");
        */
        setDefaultValue(PROP_LIMIT_OUTPUT_TO_QUERY_FIELDS, true);
        setDefaultValue(PROP_WIZARD_CLOSE_ON_FINISH, true);
        setDefaultValue(PROP_WIZARD_POPULATE_RESULTS_FOLDER_WITH_PREVIOUS_OP_RESULTS_FOLDER, true);
        setDefaultValue(PROP_WIZARD_X_OFFSET, DEFAULT_WIZARD_X_OFFSET);
        setDefaultValue(PROP_WIZARD_Y_OFFSET, DEFAULT_WIZARD_Y_OFFSET);
        setDefaultValue(PROP_CACHE_DESCRIBE_GLOBAL_RESULTS, true);
        setDefaultValue(PROP_PROCESS_EXIT_WITH_ERROR_ON_FAILED_ROWS_BATCH_MODE, false);
        setDefaultValue(PROP_INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS, false);
        setDefaultValue(PROP_OAUTH_INSTANCE_URL, false);
        String systemProxyHost = cliOptionsMap.get(AppConfig.CLI_OPTION_SYSTEM_PROXY_HOST);
        String systemProxyPort = cliOptionsMap.get(AppConfig.CLI_OPTION_SYSTEM_PROXY_PORT);
        if (systemProxyHost != null && !systemProxyHost.isBlank()) {
            setDefaultValue(PROP_PROXY_HOST, systemProxyHost);
            setDefaultValue(PROP_PROXY_PORT, systemProxyPort);
        }
        setDefaultValue(PROP_USE_LEGACY_HTTP_GET, false);
        setDefaultValue(PROP_USE_SYSTEM_PROPS_FOR_HTTP_CLIENT, true);
        setDefaultValue(PROP_EURO_DATES, false);
        setDefaultValue(PROP_NO_COMPRESSION, false);
        setDefaultValue(PROP_READ_UTF8, false);
        setDefaultValue(PROP_WRITE_UTF8, false);
        setDefaultValue(PROP_RICH_TEXT_FIELD_REGEX, DEFAULT_RICHTEXT_REGEX);
        setDefaultValue(PROP_DAO_SKIP_TOTAL_COUNT, true);
        setDefaultValue(PROP_READ_CHARSET ,getDefaultCharsetForCsvReadWrite());
        setDefaultValue(PROP_WRITE_CHARSET ,getDefaultCharsetForCsvReadWrite());
        setDefaultValue(PROP_GMT_FOR_DATE_FIELD_VALUE, false);
        setDefaultValue(PROP_SAVE_ALL_PROPS, false);
        setDefaultValue(PROP_EXTRACT_ALL_CAPS_HEADERS, false);
        setDefaultValue(PROP_EXTRACT_CSV_OUTPUT_BOM, true);
        setDefaultValue(PROP_LOAD_REMOVE_LEADING_TRAILING_WHITESPACE_IN_IDLOOKUP_FIELD, true);
    }

    /**
     * Returns true if the name is a key in the config
     *
     * @param name the name of the key
     */
    public boolean contains(String name) {
        return loadedProperties.containsKey(name) || lastRunProperties.hasParameter(name) && lastRunProperties.containsKey(name);
    }

    /**
     * Gets boolean for a given name
     *
     * @return boolean
     */
    public boolean getBoolean(String name) {
        String value = getParamValue(name);
        if (value == null || value.length() == 0) return BOOLEAN_DEFAULT;
        if (value.equals(AppConfig.TRUE)) return true;
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
        return TimeZone.getTimeZone(getString(PROP_TIMEZONE));
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
        return this.lastRunProperties == null ? null : this.lastRunProperties.getFullPath();
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

        if (lastRunProperties != null && lastRunProperties.hasParameter(name)) {
            propValue = lastRunProperties.getProperty(name);
        } else {
            propValue = loadedProperties != null ? loadedProperties.getProperty(name) : null;
        }
        
        // check if a property's value is configured when it used to be a pilot property
        // if value not set.
        if (propValue == null && !name.startsWith(PILOT_PROPERTY_PREFIX)) { 
            String pilotName = PILOT_PROPERTY_PREFIX + name;
            String pilotValue = getParamValue(pilotName);
            if (pilotValue != null && !pilotValue.isEmpty()) {
                // if picking up the value from a pilot property that is no longer in pilot,
                // set the value for the new property to be the same as the value of the pilot property
                doSetPropertyAndUpdateConfig(name, propValue, pilotValue, false);
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
        loadedProperties.list(out);
        lastRunProperties.list(out);
    }

    /**
     * Prints the contents of this preference store to the given print writer.
     *
     * @param out the print writer
     */
    public void list(PrintWriter out) {
        loadedProperties.list(out);
        lastRunProperties.list(out);
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
            Properties propsFromFile = new LinkedProperties();
            propsFromFile.load(in);
            removeEmptyProperties(propsFromFile);
            loadedProperties.putAll(propsFromFile);
            for (String roprop : READ_ONLY_PROPERTY_NAMES) {
                if (propsFromFile.containsKey(roprop)) {
                    this.readOnlyPropertiesFromPropertiesFile.put(
                                roprop,
                                propsFromFile.get(roprop));
                }
                    
            }
        } catch (IOException e) {
            logger.fatal(Messages.getFormattedString("Config.errorPropertiesLoad", e.getMessage()));
            throw e;
        }
        // paramter post-processing
        postLoad(loadedProperties, true);

        inMemoryPropValuesHaveChanged = false;
    }
    
    public static boolean isReadOnlyProperty(String propertyName) {
        if (propertyName == null) {
            return false;
        }
        for (String roProp : AppConfig.READ_ONLY_PROPERTY_NAMES) {
            if (roProp.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isOverrideProperty(String propertyName) {
        if (propertyName == null) {
            return false;
        }
        if (this.overridesMap != null
            && this.overridesMap.containsKey(propertyName)) {
            return true;
        }
        return false;
    }
    
    public static boolean isInternalProperty(String propertyName) {
        if (propertyName == null) {
            return false;
        }
        for (String roProp : AppConfig.UNPUBLISHED_PROPERTY_NAMES) {
            if (roProp.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }   
    public static boolean isEncryptedProperty(String propertyName) {
        if (propertyName == null) {
            return false;
        }
        for (String encryptedProp : AppConfig.ENCRYPTED_PROPERTY_NAMES) {
            if (encryptedProp.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Post process parameters. Right now, only decrypts encrypted values in the map
     *
     * @param values Values to be post-processed
     */
    @SuppressWarnings("unchecked")
    private void postLoad(Map<?, ?> propMap, boolean isConfigFilePropsMap) throws ConfigInitializationException {
        // initialize encryption
        initEncryption((Map<String, String>) propMap);

        // decrypt encrypted values
        for (String encryptedProp : ENCRYPTED_PROPERTY_NAMES) {
            decryptAndCacheProperty(propMap, encryptedProp);
        }
        
        // Do not load unsupported properties and CLI options even if they are specified in config.properties file
        if (isConfigFilePropsMap) {
            removeCLIOnlyOptionsFromProperties();
            removeUnsupportedProperties();
        }
    }

    private void decryptAndCacheProperty(Map<?, ?> values, String propertyName) throws ConfigInitializationException {
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
    public void loadPropertyOverrides(Map<String, String> overridesMap) throws ParameterLoadException,
            ConfigInitializationException {
        this.overridesMap = overridesMap;
        // make sure to post-process the args to be loaded
        postLoad(overridesMap, false);

        // replace values in the Config
        putValues(overridesMap);
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
     * Decrypt property with propName using the encrypter. If decryption succeeds, return the
     * decrypted value
     *
     * @return decrypted property value
     */
    static private String encryptProperty(EncryptionAesUtil encrypter, Map<String, String> propMap, String propName, boolean isBatch)
            throws ParameterLoadException {
        String propValue = propMap.get(propName);
        if (propValue != null && propValue.length() > 0) {
            try {
                return encrypter.encryptMsg(propValue);
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
        String keyFile = values.get(PROP_ENCRYPTION_KEY_FILE);
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
     * Puts a set of values from a map into config
     *
     * @param values Map of overriding values
     */
    public void putValues(Map<String, String> values) throws ParameterLoadException, ConfigInitializationException {
        if (values == null) {
            return;
        }
        for (String key : values.keySet()) {
            setProperty(key, values.get(key), false);
        }
    }

    /**
     * Saves the preferences to the file from which they were originally loaded.
     *
     * @throws java.io.IOException if there is a problem saving this store
     */
    public void save() throws IOException, GeneralSecurityException {
        // lastrun properties are always saved
        lastRunProperties.save();
        
        if (getString(AppConfig.CLI_OPTION_RUN_MODE).equalsIgnoreCase(AppConfig.RUN_MODE_BATCH_VAL)
           || getBoolean(PROP_READ_ONLY_CONFIG_PROPERTIES)
           || !inMemoryPropValuesHaveChanged) {
            return; // do not save any updates to config.properties file
        }
        if (filename == null) {
            throw new IOException(Messages.getString("Config.fileMissing")); //$NON-NLS-1$
        }

        Properties inMemoryProperties = new LinkedProperties();
        inMemoryProperties.putAll(this.loadedProperties);
        
        // property additions section - all property additions occur before removals
        // add encrypted property name, value pairs for saving
        encryptPropertiesBeforeSave();

        // property removals section - all property additions occur before this
        removeCommandLineOptionsBeforeSave();
        removeReadOnlyPropertiesBeforeSave();
        removeUnsupportedProperties();
        removeDecryptedPropertiesBeforeSave();
        removeCLIOnlyOptionsFromProperties();
        removeEmptyProperties(this.loadedProperties);
        removeDefaultPropertiesBeforeSave();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            saveConfigProperties(out, "Salesforce Data Loader Config", loadedProperties); //$NON-NLS-1$
        } finally {
            if (out != null) {
                out.close();
            }
            // restore original in-memory property values
            loadedProperties = inMemoryProperties;
        }
    }
    
    public void setAuthEndpointForCurrentEnv(String authEndpoint) {
        this.setAuthEndpointForEnv(authEndpoint, getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT));
    }
    
    public void setAuthEndpointForEnv(String authEndpoint, String env) {
        AppUtil.validateAuthenticationHostDomainUrlAndThrow(authEndpoint);
        if (env != null && env.equalsIgnoreCase(AppConfig.SERVER_SB_ENVIRONMENT_VAL)) {
            this.setValue(AppConfig.PROP_AUTH_ENDPOINT_SANDBOX, authEndpoint);
        } else {
            this.setValue(AppConfig.PROP_AUTH_ENDPOINT_PROD, authEndpoint);
        }
    }
    
    public String getAuthEndpointForCurrentEnv() {
        String endpoint = null;
        if (AppConfig.SERVER_SB_ENVIRONMENT_VAL.equals(this.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT))) {
            endpoint = getString(AppConfig.PROP_AUTH_ENDPOINT_SANDBOX);
        } else {
            endpoint = getString(AppConfig.PROP_AUTH_ENDPOINT_PROD);
        }
        
        // try with legacy endpoint property
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = getString(PROP_AUTH_ENDPOINT_LEGACY);
        }
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = getDefaultAuthEndpointForCurrentEnv();
        }
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        AppUtil.validateAuthenticationHostDomainUrlAndThrow(endpoint);
        return endpoint;
    }
    
    public String getDefaultAuthEndpointForCurrentEnv() {
        if (AppConfig.SERVER_SB_ENVIRONMENT_VAL.equals(this.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT))) {
            return AppConfig.DEFAULT_ENDPOINT_URL_SANDBOX;
        } else { // assume production is the only alternate environment
            return AppConfig.DEFAULT_ENDPOINT_URL_PROD;
        }
    }
    
    public boolean isDefaultAuthEndpointForCurrentEnv(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return false;
        }
        if (AppConfig.SERVER_SB_ENVIRONMENT_VAL.equals(this.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT))) {
            return AppConfig.DEFAULT_ENDPOINT_URL_SANDBOX.equalsIgnoreCase(endpoint);
        } else { // assume production is the only alternate environment
            return AppConfig.DEFAULT_ENDPOINT_URL_PROD.equalsIgnoreCase(endpoint);
        }
    }

    private void encryptPropertiesBeforeSave() {
        for (String encryptedProp : ENCRYPTED_PROPERTY_NAMES) {
            if (this.loadedProperties.containsKey(encryptedProp)) {
                Map<?, ?> propMap = (Map<?, ?>)this.loadedProperties;
                try {
                    @SuppressWarnings("unchecked")
                    String propValue = encryptProperty(encrypter, 
                            (Map<String, String>)propMap,
                            encryptedProp, isBatchMode());
                    this.loadedProperties.put(encryptedProp, propValue);
                } catch (ParameterLoadException e) {
                    this.loadedProperties.remove(encryptedProp); // Encryption attempt failed. Do not save.
                }
            }
        }
    }
    
    private void removeCommandLineOptionsBeforeSave() {
        if (this.overridesMap != null) {
            for (String propertyName : this.overridesMap.keySet()) {
                this.loadedProperties.remove(propertyName);
            }
        }
    }
    
    private void removeReadOnlyPropertiesBeforeSave() {
        for (String roprop : READ_ONLY_PROPERTY_NAMES) {
            if (!this.readOnlyPropertiesFromPropertiesFile.containsKey(roprop)) {
                this.loadedProperties.remove(roprop);
            }
        }
    }
    
    private void removeUnsupportedProperties() {
        // do not save a value for enabling Bulk V2 
        //this.properties.remove(BULKV2_API_ENABLED);
    }
    
    private void removeDefaultPropertiesBeforeSave() {
        if (saveAllProps) {
            // do not remove default values
            return;
        }
        this.loadedProperties.entrySet().removeIf(
                entry -> (this.defaultProperties.get(entry.getKey().toString()) != null
                        && this.defaultProperties.get(entry.getKey().toString()).equals(entry.getValue())));
    }
    
    private void removeDecryptedPropertiesBeforeSave() {
        this.loadedProperties.entrySet().removeIf(entry -> (entry.getKey().toString().endsWith(DECRYPTED_SUFFIX)));
    }
    
    private void removeCLIOnlyOptionsFromProperties() {
        Set<String> keys = this.loadedProperties.stringPropertyNames();
        Field[] allFields = AppConfig.class.getDeclaredFields();
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
                        this.loadedProperties.remove(key);
                    }
                }
            }
        }
    }
    
    private void removeEmptyProperties(Properties props) {
        props.entrySet().removeIf(entry -> 
        (entry.getValue() == null || entry.getValue().toString().isBlank()));
    }
    
    /**
     * Save statistics from the last run
     */
    public void saveLastRun() throws IOException {
        lastRunProperties.save();
    }

    /**
     * Saves this config to the given output stream. The given string is inserted as header
     * information.
     *
     * @param out    the output stream
     * @param header the header
     * @throws java.io.IOException if there is a problem saving this store
     */
    private void saveConfigProperties(OutputStream out, String header, Properties configProperties) throws IOException {
        if (getString(AppConfig.CLI_OPTION_RUN_MODE).equalsIgnoreCase(AppConfig.RUN_MODE_BATCH_VAL)
                || getBoolean(PROP_READ_ONLY_CONFIG_PROPERTIES)
                || !inMemoryPropValuesHaveChanged) {
            return;
        }
        configProperties.store(out, header);
        inMemoryPropValuesHaveChanged = false;
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
        setProperty(name, sb.toString(), false);
    }
    
    public static final String CLIENT_ID_HEADER_NAME="client_id";
    
    public String getClientIdNameValuePair() {
        return CLIENT_ID_HEADER_NAME + "=" + this.getClientIDForCurrentEnv();
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
        if (value == null) {
            return;
        }
        String oldValue = getString(name);
        if (oldValue == null || !oldValue.equals(value)) {
            setProperty(name, value.toString(), skipIfAlreadySet);
        }
    }
    
    private void setConfigPropertyMetadata(String propName, String propVal, boolean isDefault) {
        ConfigPropertyMetadata configPropMD = configPropsMetadataMap.get(propName);
        if (configPropMD == null) {
            return; // did not find the property metadata in the registry of properties
        }
        if (isDefault) {
            configPropMD.setDefaultValue(propVal);
        }
    }
    
    /**
     * @param name
     * @param newValue
     */
    private void setProperty(String name, String newValue, boolean skipIfAlreadySet) {
        setConfigPropertyMetadata(name, newValue, skipIfAlreadySet);
        final String oldValue = getString(name);
        if (skipIfAlreadySet && oldValue != null && !oldValue.isBlank()) {
            // do not override the old value
            return;
        }
        final boolean paramChanged = (oldValue == null || oldValue.length() == 0) ? (newValue != null && newValue
                .length() > 0) : !oldValue.equals(newValue);
        if (paramChanged) {
            doSetPropertyAndUpdateConfig(name, oldValue, newValue, skipIfAlreadySet);
        }
    }
    
    private void doSetPropertyAndUpdateConfig(String name, String oldValue, String newValue, boolean skipIfAlreadySet) {
        configChanged(name, oldValue, newValue);
        if (oldValue == null) {
            oldValue = "";
        }
        if (newValue == null) {
            newValue = "";
        }
        if (lastRunProperties != null && lastRunProperties.hasParameter(name)) {
            lastRunProperties.put(name, newValue);
        } else {
            loadedProperties.put(name, newValue);
        }
        if (!isReadOnlyProperty(name)
            && !isOverrideProperty(name)
            && !isInternalProperty(name)
            && !skipIfAlreadySet
            && !lastRunProperties.hasParameter(name)) {
            //read-only properties, internal properties,
            // or properties overridden by setting them in process-conf.xml
            // or passing them as command line options should not result in updates
            // to config.properties file. 
            //
            // Also, if skipIfAlreadySet==true, the property was not set
            // in config.properties file. It is being set to its default value.
            this.inMemoryPropValuesHaveChanged = true;
        }
    }

    public boolean isBatchMode() {
        return (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.BATCH);
    }

    public int getCurrentSettingForMaxRowsInImportBatch() {
        boolean bulkApi = isBulkAPIEnabled();
        boolean bulkV2Api = this.isBulkV2APIEnabled();
        
        if (bulkV2Api) {
            return MAX_NUM_ROWS_BULKV2_API_IMPORT_JOB;
        }
        
        int bs = -1;
        try {
            bs = getInt(PROP_IMPORT_BATCH_SIZE);
        } catch (ParameterLoadException e) {
        }
        int maxBatchSize = bulkApi ? MAX_NUM_ROWS_BULK_API_IMPORT_BATCH : MAX_NUM_ROWS_SOAP_API_IMPORT_BATCH;
        return bs > maxBatchSize ? maxBatchSize : bs > 0 ? bs : getDefaultNumRowsImportBatch(bulkApi, bulkV2Api);
    }

    public int getDefaultNumRowsImportBatch(boolean bulkApi, boolean bulkV2Api) {
        if (bulkV2Api) {
            return MAX_NUM_ROWS_BULKV2_API_IMPORT_JOB;
        }
        return bulkApi ? DEFAULT_NUM_ROWS_BULK_API_IMPORT_BATCH : DEFAULT_NUM_ROWS_LOAD_BATCH;
    }
    
    public int getMaxPossibleNumRowsImportBatchForAPIType(boolean bulkApi, boolean bulkV2Api) {
        if (bulkV2Api) {
            return MAX_NUM_ROWS_BULKV2_API_IMPORT_JOB;
        }
        return bulkApi ? MAX_NUM_ROWS_BULK_API_IMPORT_BATCH : MAX_NUM_ROWS_SOAP_API_IMPORT_BATCH;
    }
    
    public boolean useBulkAPIForCurrentOperation() {
        return (isBulkAPIEnabled() || isBulkV2APIEnabled()) && isBulkApiOperation();
    }

    public boolean isBulkAPIEnabled() {
        return getBoolean(PROP_BULK_API_ENABLED) && !isBulkV2APIEnabled();
    }
    
    public boolean isBulkV2APIEnabled() {
        return getBoolean(PROP_BULKV2_API_ENABLED);
    }
    
    public boolean isRESTAPIEnabled() {
        return getBoolean(PROP_UPDATE_WITH_EXTERNALID);
    }
    
    private boolean isBulkApiOperation() {
        return getOperationInfo().bulkAPIEnabled();
    }

    public OperationInfo getOperationInfo() {
        return getEnum(OperationInfo.class, PROP_OPERATION);
    }

    public String getCsvEncoding(boolean isWrite) {
        // charset is for CSV read unless isWrite is set to true
        String configProperty = PROP_READ_UTF8;
        if (isWrite) {
            configProperty = PROP_WRITE_UTF8;
            logger.debug("Getting charset for writing to CSV");
        } else {
            logger.debug("Getting charset for reading from CSV");
        }
        String charset = getDefaultCharsetForCsvReadWrite();
        if (getBoolean(configProperty)) {
            logger.debug("Using UTF8 charset because '" 
                    +  configProperty
                    +"' is set to true");
            charset = StandardCharsets.UTF_8.name();
        } else {
            if (isWrite) {
                charset = getString(PROP_WRITE_CHARSET);
            } else {
                charset = getString(PROP_READ_CHARSET);
            }
            boolean validCharset = false;
            for (String charsetName : Charset.availableCharsets().keySet()) {
                if (charset.equalsIgnoreCase(charsetName)) {
                    validCharset = true;
                    break;
                }
            }
            if (!validCharset) {
                logger.warn("configured charset" + charset + " is not supported");
                charset = getDefaultCharsetForCsvReadWrite(); 
            }
        }
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
    
    public String getOAuthEnvironmentPropertyName(String environmentName, String propertyPartialName) {
        return OAUTH_PREFIX + environmentName + "." + propertyPartialName;
    }

    public void setOAuthEnvironmentString(String environmentName, String name, String... values) {
        setValue(OAUTH_PREFIX + environmentName + "." + name, values);
    }

    public void setServerEnvironment(String environment) {
        if (environment == null || environment.isBlank()) {
            environment = SERVER_PROD_ENVIRONMENT_VAL;
        }
        String[] envArray = getString(PROP_SERVER_ENVIRONMENTS).split(",");
        boolean isEnvMatch = false;
        for (String env : envArray) {
            env = env.strip();
            if (env.equalsIgnoreCase(environment)) {
                isEnvMatch = true;
            }
        }
        if (!isEnvMatch) {
            environment = SERVER_PROD_ENVIRONMENT_VAL;
        }
        setValue(PROP_SELECTED_SERVER_ENVIRONMENT, environment);
    }
    
    public String getOAuthClientSecretForCurrentEnv() {
        return getOAuthEnvironmentString(getString(PROP_SELECTED_SERVER_ENVIRONMENT),
                                            CLIENTSECRET_LITERAL);
    }
    
    public String getOAuthRedirectURIForCurrentEnv() {
        return getAuthEndpointForCurrentEnv() + AppConfig.OAUTH_REDIRECT_URI_SUFFIX;
    }
    
    public String getClientIDForCurrentEnv() {
        String clientId;
        String environment = getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT);
        if (getBoolean(PROP_BULK_API_ENABLED) || getBoolean(PROP_BULKV2_API_ENABLED)) {
            clientId = getOAuthEnvironmentString(environment, BULK_CLIENTID_LITERAL);
        } else {
            clientId = getOAuthEnvironmentString(environment, PARTNER_CLIENTID_LITERAL);
        }
        if (clientId == null || clientId.isEmpty()) {
            clientId = getOAuthEnvironmentString(environment, CLIENTID_LITERAL);
        }
        return clientId;
    }
    
    public static synchronized String getConfigurationsDir() {
        if (AppConfig.configurationsDir == null) {
            setConfigurationsDir(null);
        }
        return AppConfig.configurationsDir;
    }

    private static String getDefaultConfigDir() {
        return AppUtil.getDirContainingClassJar(AppConfig.class) 
                + System.getProperty("file.separator")
                + AppConfig.CONFIG_DIR_DEFAULT_VALUE;
    }

    private static synchronized void setConfigurationsDir(Map<String, String> argsMap) {
        if (argsMap != null && argsMap.containsKey(CLI_OPTION_CONFIG_DIR_PROP)) {
            AppConfig.configurationsDir = argsMap.get(CLI_OPTION_CONFIG_DIR_PROP);
        } else if (AppConfig.configurationsDir != null && !AppConfig.configurationsDir.isEmpty()) {
                return;
        } else {
            // first time invocation and configurationsDir is not set through argsMap
            AppConfig.configurationsDir = System.getProperty(CLI_OPTION_CONFIG_DIR_PROP);
            if (AppConfig.configurationsDir == null || AppConfig.configurationsDir.isBlank()) {
                AppConfig.configurationsDir = AppConfig.getDefaultConfigDir();
            }
        }
        File configDirFile = new File(AppConfig.configurationsDir);
        try {
            AppConfig.configurationsDir = configDirFile.getCanonicalPath();
        } catch (IOException e) {
            logger.error("Unable to find configuration folder " + AppConfig.configurationsDir);
            AppConfig.configurationsDir = configDirFile.getAbsolutePath();
        }
        System.setProperty(CLI_OPTION_CONFIG_DIR_PROP, AppConfig.configurationsDir);
    }

    private static synchronized String[] initializeAppConfig(String[] args) throws FactoryConfigurationError, IOException, ConfigInitializationException {
        Map<String, String> argsMap = AppUtil.convertCommandArgsArrayToArgMap(args);
        AppConfig.setConfigurationsDir(argsMap);
        LoggingUtil.initializeLog(argsMap);
        return AppUtil.convertCommandArgsMapToArgsArray(argsMap);
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
                logger.debug("Created config folder: " + dirPath);
            } else {
                logger.warn("Unable to create config folder: " + dirPath);
            }
        } else {
            logger.debug("Config folder already exists: " + dirPath);
        }
        return isSuccessful;
    }

    /**
     * Get the current config.properties and load it into the config bean.
     * @throws ConfigInitializationException 
     * @throws IOException 
     * @throws FactoryConfigurationError 
     */
    private static AppConfig currentConfig = null;
    public static synchronized AppConfig getInstance(Map<String, String> argMap) throws ConfigInitializationException, FactoryConfigurationError, IOException {
        if (argMap == null) {
            argMap = new HashMap<String, String>();
        }
        if (argMap.isEmpty()) {
            logger.warn("No arguments provided to initialize AppConfig. Loading default configuration.");
        }
        AppConfig.initializeAppConfig(AppUtil.convertCommandArgsMapToArgsArray(argMap));

        String configurationsDirPath = AppConfig.getConfigurationsDir();
        File configurationsDir;
        final String DEFAULT_CONFIG_FILE = "defaultConfig.properties"; //$NON-NLS-1$

        configurationsDir = new File(configurationsDirPath);
 
        // Create dir if it doesn't exist
        boolean isMkdirSuccessfulOrExisting = createDir(configurationsDir);
        if (!isMkdirSuccessfulOrExisting) {
            String errorMsg = Messages.getMessage(AppConfig.class, "errorCreatingOutputDir", configurationsDirPath);
            logger.error(errorMsg);
            throw new ConfigInitializationException(errorMsg);
        }

        // check if the config file exists
        File configFile = new File(configurationsDir.getAbsolutePath(), CONFIG_FILE);

        String configFilePath = configFile.getAbsolutePath();
        logger.debug("Looking for file in config path: " + configFilePath);
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
            logger.debug("User config is found in " + configFile.getAbsolutePath());
        }

        try {
            currentConfig = new AppConfig(configFilePath, argMap);
            logger.debug(Messages.getMessage(AppConfig.class, "configInit")); //$NON-NLS-1$
        } catch (IOException | ProcessInitializationException e) {
            logger.error(e.getMessage());
            throw new ConfigInitializationException(Messages.getMessage(AppConfig.class, "errorConfigLoad", configFilePath), e);
        }
        return currentConfig;
    }
    
    public synchronized static AppConfig getCurrentConfig() {
        return currentConfig;
    }
    
    public static interface ConfigListener {
        void configValueChanged(String key, String oldValue, String newValue);
    }
    
    public static void setBulkV2JobMaxBytes(int maxBytes) {
        currentMaxBulkv2APIImportJobBytes = maxBytes;
    }
    
    public int getMaxBytesInBulkBatch() {
        return isBulkV2APIEnabled() ? AppConfig.currentMaxBulkv2APIImportJobBytes : AppConfig.MAX_BULK_API_IMPORT_BATCH_BYTES;
    }
}
