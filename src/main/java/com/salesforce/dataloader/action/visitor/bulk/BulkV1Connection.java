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
package com.salesforce.dataloader.action.visitor.bulk;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.client.ClientBase;
import com.salesforce.dataloader.client.transport.HttpTransportImpl;
import com.salesforce.dataloader.client.transport.HttpTransportInterface;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.HttpClientTransportException;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.async.BatchInfoList;
import com.sforce.async.BulkConnection;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.QueryResultList;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;

public class BulkV1Connection extends BulkConnection {
    private static Logger logger = DLLogManager.getLogger(BulkV1Connection.class);

    public BulkV1Connection(ConnectorConfig config) throws AsyncApiException {
        super(config);
        
        // This is needed to set the correct client name in Bulk V1 calls
        addHeader(ClientBase.SFORCE_CALL_OPTIONS_HEADER, config.getRequestHeader(ClientBase.SFORCE_CALL_OPTIONS_HEADER));
    }
    
    public void addHeader(String headerName, String headerValue) {
        super.addHeader(headerName, headerValue);
        if (ClientBase.SFORCE_CALL_OPTIONS_HEADER.equalsIgnoreCase(headerName)) {
            logger.debug(ClientBase.SFORCE_CALL_OPTIONS_HEADER + " : " + headerValue);
        }
    }
    
    public JobInfo getJobStatus(String jobId) throws AsyncApiException {
        return getJobStatus(jobId, ContentType.XML);
    }

    public JobInfo getJobStatus(String jobId, ContentType contentType) throws AsyncApiException {
        if (AppConfig.getCurrentConfig().getBoolean(AppConfig.PROP_USE_LEGACY_HTTP_GET)) {
            return super.getJobStatus(jobId, contentType);
        } else {
            String[] urlParts = {"job", jobId};
            InputStream in = invokeBulkV1GET(urlParts);
            return processBulkV1Get(in, contentType, JobInfo.class);
        }
    }
    
    public BatchInfoList getBatchInfoList(String jobId) throws AsyncApiException {
        return getBatchInfoList(jobId, ContentType.XML);
    }
    
    public BatchInfoList getBatchInfoList(String jobId, ContentType contentType) throws AsyncApiException {
        if (AppConfig.getCurrentConfig().getBoolean(AppConfig.PROP_USE_LEGACY_HTTP_GET)) {
            return super.getBatchInfoList(jobId, contentType);
        } else {
            String[] urlParts = {"job", jobId, "batch"};
            InputStream in = invokeBulkV1GET(urlParts);
            return processBulkV1Get(in, contentType, BatchInfoList.class);
        }
    }
    
    public InputStream getBatchResultStream(String jobId, String batchId) throws AsyncApiException {
        if (AppConfig.getCurrentConfig().getBoolean(AppConfig.PROP_USE_LEGACY_HTTP_GET)) {
            return super.getBatchResultStream(jobId, batchId);
        } else {
            String[] urlParts = {"job", jobId, "batch", batchId, "result"};
            return invokeBulkV1GET(urlParts);
        }
    }
    
    public QueryResultList getQueryResultList(String jobId, String batchId) throws AsyncApiException {
        return getQueryResultList(jobId, batchId, ContentType.XML);
    }

    public QueryResultList getQueryResultList(String jobId, String batchId, ContentType contentType) throws AsyncApiException {
        if (AppConfig.getCurrentConfig().getBoolean(AppConfig.PROP_USE_LEGACY_HTTP_GET)) {
            return super.getQueryResultList(jobId, batchId, contentType);
        } else {
            InputStream in = getBatchResultStream(jobId, batchId);
            return processBulkV1Get(in, contentType, QueryResultList.class);
        }
    }

    public InputStream getQueryResultStream(String jobId, String batchId, String resultId) throws AsyncApiException {
        if (AppConfig.getCurrentConfig().getBoolean(AppConfig.PROP_USE_LEGACY_HTTP_GET)) {
            return super.getQueryResultStream(jobId, batchId, resultId);
        } else {
            String[] urlParts = {"job", jobId, "batch", batchId, "result", resultId};
            return invokeBulkV1GET(urlParts);
        }
    }
    
    private InputStream invokeBulkV1GET(String[] urlParts) throws AsyncApiException {
        String endpoint = getConfig().getRestEndpoint();
        endpoint = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        if (urlParts != null) {
            for (String urlPart : urlParts) {
                endpoint += urlPart + "/";
            }
        }
        try {
            HttpTransportInterface transport = HttpTransportImpl.getInstance();
            transport.setConfig(getConfig());
            return transport.httpGet(endpoint);
        } catch (IOException | HttpClientTransportException e) {
            logger.error(e.getMessage());
            throw new AsyncApiException("Failed to get result ", AsyncExceptionCode.ClientInputError, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T processBulkV1Get(InputStream is, ContentType contentType, Class<T> returnClass) throws AsyncApiException {
        try {
            if (contentType == ContentType.JSON || contentType == ContentType.ZIP_JSON) {
                return AppUtil.deserializeJsonToObject(is, returnClass);
            } else {
                XmlInputStream xin = new XmlInputStream();
                xin.setInput(is, "UTF-8");
                Constructor<?>[] ctors = returnClass.getDeclaredConstructors();
                Constructor<?> ctor = null;
                for (int i = 0; i < ctors.length; i++) {
                    ctor = ctors[i];
                    if (ctor.getGenericParameterTypes().length == 0)
                    break;
                }
                if (ctor == null) {
                    throw new AsyncApiException("Failed to get result: " + returnClass + " cannot be instantiated", AsyncExceptionCode.ClientInputError);
                }
                T result = (T)ctor.newInstance();
                Method loadMethod = returnClass.getMethod("load", xin.getClass(), typeMapper.getClass());
                loadMethod.invoke(result, xin, typeMapper);
                return result;
            }
        } catch (IOException | PullParserException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            logger.error(e.getMessage());
            throw new AsyncApiException("Failed to get result ", AsyncExceptionCode.ClientInputError, e);
        }
    }
}