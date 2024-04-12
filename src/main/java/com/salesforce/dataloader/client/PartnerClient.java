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
import com.salesforce.dataloader.dyna.ParentIdLookupFieldFormatter;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.PasswordExpiredException;
import com.salesforce.dataloader.exception.RelationshipFormatException;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.LimitInfo;
import com.sforce.soap.partner.LimitInfoHeader_element;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UndeleteResult;
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

public class PartnerClient extends ClientBase<PartnerConnection> {

    private static Logger LOG = LogManager.getLogger(PartnerClient.class);

    PartnerConnection connection;
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
            return getConnection().create(sObjects);
        }
    };

    private final ClientOperation<SaveResult[], SObject[]> UPDATE_OPERATION = new ClientOperation<SaveResult[], SObject[]>() {
        @Override
        public String getName() {
            return "update";
        }

        @Override
        public SaveResult[] run(SObject[] sObjects) throws ConnectionException {
            return getConnection().update(sObjects);
        }
    };

    private final ClientOperation<UpsertResult[], SObject[]> UPSERT_OPERATION = new ClientOperation<UpsertResult[], SObject[]>() {
        @Override
        public String getName() {
            return "upsert";
        }

        @Override
        public UpsertResult[] run(SObject[] sObjects) throws ConnectionException {
            return getConnection().upsert(config.getString(Config.EXTERNAL_ID_FIELD), sObjects);
        }
    };

    private final ClientOperation<DeleteResult[], String[]> DELETE_OPERATION = new ClientOperation<DeleteResult[], String[]>() {
        @Override
        public String getName() {
            return "delete";
        }

        @Override
        public DeleteResult[] run(String[] ids) throws ConnectionException {
            return getConnection().delete(ids);
        }
    };

    private final ClientOperation<UndeleteResult[], String[]> UNDELETE_OPERATION = new ClientOperation<UndeleteResult[], String[]>() {
        @Override
        public String getName() {
            return "undelete";
        }

        @Override
        public UndeleteResult[] run(String[] ids) throws ConnectionException {
            return getConnection().undelete(ids);
        }
    };
    
    private final ClientOperation<QueryResult, String> QUERY_OPERATION = new ClientOperation<QueryResult, String>() {
        @Override
        public String getName() {
            return "query";
        }

        @Override
        public QueryResult run(String queryString) throws ConnectionException {
            return getConnection().query(queryString);
        }
    };

    private final ClientOperation<QueryResult, String> QUERY_ALL_OPERATION = new ClientOperation<QueryResult, String>() {
        @Override
        public String getName() {
            return "queryAll";
        }

        @Override
        public QueryResult run(String queryString) throws ConnectionException {
            return getConnection().queryAll(queryString);
        }
    };

    private final ClientOperation<QueryResult, String> QUERY_MORE_OPERATION = new ClientOperation<QueryResult, String>() {
        @Override
        public String getName() {
            return "queryMore";
        }

        @Override
        public QueryResult run(String queryString) throws ConnectionException {
            return getConnection().queryMore(queryString);
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
            return getConnection().describeGlobal();
        }
    };

    private final ClientOperation<DescribeSObjectResult, String> DESCRIBE_SOBJECT_OPERATION = new ClientOperation<DescribeSObjectResult, String>() {
        @Override
        public String getName() {
            return "describeSObject";
        }

        @Override
        public DescribeSObjectResult run(String entity) throws ConnectionException {
            return getConnection().describeSObject(entity);
        }
    };

    private DescribeGlobalResult describeGlobalResults;
    private final ReferenceEntitiesDescribeMap referenceEntitiesDescribesMap = new ReferenceEntitiesDescribeMap();
    private final Map<String, DescribeGlobalSObjectResult> describeGlobalResultsMap = new HashMap<String, DescribeGlobalSObjectResult>();
    private final Map<String, DescribeSObjectResult> entityFieldDescribesMap = new HashMap<String, DescribeSObjectResult>();

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
        if (getConnection() == null)
            throw new IllegalStateException("Client should be logged in already");

        getConnection().setCallOptions(ClientBase.getClientName(this.config), null);
        // query header
        int querySize;
        try {
            querySize = config.getInt(Config.EXTRACT_REQUEST_SIZE);
        } catch (ParameterLoadException e) {
            querySize = Config.DEFAULT_EXTRACT_REQUEST_SIZE;
        }
        if (querySize > 0) {
            getConnection().setQueryOptions(querySize);
        }

        // assignment rule for update
        if (config.getString(Config.ASSIGNMENT_RULE).length() > 14) {
            String rule = config.getString(Config.ASSIGNMENT_RULE);
            if (rule.length() > 15) {
                rule = rule.substring(0, 15);
            }
            getConnection().setAssignmentRuleHeader(rule, false);
        }

        // field truncation
        getConnection().setAllowFieldTruncationHeader(config.getBoolean(Config.TRUNCATE_FIELDS));

        // TODO: make this configurable
        getConnection().setDisableFeedTrackingHeader(true);

        getConnection().setDuplicateRuleHeader(
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
     * @throws ConnectionExceptio
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
                this.getSession().performedSessionActivity(); // reset session activity timer
                return result;
            } catch (ConnectionException ex) {
                logger.error(
                        Messages.getFormattedString(
                                "Client.operationError", new String[]{op.getName(), ex.getMessage()}), ex); //$NON-NLS-1$
                if (ex instanceof ApiFault) {
                    ApiFault fault = (ApiFault)ex;
                    String faultMessage = fault.getExceptionMessage();
                    logger.error(
                            Messages.getFormattedString(
                                    "Client.operationError", new String[]{op.getName(), faultMessage}), fault); //$NON-NLS-1$

                }
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
     * @param dynaBeans
     * @return UndeleteResult array
     * @throws ConnectionException
     */
    public UndeleteResult[] loadUndeletes(List<DynaBean> dynaBeans) throws ConnectionException {


        DynaBean dynaBean;
        String[] undels = new String[dynaBeans.size()];
        for (int i = 0; i < dynaBeans.size(); i++) {
            dynaBean = dynaBeans.get(i);
            String id = (String) dynaBean.get("Id"); //$NON-NLS-1$
            if (id == null) {
                id = "";
            }
            undels[i] = id;
        }
        logger.debug(Messages.getString("Client.arraySize") + undels.length); //$NON-NLS-1$


        UndeleteResult[] result = runOperation(UNDELETE_OPERATION, undels);

        for (int j = 0; j < result.length; j++) {
            processResult(result[j].isSuccess(), "Client.itemUndeleted", result[j].getId(), result[j].getErrors(), j);
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
    public PartnerConnection getConnection() {
        return this.connection;
    }

    public Map<String, DescribeGlobalSObjectResult> getDescribeGlobalResults() {
        if (this.describeGlobalResults == null || !config.getBoolean(Config.CACHE_DESCRIBE_GLOBAL_RESULTS)) {
            this.describeGlobalResultsMap.clear();
            try {
                this.describeGlobalResults = runOperation(DESCRIBE_GLOBAL_OPERATION, null);
            } catch (ConnectionException e) {
                logger.error("Failed to get description of sobjects", e.getMessage());
                return null;
            }
        }
        
        if (this.describeGlobalResultsMap.isEmpty()) {
            for (DescribeGlobalSObjectResult res : describeGlobalResults.getSobjects()) {
                if (res != null) {
                    if (res.getLabel().startsWith("__MISSING LABEL__")) {
                        res.setLabel(res.getName());
                    }
                    this.describeGlobalResultsMap.put(res.getName(), res);
                }
            }
        }
        return describeGlobalResultsMap;
    }

    private Map<String, DescribeSObjectResult> getCachedEntityDescribeMap() {
        return this.entityFieldDescribesMap;
    }

    public DescribeSObjectResult getFieldTypes() {
        String entity = this.config.getString(Config.ENTITY);
        try {
            return describeSObject(entity);
        } catch (ConnectionException e) {
            throw new RuntimeException("Unexpected failure describing main entity " + entity, e);
        }
    }

    public ReferenceEntitiesDescribeMap getReferenceDescribes() {
        return referenceEntitiesDescribesMap;
    }
    
    public LimitInfo getAPILimitInfo() {
        LimitInfoHeader_element limitInfoElement = getConnection().getLimitInfoHeader();
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
            dologin();
            logger.debug("able to successfully invoke server APIs of version " + apiVersionForTheSession);
        } catch (UnexpectedErrorFault fault) {
            if (fault.getExceptionCode() == ExceptionCode.UNSUPPORTED_API_VERSION) {
                logger.error("Failed to successfully invoke server APIs of version " + apiVersionForTheSession);
                apiVersionForTheSession = getPreviousAPIVersionInWSC();
                login();
            } else {
                logger.error("Failed to get user info using manually configured session id", fault);
                throw fault;
            }
        } catch (ConnectionException e) {
            String authEndpoint = config.getString(Config.ENDPOINT);
            logger.warn(Messages.getMessage(this.getClass(), "failedUsernamePasswordAuth", 
                                            authEndpoint, Config.ENDPOINT, e.getMessage()));
            if (authEndpoint.contains(Config.LIGHTNING_ENDPOINT_URL_PART_VAL)) {
                authEndpoint = authEndpoint.replace(Config.LIGHTNING_ENDPOINT_URL_PART_VAL, Config.MYSF_ENDPOINT_URL_PART_VAL);
                config.setValue(Config.ENDPOINT, authEndpoint);
                logger.info(Messages.getMessage(this.getClass(), "retryUsernamePasswordAuth", authEndpoint, Config.ENDPOINT));
                login();
            } else if (!authEndpoint.equals(Config.DEFAULT_ENDPOINT_URL)) {
                config.setValue(Config.ENDPOINT, Config.DEFAULT_ENDPOINT_URL);
                logger.info(Messages.getMessage(this.getClass(), "retryUsernamePasswordAuth", Config.DEFAULT_ENDPOINT_URL, Config.ENDPOINT));
                login();
            } else {
                logger.error("Failed to get user info using manually configured session id", e);
                throw e;   
            }
        }
        return true; // exception thrown if there is an issue with login
    }
    
    private boolean dologin() throws ConnectionException, ApiFault {
        // Attempt the login giving the user feedback
        logger.info(Messages.getString("Client.sforceLogin")); //$NON-NLS-1$
        final ConnectorConfig cc = getLoginConnectorConfig();
        boolean savedIsTraceMessage = cc.isTraceMessage();
        cc.setTraceMessage(false);
        PartnerConnection conn = Connector.newConnection(cc);
        // identify the client as dataloader
        conn.setCallOptions(ClientBase.getClientName(this.config), null);

        String oauthAccessToken = config.getString(Config.OAUTH_ACCESSTOKEN);
        try {
            if (oauthAccessToken != null && oauthAccessToken.trim().length() > 0) {
                conn = setConfiguredSessionId(conn, oauthAccessToken, null);
            } else if (config.getBoolean(Config.SFDC_INTERNAL) && config.getBoolean(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN)) {
                conn = setConfiguredSessionId(conn, config.getString(Config.SFDC_INTERNAL_SESSION_ID), null);
            } else {
                setSessionRenewer(conn);
                loginInternal(conn);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            cc.setTraceMessage(savedIsTraceMessage);
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

    private PartnerConnection setConfiguredSessionId(PartnerConnection conn, String sessionId, GetUserInfoResult userInfo) throws ConnectionException {
        logger.info("Using manually configured session id to bypass login");
        conn.setSessionHeader(sessionId);
        if (userInfo == null) {
            userInfo = conn.getUserInfo(); // check to make sure we have a good connection
        }
        loginSuccess(conn, getServerUrl(config.getString(Config.ENDPOINT)), userInfo);
        return conn;
    }

    private void loginInternal(final PartnerConnection conn) throws ConnectionException, PasswordExpiredException {
        final ConnectorConfig cc = conn.getConfig();
        cc.setRequestHeader("client_id", config.getString(Config.OAUTH_CLIENTID));
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
            loginSuccess(conn, server, loginResult.getUserInfo());
        } catch (ConnectionException ex) {
            logger.error(Messages.getMessage(getClass(), "loginError", cc.getAuthEndpoint(), ex.getMessage()), ex);
            throw ex;
        }
    }

    private void loginSuccess(PartnerConnection conn, String serv, GetUserInfoResult userInfo) {
        this.connection = conn;
        setSession(conn.getSessionHeader().getSessionId(), serv, userInfo);
    }

    private String getServerStringFromUrl(URL url) {
        return url.getProtocol() + "://" + url.getAuthority();
    }

    private String getServerUrl(String serverUrl) {
        if (config.getBoolean(Config.RESET_URL_ON_LOGIN)) {
            try {
                AppUtil.validateHttpsUrlAndThrow(serverUrl);
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
            PartnerConnection pc = getConnection();
            if (pc != null) pc.logout();
            
        } catch (ConnectionException e) {
            // ignore
        } finally {
            disconnect();
        }
        return true;
    }

    public void disconnect() {
        clearSession();
        this.connection = null;
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
     * Set the map of references to object external id info for current entity
     *
     * @throws ConnectionException
     */
    public void setFieldReferenceDescribes() throws ConnectionException {
        referenceEntitiesDescribesMap.clear();
        if (getFieldTypes() == null) {
            setFieldTypes();
        }
        if (getDescribeGlobalResults() != null) {
            Field[] entityFields = getFieldTypes().getFields();

            for (Field childObjectField : entityFields) {
                // upsert on references (aka foreign keys) is supported only
                // 1. When field has relationship is set and refers to exactly one object
                // 2. When field is either createable or updateable. If neither is true, upsert will never work for that
                // relationship.
                String[] parentObjectNames = childObjectField.getReferenceTo();
                String relationshipName = childObjectField.getRelationshipName();
                if (parentObjectNames == null || parentObjectNames.length == 0 || parentObjectNames[0] == null
                    || relationshipName == null || relationshipName.length() == 0
                    || (!childObjectField.isCreateable() && !childObjectField.isUpdateable())) {
                    // parent-child relationship either does not exist or
                    // it is neither modifiable nor updateable.
                    continue;
                }
                processParentObjectArrayForLookupReferences(parentObjectNames, childObjectField);
            }
        }
    }
    
    private void processParentObjectArrayForLookupReferences(String[] parentObjectNames, Field childObjectField) throws ConnectionException {
        for (int parentObjectIndex = 0; parentObjectIndex < parentObjectNames.length; parentObjectIndex++ ) {
            String parentObjectName = parentObjectNames[parentObjectIndex];
            processParentObjectForLookupReferences(parentObjectName, childObjectField, parentObjectIndex, parentObjectNames.length);
        }
    }
    
    private void processParentObjectForLookupReferences(String parentObjectName, Field childObjectField, int parentObjectIndex, int numParentTypes) throws ConnectionException {
        Field[] parentObjectFields = describeSObject(parentObjectName).getFields();
        Map<String, Field> parentIdLookupFieldMap = new HashMap<String, Field>();
        for (Field parentField : parentObjectFields) {
            processParentFieldForLookupReference(parentField, childObjectField, numParentTypes, parentObjectIndex, numParentTypes, parentIdLookupFieldMap);
        }
        if (!parentIdLookupFieldMap.isEmpty()) {
            DescribeRefObject describeRelationship = new DescribeRefObject(parentObjectName, childObjectField, parentIdLookupFieldMap);
            referenceEntitiesDescribesMap.put(childObjectField.getRelationshipName(), describeRelationship);
        }
    }
    
    private void processParentFieldForLookupReference(Field parentField, Field childObjectField, int numParentTypes, int parentObjectIndex, int totalParentObjects, Map<String, Field> parentIdLookupFieldMap) {
        if (!parentField.isIdLookup()) {
            return;
        }
        parentIdLookupFieldMap.put(parentField.getName(), parentField);

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
    public ConnectorConfig getConnectorConfig() {
        ConnectorConfig cc = super.getConnectorConfig();
        cc.setManualLogin(true);
        return cc;
    }
    
    public static String getServicePath() {
        // Auth endpoint is a SOAP service
        return ClientBase.getServicePathWithAPIVersion(DEFAULT_AUTH_ENDPOINT_URL.getPath());
    }

    private synchronized ConnectorConfig getLoginConnectorConfig() {
        this.connectorConfig = getConnectorConfig();
        String serverUrl = getDefaultServer();
        this.connectorConfig.setAuthEndpoint(serverUrl + getServicePath());
        this.connectorConfig.setServiceEndpoint(serverUrl + getServicePath());
        return this.connectorConfig;
    }

    private String getDefaultServer() {
        String serverUrl = config.getString(Config.ENDPOINT);
        if (serverUrl == null || serverUrl.length() == 0) {
            serverUrl = getServerStringFromUrl(DEFAULT_AUTH_ENDPOINT_URL);
        }
        AppUtil.validateHttpsUrlAndThrow(serverUrl);
        return serverUrl;
    }

    /**
     * This function returns the describe call for an sforce entity
     *
     * @return DescribeSObjectResult
     * @throws ConnectionException
     */

    public DescribeSObjectResult describeSObject(String entity) throws ConnectionException {
        DescribeSObjectResult result = null;
        if (config.getBoolean(Config.CACHE_DESCRIBE_GLOBAL_RESULTS)) {
            result = getCachedEntityDescribeMap().get(entity);
        }
        if (result == null) {
            result = runOperation(DESCRIBE_SOBJECT_OPERATION, entity);
            if (result != null) {
                getCachedEntityDescribeMap().put(result.getName(), result);
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

    public Field getField(String sObjectFieldName) {
        Field field = this.fieldsByName.get(sObjectFieldName);
        if (field == null) {
            field = lookupField(sObjectFieldName);
            this.fieldsByName.put(sObjectFieldName, field);
        }
        return field;
    }

    private Field lookupField(String sObjectFieldName) {
        ParentIdLookupFieldFormatter parentLookupFieldFormatter = null;
        try {
            parentLookupFieldFormatter = new ParentIdLookupFieldFormatter(sObjectFieldName);
        } catch (RelationshipFormatException ex) {
            // ignore
        }
        // look for field on target object
        for (Field f : getFieldTypes().getFields()) {
            if (sObjectFieldName.equalsIgnoreCase(f.getName()) || sObjectFieldName.equalsIgnoreCase(f.getLabel())) {
                return f;
            }
            if (parentLookupFieldFormatter != null) {
                if (!parentLookupFieldFormatter.getParent().getRelationshipName().equalsIgnoreCase(f.getRelationshipName())) {
                    continue;
                }
                Field parentField = this.referenceEntitiesDescribesMap.getParentField(sObjectFieldName);
                if (parentField != null) {
                    return parentField;
                }
                // need to add the relationship mapping to referenceEntitiesDescribesMap
                try {
                    processParentObjectForLookupReferences(parentLookupFieldFormatter.getParent().getParentObjectName(), f, 0, 1);
                } catch (ConnectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return this.referenceEntitiesDescribesMap.getParentField(sObjectFieldName);
            }
        }
        return this.referenceEntitiesDescribesMap.getParentField(sObjectFieldName);
    }

}
