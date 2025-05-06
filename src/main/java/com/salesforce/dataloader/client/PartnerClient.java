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

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UndeleteResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import org.apache.commons.beanutils.DynaBean;
import org.apache.logging.log4j.Logger;
import com.salesforce.dataloader.util.DLLogManager;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PartnerClient extends ClientBase<PartnerConnection> {

    private static Logger LOG = DLLogManager.getLogger(PartnerClient.class);
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
            return getConnection().upsert(appConfig.getString(AppConfig.PROP_IDLOOKUP_FIELD), sObjects);
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
            setExportBatchSize();
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
            setExportBatchSize();
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
            setExportBatchSize();
            return getConnection().queryMore(queryString);
        }
    };

    private PartnerClient(Controller controller) {
        super(controller, LOG);
    }

    private void setExportBatchSize() {
        // query header
        int querySize = AppConfig.DEFAULT_EXPORT_BATCH_SIZE;
        try {
            querySize = appConfig.getInt(AppConfig.PROP_EXPORT_BATCH_SIZE);
        } catch (ParameterLoadException e) {
            querySize = AppConfig.DEFAULT_EXPORT_BATCH_SIZE;
        }
        if (querySize <= AppConfig.MIN_EXPORT_BATCH_SIZE) {
            querySize = AppConfig.MIN_EXPORT_BATCH_SIZE;
        }
        if (querySize > AppConfig.MAX_EXPORT_BATCH_SIZE) {
            querySize = AppConfig.MAX_EXPORT_BATCH_SIZE;
        }
        getConnection().setQueryOptions(querySize);
    }
    @Override
    protected boolean connectPostLogin(ConnectorConfig cc) {
        if (getConnection() == null)
            throw new IllegalStateException("Client should be logged in already");

        getConnection().setCallOptions(ClientBase.getClientName(this.appConfig), null);
        setExportBatchSize();
        
        // assignment rule for update
        if (appConfig.getString(AppConfig.PROP_ASSIGNMENT_RULE).length() > 14) {
            String rule = appConfig.getString(AppConfig.PROP_ASSIGNMENT_RULE);
            if (rule.length() > 15) {
                rule = rule.substring(0, 15);
            }
            getConnection().setAssignmentRuleHeader(rule, false);
        }

        // field truncation
        getConnection().setAllowFieldTruncationHeader(appConfig.getBoolean(AppConfig.PROP_TRUNCATE_FIELDS));

        // TODO: make this configurable
        getConnection().setDisableFeedTrackingHeader(true);

        getConnection().setDuplicateRuleHeader(
            appConfig.getBoolean(AppConfig.PROP_DUPLICATE_RULE_ALLOW_SAVE),
            appConfig.getBoolean(AppConfig.PROP_DUPLICATE_RULE_INCLUDE_RECORD_DETAILS),
            appConfig.getBoolean(AppConfig.PROP_DUPLICATE_RULE_RUN_AS_CURRENT_USER)
        );

        return true;
    }
    
    public PartnerConnection getConnection() {
    	PartnerConnection conn = super.getConnection();
    	if (conn == null) {
    		conn = controller.getLoginClient().getConnection();
    		super.setConnection(conn);
    	}
        return conn;
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
            SObject[] sobjects = SforceDynaBean.getSObjectArray(controller, dynaBeans, appConfig.getString(AppConfig.PROP_ENTITY),
                    appConfig.getBoolean(AppConfig.PROP_INSERT_NULLS));
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
                    startRow = appConfig.getInt(AppConfig.PROP_LOAD_ROW_TO_START_AT);
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
    public ConnectorConfig getConnectorConfig() {
        ConnectorConfig cc = super.getConnectorConfig();
        cc.setManualLogin(true);
        return cc;
    }
    
    public static String getServicePath() {
        return "/services/Soap/u/" + getAPIVersionForTheSession() + "/";
    }

	@Override
	public boolean logout() {
		instance = null;
		return true;
	}
	
	private static PartnerClient instance = null;
	public static PartnerClient getInstance(Controller controller) {
		if (instance == null || instance.controller != controller) {
			instance = new PartnerClient(controller);
		}
		return instance;
	}

}
