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

package com.salesforce.dataloader.client;

/**
 * The sfdc api client class - implemented using the partner wsdl
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.PasswordExpiredException;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.LimitInfo;
import com.sforce.soap.partner.LimitInfoHeader_element;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

import org.apache.commons.beanutils.DynaBean;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.salesforce.dataloader.ui.UIUtils.validateHttpsUrlAndThrow;

public class PartnerClient extends ClientBase<PartnerConnection> {

    private static Logger LOG = LogManager.getLogger(PartnerClient.class);

    PartnerConnection client;
    private ConnectorConfig connectorConfig = null;

    private static interface ClientOperation<RESULT, ARG> {
        String getName();

        RESULT run(ARG arg) throws ConnectionException;
    }

    private final ClientOperation<SaveResult[], SObject[]> INSERT_OPERATION = new ClientOperation<SaveResult[], SObject[]>() {
        @Override
        public String getName() {
            return "insert";
        }

        @Override
        public SaveResult[] run(SObject[] sObjects) throws ConnectionException {
            return getClient().create(sObjects);
        }
    };

    private final ClientOperation<SaveResult[], SObject[]> UPDATE_OPERATION = new ClientOperation<SaveResult[], SObject[]>() {
        @Override
        public String getName() {
            return "update";
        }

        @Override
        public SaveResult[] run(SObject[] sObjects) throws ConnectionException {
            return getClient().update(sObjects);
        }
    };

    private final ClientOperation<UpsertResult[], SObject[]> UPSERT_OPERATION = new ClientOperation<UpsertResult[], SObject[]>() {
        @Override
        public String getName() {
            return "upsert";
        }

        @Override
        public UpsertResult[] run(SObject[] sObjects) throws ConnectionException {
            return getClient().upsert(config.getString(Config.EXTERNAL_ID_FIELD), sObjects);
        }
    };

    private final ClientOperation<DeleteResult[], String[]> DELETE_OPERATION = new ClientOperation<DeleteResult[], String[]>() {
        @Override
        public String getName() {
            return "delete";
        }

        @Override
        public DeleteResult[] run(String[] ids) throws ConnectionException {
            return getClient().delete(ids);
        }
    };

    private final ClientOperation<QueryResult, String> QUERY_OPERATION = new ClientOperation<QueryResult, String>() {
        @Override
        public String getName() {
            return "query";
        }

        @Override
        public QueryResult run(String queryString) throws ConnectionException {
            return getClient().query(queryString);
        }
    };

    private final ClientOperation<QueryResult, String> QUERY_ALL_OPERATION = new ClientOperation<QueryResult, String>() {
        @Override
        public String getName() {
            return "queryAll";
        }

        @Override
        public QueryResult run(String queryString) throws ConnectionException {
            return getClient().queryAll(queryString);
        }
    };

    private final ClientOperation<QueryResult, String> QUERY_MORE_OPERATION = new ClientOperation<QueryResult, String>() {
        @Override
        public String getName() {
            return "queryMore";
        }

        @Override
        public QueryResult run(String queryString) throws ConnectionException {
            return getClient().queryMore(queryString);
        }
    };

    private final ClientOperation<LoginResult, PartnerConnection> LOGIN_OPERATION = new ClientOperation<LoginResult, PartnerConnection>() {
        @Override
        public String getName() {
            return "login";
        }

        @Override
        public LoginResult run(PartnerConnection client) throws ConnectionException {
            ConnectorConfig cc = client.getConfig();
            return client.login(cc.getUsername(), cc.getPassword());
        }
    };

    private final ClientOperation<DescribeGlobalResult, Object> DESCRIBE_GLOBAL_OPERATION = new ClientOperation<DescribeGlobalResult, Object>() {
        @Override
        public String getName() {
            return "describeGlobal";
        }

        @Override
        public DescribeGlobalResult run(Object ignored) throws ConnectionException {
            return getClient().describeGlobal();
        }
    };

    private final ClientOperation<DescribeSObjectResult, String> DESCRIBE_SOBJECT_OPERATION = new ClientOperation<DescribeSObjectResult, String>() {
        @Override
        public String getName() {
            return "describeSObject";
        }

        @Override
        public DescribeSObjectResult run(String entity) throws ConnectionException {
            return getClient().describeSObject(entity);
        }
    };

    private DescribeGlobalResult entityTypes;
    private final Map<String, DescribeRefObject> referenceDescribes = new HashMap<String, DescribeRefObject>();
    private final Map<String, DescribeGlobalSObjectResult> describeGlobalResults = new HashMap<String, DescribeGlobalSObjectResult>();
    private final Map<String, DescribeSObjectResult> entityDescribes = new HashMap<String, DescribeSObjectResult>();

    private final boolean enableRetries;
    private final int maxRetries;

    public PartnerClient(Controller controller) {
        super(controller, LOG);
        int retries = -1;
        this.enableRetries = config.getBoolean(Config.ENABLE_RETRIES);
        if (this.enableRetries) {
            try {
                // limit the number of max retries in case limit is exceeded
                retries = Math.min(Config.MAX_RETRIES_LIMIT, config.getInt(Config.MAX_RETRIES));
            } catch (ParameterLoadException e) {
                retries = Config.DEFAULT_MAX_RETRIES;
            }
        }
        this.maxRetries = retries;
    }

    public boolean connect() throws ConnectionException {
        return login();
    }

    @Override
    protected boolean connectPostLogin(ConnectorConfig cc) {
        if (getClient() == null)
            throw new IllegalStateException("Client should be logged in already");

        getClient().setCallOptions(ClientBase.getClientName(this.config), null);
        // query header
        int querySize;
        try {
            querySize = config.getInt(Config.EXTRACT_REQUEST_SIZE);
        } catch (ParameterLoadException e) {
            querySize = Config.DEFAULT_EXTRACT_REQUEST_SIZE;
        }
        if (querySize > 0) {
            getClient().setQueryOptions(querySize);
        }

        // assignment rule for update
        if (config.getString(Config.ASSIGNMENT_RULE).length() > 14) {
            String rule = config.getString(Config.ASSIGNMENT_RULE);
            if (rule.length() > 15) {
                rule = rule.substring(0, 15);
            }
            getClient().setAssignmentRuleHeader(rule, false);
        }

        // field truncation
        getClient().setAllowFieldTruncationHeader(config.getBoolean(Config.TRUNCATE_FIELDS));

        // TODO: make this configurable
        getClient().setDisableFeedTrackingHeader(true);

        getClient().setDuplicateRuleHeader(
            config.getBoolean(Config.DUPLICATE_RULE_ALLOW_SAVE),
            config.getBoolean(Config.DUPLICATE_RULE_INCLUDE_RECORD_DETAILS),
            config.getBoolean(Config.DUPLICATE_RULE_RUN_AS_CURRENT_USER)
        );

        return true;
    }

    public UpsertResult[] loadUpserts(List<DynaBean> dynaBeans) throws ConnectionException {
        UpsertResult[] ur = runOperation(UPSERT_OPERATION, getSobjects(dynaBeans, UPSERT_OPERATION.getName()));

        for (int j = 0; j < ur.length; j++) {
            if (ur[j].getSuccess()) {
                if (ur[j].getCreated()) {
                    logger.debug(Messages.getString("Client.itemCreated") + ur[j].getId()); //$NON-NLS-1$
                } else {
                    logger.debug(Messages.getString("Client.itemUpdated") + ur[j].getId()); //$NON-NLS-1$
                }
            }
            processResult(ur[j].getSuccess(), "Client.itemUpserted", ur[j].getId(), ur[j].getErrors(), j);
        }
        return ur;
    }

    /**
     * @param dynaBeans
     * @return SaveResult array
     * @throws ConnectionException
     */
    public SaveResult[] loadUpdates(List<DynaBean> dynaBeans) throws ConnectionException {
        return runSaveOperation(dynaBeans, UPDATE_OPERATION, false);
    }

    /**
     * @param dynaBeans
     * @return SaveResult array
     * @throws ConnectionException
     */
    public SaveResult[] loadInserts(List<DynaBean> dynaBeans) throws ConnectionException {
        return runSaveOperation(dynaBeans, INSERT_OPERATION, true);

    }

    private SaveResult[] runSaveOperation(List<DynaBean> dynaBeans, ClientOperation<SaveResult[], SObject[]> op,
                                          boolean isInsert) throws ApiFault, ConnectionException {
        SaveResult[] sr = runOperation(op, getSobjects(dynaBeans, op.getName()));
        String saveMessage = isInsert ? "Client.itemCreated" : "Client.itemUpdated";
        for (int j = 0; j < sr.length; j++) {
            processResult(sr[j].isSuccess(), saveMessage, sr[j].getId(), sr[j].getErrors(), j);
        }
        return sr;
    }

    private SObject[] getSobjects(List<DynaBean> dynaBeans, String opName) {
        try {
            SObject[] sobjects = SforceDynaBean.getSObjectArray(controller, dynaBeans, config.getString(Config.ENTITY),
                    config.getBoolean(Config.INSERT_NULLS));
            logger.debug(Messages.getString("Client.arraySize") + sobjects.length); //$NON-NLS-1$
            return sobjects;
        } catch (IllegalAccessException ex) {
            logger.error(
                    Messages.getFormattedString("Client.operationError", new String[]{opName, ex.getMessage()}), ex); //$NON-NLS-1$
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            logger.error(
                    Messages.getFormattedString("Client.operationError", new String[]{opName, ex.getMessage()}), ex); //$NON-NLS-1$
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            logger.error(
                    Messages.getFormattedString("Client.operationError", new String[]{opName, ex.getMessage()}), ex); //$NON-NLS-1$
            throw new RuntimeException(ex);
        } catch (ParameterLoadException ex) {
            logger.error(
                    Messages.getFormattedString("Client.operationError", new String[]{opName, ex.getMessage()}), ex); //$NON-NLS-1$
            throw new RuntimeException(ex);
        }
    }

    protected <R, A> R runOperation(ClientOperation<R, A> op, A arg) throws ConnectionException {
        logger.debug(Messages.getFormattedString("Client.beginOperation", op.getName())); //$NON-NLS-1$
        if (op != this.LOGIN_OPERATION && !isSessionValid()) {
            connect();
        }
        int totalAttempts = 1 + (this.enableRetries ? this.maxRetries : 0);
        ConnectionException connectionException = null;
        for (int tryNum = 0; tryNum < totalAttempts; tryNum++) {
            try {
                R result = op.run(arg);
                if (result == null)
                    logger.info(Messages.getString("Client.resultNull")); //$NON-NLS-1$
                return result;
            } catch (ConnectionException ex) {
                logger.error(
                        Messages.getFormattedString(
                                "Client.operationError", new String[]{op.getName(), ex.getMessage()}), ex); //$NON-NLS-1$
                // check retries
                if (!checkConnectionException(ex, op.getName(), tryNum)) throw ex;
                connectionException = ex;
            }
        }
        throw connectionException;
    }

    /**
     * @param dynaBeans
     * @return DeleteResult array
     * @throws ConnectionException
     */
    public DeleteResult[] loadDeletes(List<DynaBean> dynaBeans) throws ConnectionException {


        DynaBean dynaBean;
        String[] dels = new String[dynaBeans.size()];
        for (int i = 0; i < dynaBeans.size(); i++) {
            dynaBean = dynaBeans.get(i);
            String id = (String) dynaBean.get("Id"); //$NON-NLS-1$
            if (id == null) {
                id = "";
            }
            dels[i] = id;
        }
        logger.debug(Messages.getString("Client.arraySize") + dels.length); //$NON-NLS-1$


        DeleteResult[] result = runOperation(DELETE_OPERATION, dels);

        for (int j = 0; j < result.length; j++) {
            processResult(result[j].isSuccess(), "Client.itemDeleted", result[j].getId(), result[j].getErrors(), j);
        }
        return result;
    }

    /**
     * Query next batch of records using the query cursor
     *
     * @param soql
     * @return query results
     * @throws ConnectionException
     */
    public QueryResult queryMore(String soql) throws ConnectionException {
        return runOperation(QUERY_MORE_OPERATION, soql);
    }

    /**
     * Query objects excluding the deleted objects
     *
     * @param soql
     * @return query results
     * @throws ConnectionException
     */
    public QueryResult query(String soql) throws ConnectionException {
        return runOperation(QUERY_OPERATION, soql);
    }

    /**
     * Query objects including the deleted objects
     *
     * @param soql
     * @return query results
     */
    public QueryResult queryAll(String soql) throws ConnectionException {
        return runOperation(QUERY_ALL_OPERATION, soql);
    }

    /**
     * Process result of a change operation that returns data success / errors (examples of operations with such
     * results: insert, update, upsert, delete, merge)
     *
     * @param success
     *            True if result is success
     * @param successMsgKey
     * @param id
     *            Item id(if available)
     * @param errors
     *            Error array(if available)
     * @param itemNbr
     *            Item number in the result
     */
    private void processResult(boolean success, String successMsgKey, String id, Error[] errors, int itemNbr) {
        if (success) {
            logger.debug(Messages.getString(successMsgKey) + id);
        } else {
            // there were errors during the delete call, go through the errors
            // array and write them to the screen
            for (Error err : errors) {
                int startRow;
                try {
                    startRow = config.getInt(Config.LOAD_ROW_TO_START_AT);
                } catch (ParameterLoadException e) {
                    startRow = 0;
                }
                logger.error(Messages.getString("Client.itemError") //$NON-NLS-1$
                        + Integer.valueOf(itemNbr + startRow).toString());
                logger.error(Messages.getString("Client.errorCode") + err.getStatusCode().toString()); //$NON-NLS-1$
                logger.error(Messages.getString("Client.errorMessage") + err.getMessage()); //$NON-NLS-1$
            }
        }
    }

    @Override
    public PartnerConnection getClient() {
        return this.client;
    }

    public Map<String, DescribeGlobalSObjectResult> getDescribeGlobalResults() {
        return describeGlobalResults;
    }

    Map<String, DescribeSObjectResult> getEntityDescribeMap() {
        return this.entityDescribes;
    }

    DescribeGlobalResult getEntityTypes() {
        return entityTypes;
    }

    public DescribeSObjectResult getFieldTypes() {
        String entity = this.config.getString(Config.ENTITY);
        try {
            return describeSObject(entity);
        } catch (ConnectionException e) {
            throw new RuntimeException("Unexpected failure describing main entity " + entity, e);
        }
    }

    public Map<String, DescribeRefObject> getReferenceDescribes() {
        return referenceDescribes;
    }
    
    public LimitInfo getAPILimitInfo() {
        LimitInfoHeader_element limitInfoElement = getClient().getLimitInfoHeader();
        for (LimitInfo info : limitInfoElement.getLimitInfo()) {
            if ("API REQUESTS".equalsIgnoreCase(info.getType())) {
                return info;
            }
        }
        return null;
    }

    boolean isSessionValid() {
        if (config.getBoolean(Config.SFDC_INTERNAL) && config.getBoolean(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN)) {
            return true;
        }
        if (config.getString(Config.OAUTH_ACCESSTOKEN) != null && config.getString(Config.OAUTH_ACCESSTOKEN).trim().length() > 0) {
            return true;
        }
        return isLoggedIn();
    }

    private boolean login() throws ConnectionException, ApiFault {
        disconnect();
        try {
            login(apiVersionForTheSession);
            logger.debug("able to successfully invoke server APIs of version " + apiVersionForTheSession);
        } catch (UnexpectedErrorFault fault) {
            if (fault.getExceptionCode() == ExceptionCode.UNSUPPORTED_API_VERSION) 
            /*
                && (apiVersionForTheSession.equals(getCurrentAPIVersionInWSC())
               ) 
            */
            {
                logger.error("Failed to successfully invoke server APIs of version " + apiVersionForTheSession);
                apiVersionForTheSession = getPreviousAPIVersionInWSC();
                login(apiVersionForTheSession);
            } else {
                logger.error("Failed to get user info using manually configured session id", fault);
                throw fault;
            }
        }   catch (ConnectionException e) {
            logger.error("Failed to get user info using manually configured session id", e);
            throw e;
        }
        return true; // exception thrown if there is an issue with login
    }
    
    private boolean login(String apiVersionStr) throws ConnectionException, ApiFault {
        // Attempt the login giving the user feedback
        logger.info(Messages.getString("Client.sforceLogin")); //$NON-NLS-1$
        final ConnectorConfig cc = getLoginConnectorConfig(apiVersionStr);
        PartnerConnection conn = Connector.newConnection(cc);
        // identify the client as dataloader
        conn.setCallOptions(ClientBase.getClientName(this.config), null);

        String oauthAccessToken = config.getString(Config.OAUTH_ACCESSTOKEN);
        if (oauthAccessToken != null && oauthAccessToken.trim().length() > 0) {
            conn = setConfiguredSessionId(conn, oauthAccessToken);
        } else if (config.getBoolean(Config.SFDC_INTERNAL) && config.getBoolean(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN)) {
            conn = setConfiguredSessionId(conn, config.getString(Config.SFDC_INTERNAL_SESSION_ID));
        } else {
            setSessionRenewer(conn);
            loginInternal(conn);
        }
        synchronized (apiVersionForTheSession) {
            if (!isValidApiVersionForTheSession) {
                apiVersionForTheSession = apiVersionStr;
                isValidApiVersionForTheSession = true;
            }
        }
        return true;

    }

    private void setSessionRenewer(final PartnerConnection conn) {
        conn.getConfig().setSessionRenewer(new SessionRenewer() {
            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                loginInternal(conn);
                return null;
            }
        });
    }

    private PartnerConnection setConfiguredSessionId(PartnerConnection conn, String sessionId) throws ConnectionException {
        logger.info("Using manually configured session id to bypass login");
        conn.setSessionHeader(sessionId);
        conn.getUserInfo(); // check to make sure we have a good connection
        loginSuccess(conn, getServerUrl(config.getString(Config.ENDPOINT)));
        return conn;
    }

    private void loginInternal(final PartnerConnection conn) throws ConnectionException, PasswordExpiredException {
        final ConnectorConfig cc = conn.getConfig();
        try {
            logger.info(Messages.getMessage(getClass(), "sforceLoginDetail", cc.getAuthEndpoint(), cc.getUsername()));
            LoginResult loginResult = runOperation(LOGIN_OPERATION, conn);
            // if password has expired, throw an exception
            if (loginResult.getPasswordExpired()) {
                throw new PasswordExpiredException(Messages
                        .getString("Client.errorExpiredPassword")); //$NON-NLS-1$
            }
            // update session id and service endpoint based on response
            conn.setSessionHeader(loginResult.getSessionId());
            String serverUrl = loginResult.getServerUrl();
            String server = getServerUrl(serverUrl);
            if (config.getBoolean(Config.RESET_URL_ON_LOGIN)) {
                cc.setServiceEndpoint(serverUrl);
            }
            loginSuccess(conn, server);
        } catch (ConnectionException ex) {
            logger.error(Messages.getMessage(getClass(), "loginError", cc.getAuthEndpoint(), ex.getMessage()), ex);
            throw ex;
        }
    }

    private void loginSuccess(PartnerConnection conn, String serv) {
        this.client = conn;
        setSession(conn.getSessionHeader().getSessionId(), serv);
    }

    private String getServerStringFromUrl(URL url) {
        return url.getProtocol() + "://" + url.getAuthority();
    }

    private String getServerUrl(String serverUrl) {
        if (config.getBoolean(Config.RESET_URL_ON_LOGIN)) {
            try {
                validateHttpsUrlAndThrow(serverUrl);
                return getServerStringFromUrl(new URL(serverUrl));
            } catch (MalformedURLException e) {
                logger.fatal("Unexpected error", e);
                throw new RuntimeException(e);
            }
        }
        return getDefaultServer();
    }

    public boolean logout() {
        try {
            PartnerConnection pc = getClient();
            if (pc != null) pc.logout();
            
        } catch (ConnectionException e) {
            // ignore
        } finally {
            synchronized (apiVersionForTheSession) {
                apiVersionForTheSession = null;
                isValidApiVersionForTheSession = false;
            }
            disconnect();
        }
        return true;
    }

    public void disconnect() {
        clearSession();
        this.client = null;
    }

    /**
     * @param operationName
     */
    private void retrySleep(String operationName, int retryNum) {
        int sleepSecs;
        try {
            sleepSecs = config.getInt(Config.MIN_RETRY_SLEEP_SECS);
        } catch (ParameterLoadException e1) {
            sleepSecs = Config.DEFAULT_MIN_RETRY_SECS;
        }
        // sleep between retries is based on the retry attempt #. Sleep for longer periods with each retry
        sleepSecs = sleepSecs + (retryNum * 10); // sleep for MIN_RETRY_SLEEP_SECS + 10, 20, 30, etc.

        logger.info(Messages.getFormattedString("Client.retryOperation", new String[]{Integer.toString(retryNum + 1),
                operationName, Integer.toString(sleepSecs)}));
        try {
            Thread.sleep(sleepSecs * 1000);
        } catch (InterruptedException e) { // ignore
        }
    }

    /**
     * Gets the sObject describes for all entities
     */
    public boolean setEntityDescribes() throws ConnectionException {
        setEntityTypes();
        if (this.describeGlobalResults.isEmpty()) {
            for (DescribeGlobalSObjectResult res : entityTypes.getSobjects()) {
                if (res != null) this.describeGlobalResults.put(res.getName(), res);
            }
        }

        return true;
    }

    /**
     * Gets the available objects from the global describe
     */
    private void setEntityTypes() throws ConnectionException {
        if (this.entityTypes == null)
            this.entityTypes = runOperation(DESCRIBE_GLOBAL_OPERATION, null);
    }

    /**
     * Set the map of references to object external id info for current entity
     *
     * @throws ConnectionException
     */
    public void setFieldReferenceDescribes() throws ConnectionException {
        referenceDescribes.clear();
        if (getDescribeGlobalResults().isEmpty()) {
            setEntityDescribes();
        }
        if (getFieldTypes() == null) {
            setFieldTypes();
        }
        if (getDescribeGlobalResults() != null) {
            Field[] entityFields = getFieldTypes().getFields();
            String entityName = this.config.getString(Config.ENTITY);

            for (Field entityField : entityFields) {
                // upsert on references (aka foreign keys) is supported only
                // 1. When field has relationship is set and refers to exactly one object
                // 2. When field is either createable or updateable. If neither is true, upsert will never work for that
                // relationship.
                if (entityField.isCreateable() || entityField.isUpdateable()) {
                    String relationshipName = entityField.getRelationshipName();
                    
                    // Skip traversing CreatedBy and LastModifiedBy relationships.
                    // Upserts should not use these relationships.
                    if (!entityField.isCustom()
                        && (
                            "CreatedBy".equalsIgnoreCase(relationshipName)
                        || "LastModifiedBy".equalsIgnoreCase(relationshipName)
                        )) {
                        continue;
                    }
                    String[] referenceTos = entityField.getReferenceTo();
                    if (referenceTos != null && referenceTos.length == 1 && referenceTos[0] != null
                            && relationshipName != null && relationshipName.length() > 0
                            && (entityField.isCreateable() || entityField.isUpdateable())) {

                        String refEntityName = referenceTos[0];

                        // make sure that the object is legal to upsert
                        Field[] refObjectFields = describeSObject(refEntityName).getFields();
                        Map<String, Field> refFieldInfo = new HashMap<String, Field>();
                        for (Field refField : refObjectFields) {
                            boolean skipReference = true;
                            if (refField.isExternalId()
                                || ((refField.isNameField() ||  refField.getType().equals(FieldType.email)) 
                                    && refField.isIdLookup())) {
                                // change createable and updateable attributes of a reference field
                                // only if it is not a self-reference.
                                if (!entityName.equalsIgnoreCase(refEntityName)) {
                                    refField.setCreateable(entityField.isCreateable());
                                    refField.setUpdateable(entityField.isUpdateable());
                                }
                                refFieldInfo.put(refField.getName(), refField);
                            }
                        }
                        if (!refFieldInfo.isEmpty()) {
                            DescribeRefObject describe = new DescribeRefObject(refEntityName, refFieldInfo);
                            referenceDescribes.put(relationshipName, describe);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the sobject describe for the given entity
     *
     * @throws ConnectionException
     */
    public void setFieldTypes() throws ConnectionException {
        describeSObject(config.getString(Config.ENTITY));
    }

    /**
     * @return true if loggedIn
     */
    public boolean isLoggedIn() {
        return getSession().isSessionValid();
    }

    public void validateSession() {
        getSession().validate();
    }

    @Override
    protected ConnectorConfig getConnectorConfig(String apiVersionStr) {
        ConnectorConfig cc = super.getConnectorConfig(apiVersionStr);
        cc.setManualLogin(true);
        return cc;
    }
    
    protected static String getServicePathForAPIVersion(String apiVersionStr) {
        // Auth endpoint is a SOAP service
        return ClientBase.getServicePathForAPIVersion(DEFAULT_AUTH_ENDPOINT_URL.getPath(), apiVersionStr);
    }

    private synchronized ConnectorConfig getLoginConnectorConfig(String apiVersion) {
        this.connectorConfig = getConnectorConfig(apiVersion);
        String serverUrl = getDefaultServer();
        this.connectorConfig.setAuthEndpoint(serverUrl + getServicePathForAPIVersion(apiVersion));
        this.connectorConfig.setServiceEndpoint(serverUrl + getServicePathForAPIVersion(apiVersion));
        return this.connectorConfig;
    }

    private String getDefaultServer() {
        String serverUrl = config.getString(Config.ENDPOINT);
        if (serverUrl == null || serverUrl.length() == 0) {
            serverUrl = getServerStringFromUrl(DEFAULT_AUTH_ENDPOINT_URL);
        }
        validateHttpsUrlAndThrow(serverUrl);
        return serverUrl;
    }

    /**
     * This function returns the describe call for an sforce entity
     *
     * @return DescribeSObjectResult
     * @throws ConnectionException
     */

    public DescribeSObjectResult describeSObject(String entity) throws ConnectionException {
        DescribeSObjectResult result = getEntityDescribeMap().get(entity);
        if (result == null) {
            result = runOperation(DESCRIBE_SOBJECT_OPERATION, entity);
            if (result != null) {
                getEntityDescribeMap().put(result.getName(), result);
            }
        }
        return result;
    }

    /**
     * Checks whether retry makes sense for the given exception and given the number of current vs. max retries. If
     * retry makes sense, then before returning, this method will put current thread to sleep before allowing another
     * retry.
     *
     * @param ex
     * @param operationName
     * @return true if retry should be executed for operation. false if there's no retry.
     */
    private boolean checkConnectionException(ConnectionException ex, String operationName, int retryNum) {
        if (!this.enableRetries) return false;
        final String msg = ex.getMessage();
        if (msg != null && msg.toLowerCase().indexOf("connection reset") >= 0) {
            retrySleep(operationName, retryNum);
            return true;
        }
        return false;
    }

    private final Map<String, Field> fieldsByName = new HashMap<String, Field>();

    public Field getField(String apiName) {
        apiName = apiName.toLowerCase();
        Field field = this.fieldsByName.get(apiName);
        if (field == null) {
            field = lookupField(apiName);
            this.fieldsByName.put(apiName, field);
        }
        return field;
    }

    private Field lookupField(String apiName) {
        // look for field on target object
        for (Field f : getFieldTypes().getFields()) {
            if (apiName.equals(f.getName().toLowerCase()) || apiName.equals(f.getLabel().toLowerCase()))
                return f;
        }
        // look for reference field on target object
        if (apiName.contains(":")) {
            Map<String, DescribeRefObject> refs = getReferenceDescribes();
            for (Map.Entry<String, DescribeRefObject> ent : refs.entrySet()) {
                String relName = ent.getKey().toLowerCase();
                if (apiName.startsWith(relName)) {
                    for (Map.Entry<String, Field> refEntry : ent.getValue().getFieldInfoMap().entrySet()) {
                        String thisRefName = relName + ":" + refEntry.getKey().toLowerCase();
                        if (apiName.equals(thisRefName)) return refEntry.getValue();
                    }
                }
            }
        }
        return null;
    }

}
