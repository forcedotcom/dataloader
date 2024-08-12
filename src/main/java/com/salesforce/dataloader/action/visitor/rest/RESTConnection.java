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
package com.salesforce.dataloader.action.visitor.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.salesforce.dataloader.client.HttpClientTransport;
import com.salesforce.dataloader.client.HttpTransportInterface;
import com.salesforce.dataloader.client.SessionInfo;
import com.salesforce.dataloader.client.CompositeRESTClient.ACTION_ENUM;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.StatusCode;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class RESTConnection {
    private ConnectorConfig connectorConfig;
    private Controller controller;
    private static Logger logger = LogManager.getLogger(RESTConnection.class);

    public RESTConnection(ConnectorConfig config, Controller controller) throws AsyncApiException {
        this.connectorConfig = config;
        this.controller = controller;
    }


    @SuppressWarnings("unchecked")
    public SaveResult[] loadAction(SessionInfo session, ACTION_ENUM action, List<DynaBean> dynabeans) throws ConnectionException {
        String actionStr = "update"; // default
        switch (action) {
            case DELETE:
                actionStr = "delete";
                break;
            default:
                actionStr = "update";
                break;
        }
        logger.debug(Messages.getFormattedString("Client.beginOperation", actionStr)); //$NON-NLS-1$
        ConnectionException connectionException = null;
        try {
            Map<String, Object> batchRecords = this.getSobjectMapForCompositeREST(dynabeans, "update");
            String json = "";
            try {
                json = AppUtil.serializeToJson(batchRecords);
                logger.debug("JSON for batch update using Composite REST:\n" + json);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage());
                throw new ConnectionException(e.getMessage());
            }
            
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/JSON");
            headers.put("ACCEPT", "application/JSON");
            headers.put("Authorization", "Bearer " + session.getSessionId());
            String lookupFieldName = Config.getCurrentConfig().getString(Config.IDLOOKUP_FIELD);
            if (lookupFieldName == null || lookupFieldName.isBlank()) {
                lookupFieldName = "id";
            }
            if (!"id".equalsIgnoreCase(lookupFieldName) 
                    && Controller.getAPIMajorVersion() < 61) {
                String message = "update operation does not support referencing objects using a non-id field such as " 
                        + lookupFieldName + " for API version 60 or lower. The current API version is " 
                        + Controller.getAPIVersion();
                logger.error(message);
                throw new ConnectionException(message);
            }
            HttpClientTransport transport = new HttpClientTransport(this.connectorConfig);
    
            // assume update operation by default and set http method value to PATCH
            HttpTransportInterface.SupportedHttpMethodType httpMethod = HttpTransportInterface.SupportedHttpMethodType.PATCH;
            if (action == ACTION_ENUM.DELETE) {
                httpMethod = HttpTransportInterface.SupportedHttpMethodType.DELETE;
            }
            try {
                OutputStream out = transport.connect(
                        connectorConfig.getRestEndpoint() 
                            + controller.getConfig().getString(Config.ENTITY) 
                            + "/" + lookupFieldName + "/"
                            + "?updateOnly=true",
                        headers,
                        true,
                        httpMethod);
                out.write(json.getBytes(StandardCharsets.UTF_8.name()));
                out.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new ConnectionException(e.getMessage());
            }
            InputStream in = null;
            try {
                in = transport.getContent();
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new ConnectionException(e.getMessage());
            }
            boolean successfulRequest = transport.isSuccessful();
            ArrayList<SaveResult> resultList = new ArrayList<SaveResult>();
            if (successfulRequest) {
                Object[] jsonResults = null;
                try {
                    jsonResults = AppUtil.deserializeJsonToObject(in, Object[].class);
                } catch (IOException e) {
                    logger.warn("Composite REST returned no results - " + e.getMessage());
                    throw new ConnectionException(e.getMessage());
                }
                for (Object result : jsonResults) {
                    Map<String, Object> resultMap = (Map<String, Object>)result;
                    SaveResult resultToSave = new SaveResult();
                    resultToSave.setId((String)resultMap.get("id"));
                    resultToSave.setSuccess(((Boolean)resultMap.get("success")).booleanValue());
                    List<Map<String, Object>> errorResultsArray = (List<Map<String, Object>>)resultMap.get("errors");
                    ArrayList<com.sforce.soap.partner.Error> errorList = new ArrayList<com.sforce.soap.partner.Error>();
                    if (errorResultsArray != null) {
                        for (Map<String, Object> errorMap : errorResultsArray) {
                            com.sforce.soap.partner.Error error = new com.sforce.soap.partner.Error();
                            String codeStr = StatusCode.valuesToEnums.get((String)errorMap.get("statusCode"));
                            StatusCode statusCode = StatusCode.valueOf(codeStr);
                            error.setStatusCode(statusCode);
                            error.setMessage((String) errorMap.get("message"));
                            List<String> fieldsList = (List<String>) errorMap.get("fields");
                            error.setFields(fieldsList.toArray(new String[1]));
                            errorList.add(error);
                        }
                        resultToSave.setErrors(errorList.toArray(new com.sforce.soap.partner.Error[1]));
                    }
                    resultList.add(resultToSave);
                }
            } else {
                try {
                    String resultStr = IOUtils.toString(in, StandardCharsets.UTF_8);
                    logger.warn(resultStr);
                } catch (IOException e) {
                    logger.warn(e.getMessage());
                }
            }
            session.performedSessionActivity(); // reset session activity timer
            return resultList.toArray(new SaveResult[0]);
        } catch (ConnectionException ex) {
            logger.error(
                    Messages.getFormattedString(
                            "Client.operationError", new String[]{actionStr, ex.getMessage()}), ex); //$NON-NLS-1$
            if (ex instanceof ApiFault) {
                ApiFault fault = (ApiFault)ex;
                String faultMessage = fault.getExceptionMessage();
                logger.error(
                        Messages.getFormattedString(
                                "Client.operationError", new String[]{actionStr, faultMessage}), fault); //$NON-NLS-1$
            }
            connectionException = ex;
        }
        throw connectionException;
    }
    
    private Map<String, Object> getSobjectMapForCompositeREST(List<DynaBean> dynaBeans, String opName) {
        try {
            List<Map<String, Object>> sobjectList = SforceDynaBean.getRESTSObjectArray(controller, dynaBeans, controller.getConfig().getString(Config.ENTITY),
                    controller.getConfig().getBoolean(Config.INSERT_NULLS));
            HashMap<String, Object> recordsMap = new HashMap<String, Object>();
            recordsMap.put("records", sobjectList);
            recordsMap.put("allOrNone", false);
            return recordsMap;
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

}
