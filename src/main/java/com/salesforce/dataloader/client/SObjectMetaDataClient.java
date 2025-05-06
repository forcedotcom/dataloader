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



import com.salesforce.dataloader.action.OperationInfo;

/**
 * The sfdc api client class - implemented using the partner wsdl
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dyna.ParentIdLookupFieldFormatter;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.RelationshipFormatException;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.salesforce.dataloader.model.TableRow;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import org.apache.logging.log4j.Logger;
import com.salesforce.dataloader.util.DLLogManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SObjectMetaDataClient extends ClientBase<PartnerConnection> {

    private static Logger LOG = DLLogManager.getLogger(SObjectMetaDataClient.class);

    private static interface ClientOperation<RESULT, ARG> {
        String getName();

        RESULT run(ARG arg) throws ConnectionException;
    }

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
    private ReferenceEntitiesDescribeMap referenceEntitiesDescribesMap = new ReferenceEntitiesDescribeMap(this);
    private final Map<String, DescribeGlobalSObjectResult> describeGlobalResultsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, DescribeSObjectResult> entityFieldDescribesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, ReferenceEntitiesDescribeMap> parentDescribeCache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private SObjectMetaDataClient(Controller controller) {
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

    protected <R, A> R runOperation(ClientOperation<R, A> op, A arg) throws ConnectionException {
        logger.debug(Messages.getFormattedString("Client.beginOperation", op.getName())); //$NON-NLS-1$
        if (!controller.getLoginClient().isSessionValid()) {
        	controller.getLoginClient().connect();
        }
        ConnectionException connectionException = null;
        try {
            R result = op.run(arg);
            if (result == null)
                logger.info(Messages.getString("Client.resultNull")); //$NON-NLS-1$
            this.getSession().performedSessionActivity(); // reset session activity timer
            return result;
        } catch (ConnectionException ex) {
            String exceptionMessage = ex.getMessage();
            if (ex instanceof LoginFault) {
                LoginFault lf = (LoginFault)ex;
                exceptionMessage = lf.getExceptionMessage();
            }

            logger.error(
                    Messages.getFormattedString(
                            "Client.operationError", new String[]{op.getName(), exceptionMessage}), ex); //$NON-NLS-1$
            if (ex instanceof ApiFault) {
                ApiFault fault = (ApiFault)ex;
                String faultMessage = fault.getExceptionMessage();
                logger.error(
                        Messages.getFormattedString(
                                "Client.operationError", new String[]{op.getName(), faultMessage}), fault); //$NON-NLS-1$

            }
            // check retries
            connectionException = ex;
        }
        throw connectionException;
    }


    public Map<String, DescribeGlobalSObjectResult> getDescribeGlobalResults() {
        if (this.describeGlobalResults == null || !appConfig.getBoolean(AppConfig.PROP_CACHE_DESCRIBE_GLOBAL_RESULTS)) {
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
        String entity = this.appConfig.getString(AppConfig.PROP_ENTITY);
        try {
            return describeSObject(entity);
        } catch (ConnectionException e) {
            throw new RuntimeException("Unexpected failure describing main entity " + entity, e);
        }
    }

    public ReferenceEntitiesDescribeMap getReferenceDescribes() {
        return referenceEntitiesDescribesMap;
    }
    
    /**
     * Set the map of references to object external id info for current entity
     *
     * @throws ConnectionException
     */
    public void setFieldReferenceDescribes() throws ConnectionException {
        if (getFieldTypes() == null) {
            setFieldTypes();
        }
        Collection<String> mappedSFFields = null;
        if (getDescribeGlobalResults() != null) {
            String operation = appConfig.getString(AppConfig.PROP_OPERATION);
            if (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.BATCH
                    && operation != null
                    && !operation.isBlank()
                    && appConfig.getOperationInfo() != OperationInfo.extract
                    && appConfig.getOperationInfo() != OperationInfo.extract_all) {
                // import operation in batch mode
                LoadMapper mapper = (LoadMapper)controller.getMapper();
                // call describe only for mapped fields
                mappedSFFields = mapper.getDestColumns();
            }
            setFieldReferenceDescribes(mappedSFFields);
        }
    }
    
    public void setFieldReferenceDescribes(Collection<String> sfFields) throws ConnectionException {
        if (appConfig.getBoolean(AppConfig.PROP_CACHE_DESCRIBE_GLOBAL_RESULTS)) {
            referenceEntitiesDescribesMap = parentDescribeCache.get(appConfig.getString(AppConfig.PROP_ENTITY));
        } else {
            referenceEntitiesDescribesMap.clear();
        }
        if (referenceEntitiesDescribesMap == null) {
            referenceEntitiesDescribesMap = new ReferenceEntitiesDescribeMap(this);
        }
        if (referenceEntitiesDescribesMap.size() > 0) {
            return; // use cached value
        }
        Field[] entityFields = getFieldTypes().getFields();
        boolean useMappedLookupRelationshipNamesForRefDescribes = false;
        ArrayList<String> relFieldsNeedingRefDescribes = new ArrayList<String>();
        if (sfFields != null && !sfFields.isEmpty()) {
            useMappedLookupRelationshipNamesForRefDescribes = true;
            for (String mappedFieldList : sfFields) {
                String[]mappedFields = mappedFieldList.split(",");
                for (String field : mappedFields) {
                    try {
                        ParentIdLookupFieldFormatter lookupFieldFormatter = new ParentIdLookupFieldFormatter(field);
                        if (lookupFieldFormatter.getParent() != null
                            && lookupFieldFormatter.getParentFieldName() != null) {
                            String relationshipNameInMappedField = lookupFieldFormatter.getParent().getRelationshipName();
                            if (relationshipNameInMappedField == null) {
                                useMappedLookupRelationshipNamesForRefDescribes = false;
                                break;
                            } else {
                                relFieldsNeedingRefDescribes.add(relationshipNameInMappedField);
                            }
                        }
                    } catch (RelationshipFormatException e) {
                     // do not optimize getting lookup field describes
                        useMappedLookupRelationshipNamesForRefDescribes = false;
                        break;
                    }
                }
                if (!useMappedLookupRelationshipNamesForRefDescribes) {
                    relFieldsNeedingRefDescribes.clear();
                    break;
                }
            }
        }

        for (Field childObjectField : entityFields) {
            // verify that the sobject field represents a relationship field where
            // the sobject is a child with one or more parent sobjects.
            String[] parentObjectNames = childObjectField.getReferenceTo();
            String relationshipName = childObjectField.getRelationshipName();
            if (parentObjectNames == null || parentObjectNames.length == 0 || parentObjectNames[0] == null
                || relationshipName == null || relationshipName.length() == 0
                || (!childObjectField.isCreateable() && !childObjectField.isUpdateable())) {
                // parent-child relationship either does not exist or
                // it is neither modifiable nor updateable.
                continue;
            }
            if (!useMappedLookupRelationshipNamesForRefDescribes
                || relFieldsNeedingRefDescribes.contains(relationshipName)) {
                processParentObjectArrayForLookupReferences(parentObjectNames, childObjectField);
            }
        }
        if (appConfig.getBoolean(AppConfig.PROP_CACHE_DESCRIBE_GLOBAL_RESULTS)
            && sfFields == null) {
            // got the full list of parents' describes for an sobject
            parentDescribeCache.put(appConfig.getString(AppConfig.PROP_ENTITY), referenceEntitiesDescribesMap);
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
        describeSObject(appConfig.getString(AppConfig.PROP_ENTITY));
    }

    public DescribeSObjectResult describeSObject(String entity) throws ConnectionException {
        DescribeSObjectResult result = null;
        if (appConfig.getBoolean(AppConfig.PROP_CACHE_DESCRIBE_GLOBAL_RESULTS)) {
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
    
    public Field[] getSObjectFieldAttributesForRow(String sObjectName, TableRow dataRow) throws ConnectionException {
        ArrayList<Field> attributesForRow = new ArrayList<Field>();
        DescribeSObjectResult entityDescribe = describeSObject(sObjectName);
        for (String headerColumnName : dataRow.getHeader().getColumns()) {
            Field[] fieldAttributesArray = entityDescribe.getFields();
            for (Field fieldAttributes : fieldAttributesArray) {
                if (fieldAttributes.getName().equalsIgnoreCase(headerColumnName)) {
                    attributesForRow.add(fieldAttributes);
                }
            }
        }
        return attributesForRow.toArray(new Field[1]);
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
    
    public Field getFieldFromRelationshipName(String relationshipName) {
        for (Field f : getFieldTypes().getFields()) {
            if (f.getReferenceTo().length > 0) {
                String relName = f.getRelationshipName();
                if (relName != null
                        && !relName.isBlank()
                        && relName.equalsIgnoreCase(relationshipName)) {
                    return f;
                }
            }
        }
        return null;
    }
    
    // sObjectFieldName could be sObject's field name
    // or a reference to parent sObject's field name in the old or new format.
    private Field lookupField(String sObjectFieldName) {
        for (Field f : getFieldTypes().getFields()) {
            if (sObjectFieldName.equalsIgnoreCase(f.getName()) || sObjectFieldName.equalsIgnoreCase(f.getLabel())) {
                return f;
            }
            ParentIdLookupFieldFormatter parentLookupFieldFormatter = null;
            try {
                parentLookupFieldFormatter = new ParentIdLookupFieldFormatter(sObjectFieldName);
            } catch (RelationshipFormatException ex) {
                // ignore
            }
            if (parentLookupFieldFormatter != null) {
                if (!parentLookupFieldFormatter.getParent().getRelationshipName().equalsIgnoreCase(f.getRelationshipName())) {
                    continue;
                }
                Field parentField = this.referenceEntitiesDescribesMap.getParentField(sObjectFieldName);
                if (parentField != null) {
                    return parentField;
                }
                String parentSObjectName = parentLookupFieldFormatter.getParent().getParentObjectName();
                if (parentSObjectName == null || parentSObjectName.isBlank()) {
                    parentSObjectName = f.getReferenceTo()[0];
                }
                if (parentSObjectName == null || parentSObjectName.isBlank()) {
                    // something is wrong for a relationship field
                    logger.error("Field " + f.getName() + " does not have a parent sObject");
                    continue;
                }
                // need to add the relationship mapping to referenceEntitiesDescribesMap
                try {
                    processParentObjectForLookupReferences(parentSObjectName, f, 0, 1);
                } catch (ConnectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return this.referenceEntitiesDescribesMap.getParentField(sObjectFieldName);
            }
        }
        return this.referenceEntitiesDescribesMap.getParentField(sObjectFieldName);
    }

	@Override
	public boolean logout() {
		instance = null;
		return true;
	}
	
	private static SObjectMetaDataClient instance = null;
	public static SObjectMetaDataClient getInstance(Controller controller) {
		if (instance == null || instance.controller != controller) {
			instance = new SObjectMetaDataClient(controller);
		}
		return instance;
	}

}