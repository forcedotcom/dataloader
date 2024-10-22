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

/**********************************
 * A code snippet showing Bulk v2 Ingest using BulkV2Connection. 
 * Requires dataloader-<version>-uber.jar in the classpath to compile.
 * 

import java.net.URL;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.salesforce.dataloader.action.visitor.BulkV2Connection;
import com.salesforce.dataloader.client.HttpClientTransport;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectorConfig;

public class TestBulkV2 {
    public static void main(String[] args) {
        String insertFilename = "./insertAccountCsv.csv";
        String deleteFilename = "./deleteAccountCsv.csv";
        String successFilename = "./ingestSuccessResults.csv";
        failureFilename = "./ingestFailureResults.csv";
        String unprocessedFilename = "./ingestUnprocessedRecords.csv";
        String bulkQueryResultsFilename = "./queryResults.csv";
        String username = "";
        String password = "";
		static final String myDomainURLString = "https://<mydomain prefix>.my.salesforce.com";
		static final String restEndpoint = myDomainURLString + "/services/data/v52.0/jobs/";

        try {
                URL DEFAULT_AUTH_ENDPOINT_URL = new URL(Connector.END_POINT);
                URL serverUrl = new URL(myDomainURLString);

                ConnectorConfig cc = new ConnectorConfig();
                cc.setTransport(HttpClientTransport.class);
                cc.setUsername(username);
                cc.setPassword(password);
                cc.setAuthEndpoint(serverUrl + DEFAULT_AUTH_ENDPOINT_URL.getPath());
                cc.setServiceEndpoint(serverUrl + DEFAULT_AUTH_ENDPOINT_URL.getPath());
                cc.setRestEndpoint(restEndpoint);
                final PartnerConnection conn = Connector.newConnection(cc);
                
                // bulkv2 insert
                BulkV2Connection v2conn = new BulkV2Connection(cc);
                JobInfo job = executeJob("account", OperationEnum.insert, v2conn, insertFilename);
                v2conn.saveIngestSuccessResults(job.getId(), successFilename);
                v2conn.saveIngestFailureResults(job.getId(), failureFilename);
                v2conn.saveIngestUnprocessedRecords(job.getId(), unprocessedFilename);
                
	            // bulkv2 query
		        job = new JobInfo();
		        job.setOperation(OperationEnum.query);
		        job.setObject("account");
		        job.setContentType(ContentType.CSV);
	            job.setObject("select id from Account");
	            job = v2conn.createJob(job);
	            // wait for the job to complete
	            while (job.getState() != JobStateEnum.JobComplete) {
	            	Thread.sleep(10,000);
	            	job = v2conn.getExtractJobStatus(job.getId());
	            }
	            // download query results
	            BufferedOutputStream csvFileStream = new BufferedOutputStream(new FileOutputStream(bulkQueryResultsFilename));
	            String locator = v2conn.getQueryLocator();
	            while (!"null".equalsIgnoreCase(locator)) {
	                BufferedInputStream resultsStream = new BufferedInputStream(v2conn.getQueryResultStream(job.getId(), locator));
	                writeTo(resultsStream, csvFileStream);
	                resultsStream.close();
	                locator = v2conn.getQueryLocator();
	            }
	            csvFileStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
    }

    private static JobInfo executeJob(String objectName, OperationEnum operation,
                BulkV2Connection v2conn, String ingestFilename) throws Exception {
        JobInfo job = new JobInfo();
        job.setObject(objectName);
        job.setOperation(operation);
        job = v2conn.createJob(job);
        job = v2conn.startIngest(job.getId(), ingestFilename);
        while (job.getState() != JobStateEnum.JobComplete) {
                Thread.sleep(10,000);
                job = v2conn.getIngestJobStatus(job.getId());
        }
        return job;
    }
    
    private static void writeTo(BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
        byte[] buffer = new byte[2048];
        for(int len; (len = bis.read(buffer)) > 0;) {
            bos.write(buffer, 0, len);
        }
    }
}
 **************************/

package com.salesforce.dataloader.action.visitor.bulk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.client.ClientBase;
import com.salesforce.dataloader.client.HttpClientTransport;
import com.salesforce.dataloader.client.HttpTransportInterface;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.HttpClientTransportException;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.AppUtil.OSType;
import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.async.BulkConnection;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;

enum HttpMethod {
    GET,
    POST,
    PATCH,
    PUT
}

public class BulkV2Connection extends BulkConnection {
    private static final String URI_STEM_QUERY = "query/";
    private static final String URI_STEM_INGEST = "ingest/";
    private static final String AUTH_HEADER = "Authorization";
    private static final String REQUEST_CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCEPT_CONTENT_TYPES_HEADER = "ACCEPT";
    private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer ";
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private static final String INGEST_RESULTS_SUCCESSFUL = "successfulResults";
    private static final String INGEST_RESULTS_UNSUCCESSFUL = "failedResults";
    private static final String INGEST_RECORDS_UNPROCESSED = "unprocessedrecords";

    private String queryLocator = "";
    private int numberOfRecordsInQueryResult = 0;
    private HashMap<String, String> headers = new HashMap<String, String>();
    private Controller controller = null;
    
    private static Logger logger = DLLogManager.getLogger(BulkV2Connection.class);

    /**********************************
     * 
     * public, common methods 
     * 
     **********************************/
    public BulkV2Connection(ConnectorConfig connectorConfig, Controller controller) throws AsyncApiException {
        super(connectorConfig);
        this.controller = controller;
    }
    
    public JobInfo getJobStatus(String jobId) throws AsyncApiException {
        return getJobStatus(jobId, ContentType.JSON);
    }
    
    public JobInfo closeJob(String jobId) throws AsyncApiException {
        return getJobStatus(jobId);
    }
    
    public JobInfo getJobStatus(String jobId, ContentType contentType) throws AsyncApiException {
        String urlString = constructRequestURL(jobId);
        HashMap<String, String> headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);
    	// there is nothing in the request body.
    	return doSendJobRequestToServer(urlString, 
										headers,
										HttpMethod.GET,
										ContentType.JSON,
										null,
										true,
										"Failed to get job status for job " + jobId);
    }
    
    public JobInfo abortJob(String jobId, boolean isQuery) throws AsyncApiException {
        return setJobState(jobId, isQuery, JobStateEnum.Aborted, "Failed to abort job " + jobId);
    }
    
    public JobInfo setJobState(String jobId, boolean isQuery, JobStateEnum state, String errorMessage) throws AsyncApiException {
        String urlString = constructRequestURL(jobId);
        HashMap<String, String> headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);
    	HashMap<String, Object> requestBodyMap = new HashMap<String, Object>();
    	requestBodyMap.put("state", state.toString());

        return doSendJobRequestToServer(urlString, 
										headers,
										HttpMethod.PATCH,
										ContentType.JSON,
										requestBodyMap,
										true,
										errorMessage);
    }
    
    /**********************************
     * 
     * public, extract (aka query) methods 
     * 
     **********************************/
    public JobInfo getExtractJobStatus(String jobId) throws AsyncApiException {
        return getJobStatus(jobId);
    }        

    public InputStream getQueryResultStream(String jobId, String locator) throws AsyncApiException {
    	String urlString =  constructRequestURL(jobId) + "results/";
        if (locator != null && !locator.isEmpty() && !"null".equalsIgnoreCase(locator)) {
        	urlString += "?locator=" + locator;
        }
        try {
            return doGetQueryResultStream(new URL(urlString), getHeaders(JSON_CONTENT_TYPE, CSV_CONTENT_TYPE));
        } catch (IOException | ConnectionException e) {
            throw new AsyncApiException("Failed to get query results for job " + jobId, AsyncExceptionCode.ClientInputError, e);
        }
    }
    
    public String getQueryLocator() {
        return this.queryLocator;
    }
    
    public int getNumberOfRecordsInQueryResult() {
        return this.numberOfRecordsInQueryResult;
    }
    
    /**********************************
     * 
     * public, ingest (create, update, upsert, delete) methods 
     * 
     **********************************/
    // needed for all upload operations (non-query operations)	
    public JobInfo startIngest(String jobId, InputStream bulkUploadStream) throws AsyncApiException {
        String urlString = constructRequestURL(jobId) + "batches/";
        HashMap<String, String> headers = getHeaders(CSV_CONTENT_TYPE, JSON_CONTENT_TYPE);
        try {
        	HttpClientTransport clientTransport = HttpClientTransport.getInstance();
        	clientTransport.setConfig(getConfig());
        	clientTransport.connect(urlString, headers, false, HttpTransportInterface.SupportedHttpMethodType.PUT, bulkUploadStream, CSV_CONTENT_TYPE);

            // Following is needed to actually send the request to the server
            InputStream serverResponseStream = clientTransport.getContent();
            if (!clientTransport.isSuccessful()) {
	            parseAndThrowException(serverResponseStream, ContentType.JSON);
            }
        }catch (IOException e) {
            throw new AsyncApiException("Failed to upload to server for job " + jobId, AsyncExceptionCode.ClientInputError, e);
        }
        
        // Mark upload as completed
        urlString = constructRequestURL(jobId);
        headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);

    	setJobState(jobId, false, JobStateEnum.UploadComplete, "Failed to mark completion of the upload");
    	return getIngestJobStatus(jobId);
    }
    
    public JobInfo getIngestJobStatus(String jobId) throws AsyncApiException {
        return getJobStatus(jobId);
    }
    
    public void saveIngestSuccessResults(String jobId, String filename) throws AsyncApiException {
    	doSaveIngestResults(jobId, filename, INGEST_RESULTS_SUCCESSFUL, false);
    }
    
    public void saveIngestFailureResults(String jobId, String filename) throws AsyncApiException {
    	doSaveIngestResults(jobId, filename, INGEST_RESULTS_UNSUCCESSFUL, true);
    }
    
    public void saveIngestUnprocessedRecords(String jobId, String filename) throws AsyncApiException {
    	doSaveIngestResults(jobId, filename, INGEST_RECORDS_UNPROCESSED, false);
    }
    
    public InputStream getIngestSuccessResultsStream(String jobId) throws AsyncApiException {
    	return doGetIngestResultsStream(jobId, INGEST_RESULTS_SUCCESSFUL);
    }

    public InputStream getIngestFailedResultsStream(String jobId) throws AsyncApiException {
    	return doGetIngestResultsStream(jobId, INGEST_RESULTS_UNSUCCESSFUL);
    }

    public InputStream getIngestUnprocessedRecordsStream(String jobId) throws AsyncApiException {
    	return doGetIngestResultsStream(jobId, INGEST_RECORDS_UNPROCESSED);
    }
    
    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }
    
    /**********************************
     * 
     * private, common methods 
     * 
     **********************************/
    private String constructRequestURL(String jobId) {
        String urlString = getConfig().getRestEndpoint();
        if (jobId == null) {
        	jobId = "";
        }
        boolean isExtraction = controller.getAppConfig().getOperationInfo().isExtraction();
        if (isExtraction) {
        	urlString += URI_STEM_QUERY + jobId + "/";
        } else {
        	urlString += URI_STEM_INGEST + jobId + "/";
        }
        return urlString;
    }
    
    public JobInfo createJob(JobInfo job) throws AsyncApiException {
        ContentType type = job.getContentType();
        if (type != null && type != ContentType.CSV) {
            throw new AsyncApiException("Unsupported Content Type", AsyncExceptionCode.FeatureNotEnabled);
        }
        OperationEnum operation = job.getOperation();
        String urlString = constructRequestURL(job.getId());
        HashMap<String, String>headers = null;
        
    	HashMap<String, Object> requestBodyMap = new HashMap<String, Object>();
    	requestBodyMap.put("operation", job.getOperation().toString());
        if (controller.getAppConfig().getOperationInfo().isExtraction()) {
        	headers = getHeaders(JSON_CONTENT_TYPE, CSV_CONTENT_TYPE);
        	requestBodyMap.put("query", job.getObject());        	
        } else {
        	headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);
        	requestBodyMap.put("object", job.getObject());
        	requestBodyMap.put("contentType", type.toString());
        	if (AppUtil.getOSType() == OSType.WINDOWS) {
                requestBodyMap.put("lineEnding", "CRLF");
        	} else {
        	    requestBodyMap.put("lineEnding", "LF");
        	}
        	if (operation.equals(OperationEnum.upsert)) {
        	    requestBodyMap.put("externalIdFieldName", job.getExternalIdFieldName());
        	}
        	if (operation.equals(OperationEnum.upsert)
        	   || operation.equals(OperationEnum.insert)
        	   || operation.equals(OperationEnum.update)) {
        	    if (job.getAssignmentRuleId() != null && !job.getAssignmentRuleId().isBlank()) {
        	        requestBodyMap.put("assignmentRuleId", job.getAssignmentRuleId());
        	    }
        	}
        }
        return doSendJobRequestToServer(urlString, 
										headers,
										HttpMethod.POST,
										ContentType.JSON,
										requestBodyMap,
										true,
										"Failed to create job");
    }
    
    private JobInfo doSendJobRequestToServer(String urlString, 
    		HashMap<String, String> headers,
    		HttpMethod requestMethod,
    		ContentType responseContentType,
    		HashMap<String, Object> requestBodyMap,
    		boolean processServerResponse,
    		String exceptionMessageString) throws AsyncApiException 
    {
    	if (headers == null) {
            headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);
    	}
		try {
	        InputStream in = null;
	        boolean successfulRequest = true;
            HttpClientTransport transport = HttpClientTransport.getInstance();
            transport.setConfig(getConfig());
	        if (requestMethod == HttpMethod.GET) {
	        	if (requestBodyMap != null && !requestBodyMap.isEmpty()) {
	        		Set<String> paramNameSet = requestBodyMap.keySet();
	        		boolean firstParam = true;
	        		for (Object paramName : paramNameSet) {
	        			if (firstParam) {
	        				urlString += "?" + paramName.toString() + "=" + requestBodyMap.get(paramName);
	        				firstParam = false;
	        			} else {
	        				urlString += "&" + paramName.toString() + "=" + requestBodyMap.get(paramName);
	        			}
	        		}
	        	}
	        	// make a get request
	        	try {
	        	    in = transport.httpGet(urlString);
	        	} catch (HttpClientTransportException ex) {
	                parseAndThrowException(ex);
	        	}
	        } else {
		        OutputStream out;
		        if (requestMethod == HttpMethod.PATCH) {
		        	out = transport.connect(urlString, headers, true, HttpTransportInterface.SupportedHttpMethodType.PATCH);
		        } else if (requestMethod == HttpMethod.PUT) {
		        	out = transport.connect(urlString, headers, true, HttpTransportInterface.SupportedHttpMethodType.PUT);
		        } else { // assume post method
		        	out = transport.connect(urlString, headers, true, HttpTransportInterface.SupportedHttpMethodType.POST);
		        }
	    		String requestContent = AppUtil.serializeToJson(requestBodyMap);
		        out.write(requestContent.getBytes(UTF_8));
		        out.close();
		        in = transport.getContent();
		        successfulRequest = transport.isSuccessful();
	        }
	        if (!processServerResponse) {
	        	// sent the request to server, return without processing the response
	        	return null;
	        }
	    	JobInfo result = null;
	        if (successfulRequest) {
	            if (responseContentType == ContentType.ZIP_XML || responseContentType == ContentType.XML) {
	                XmlInputStream xin = new XmlInputStream();
	                xin.setInput(in, UTF_8);
	                result = new JobInfo();
	                result.load(xin, typeMapper);
	            } else {
	                result = AppUtil.deserializeJsonToObject(in, JobInfo.class);
	            }
	        } else {
	            parseAndThrowException(in, responseContentType);
	        }
	        return result;
	    }  catch (IOException e) {
	        throw new AsyncApiException(exceptionMessageString, AsyncExceptionCode.ClientInputError, e);
	    } catch (ConnectionException e) {
	        throw new AsyncApiException(exceptionMessageString, AsyncExceptionCode.ClientInputError, e);
	    } catch (PullParserException e) {
	        throw new AsyncApiException(exceptionMessageString, AsyncExceptionCode.ClientInputError, e);
		}
	}
	
	private static void parseAndThrowException(HttpClientTransportException ex) throws AsyncApiException {
        ContentType type = null;
        String contentTypeHeader = ex.getConnection().getContentType();
        if (contentTypeHeader != null) {
            if (contentTypeHeader.contains(XML_CONTENT_TYPE)) {
                type = ContentType.XML;
            } else if (contentTypeHeader.contains(JSON_CONTENT_TYPE)) {
                type = ContentType.JSON;
            }
        }
        parseAndThrowException(ex.getInputStream(), type);
	}
	
	static void parseAndThrowException(InputStream is, ContentType type) throws AsyncApiException {
	    try {
	        AsyncApiException exception;
	        
	        BulkV2Error[] errorList = AppUtil.deserializeJsonToObject(is, BulkV2Error[].class);
	        if (errorList[0].message.contains("Aggregate Relationships not supported in Bulk Query")) {
	            exception = new AsyncApiException(errorList[0].message, AsyncExceptionCode.FeatureNotEnabled);
	        } else {
	            exception = new AsyncApiException(errorList[0].errorCode + " : " + errorList[0].message, AsyncExceptionCode.Unknown);
	        }
	        throw exception;
	    } catch (IOException | NullPointerException e) {
	        throw new AsyncApiException("Failed to parse exception", AsyncExceptionCode.ClientInputError, e);
	    }
	}
	
	private HashMap<String, String> getHeaders(String requestContentType, String acceptContentType) {
	    HashMap<String, String> newMap = new HashMap<String, String>();
        String authHeaderValue = AUTH_HEADER_VALUE_PREFIX + getConfig().getSessionId();
	    newMap.put(REQUEST_CONTENT_TYPE_HEADER, requestContentType);
	    newMap.put(ACCEPT_CONTENT_TYPES_HEADER, acceptContentType);
	    newMap.put(AUTH_HEADER, authHeaderValue);
	    newMap.put(ClientBase.SFORCE_CALL_OPTIONS_HEADER, getConfig().getRequestHeader(ClientBase.SFORCE_CALL_OPTIONS_HEADER));
	    logger.debug(ClientBase.SFORCE_CALL_OPTIONS_HEADER + " : " + getConfig().getRequestHeader(ClientBase.SFORCE_CALL_OPTIONS_HEADER));
	    for (Map.Entry<String, String> entry : headers.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
        }
	    return newMap;
	}
	
    /**********************************
     * 
     * private, extract (aka query) methods 
     * @throws ConnectionException 
     * 
     **********************************/
    private InputStream doGetQueryResultStream(URL resultsURL, HashMap<String, String> headers) throws IOException, AsyncApiException, ConnectionException {
        InputStream is = null;
        try {
            HttpClientTransport transport = HttpClientTransport.getInstance();
            transport.setConfig(getConfig());
            is = transport.httpGet(resultsURL.toString());
            HttpResponse httpResponse = transport.getHttpResponse();
            if (httpResponse != null) {
                Header header = httpResponse.getFirstHeader("Sforce-Locator");
                if (header != null) {
                    this.queryLocator = header.getValue();
                }
                header = httpResponse.getFirstHeader("Sforce-NumberOfRecords");
                if (header != null) {
                    this.numberOfRecordsInQueryResult = Integer.valueOf(header.getValue());
                }
            }
         } catch (HttpClientTransportException ex) {
            parseAndThrowException(ex);
        }
        return is;
    }
    
    /**********************************
     * 
     * private, ingest methods 
     * @throws AsyncApiException 
     * 
     **********************************/

    private InputStream doGetIngestResultsStream(String jobId, String resultsType) throws AsyncApiException {
        String resultsURLString = constructRequestURL(jobId) + resultsType;
        InputStream is = null;
        try {
            HttpClientTransport transport = HttpClientTransport.getInstance();
            transport.setConfig(getConfig());
            is = transport.httpGet(resultsURLString);
        } catch (IOException e) {
            throw new AsyncApiException("Failed to get " + resultsType + " for job id " + jobId, AsyncExceptionCode.ClientInputError, e);
        } catch (HttpClientTransportException e) {
            parseAndThrowException(e);
        }
        return is;
    }
    
    private void doSaveIngestResults(String jobId, String filename, String resultsType, boolean append) throws AsyncApiException {
    	BufferedOutputStream bos;
    	try {
    		bos = new BufferedOutputStream(new FileOutputStream(filename, append));
    	} catch (FileNotFoundException e) {
	        throw new AsyncApiException("File " + filename + " not found", AsyncExceptionCode.ClientInputError, e);
    	}
    	BufferedInputStream bis = new BufferedInputStream(doGetIngestResultsStream(jobId, resultsType));
        try {
            byte[] buffer = new byte[2048];
            boolean firstLineSkipped = !append;
	        for(int len; (len = bis.read(buffer)) > 0;) {
	            if (!firstLineSkipped) {
	                String str = new String(buffer);
	                if (str.contains("\n")) {
	                    String[] parts = str.split("\n");
	                    if (parts.length > 1) {
	                        str = "";
	                        for (int i = 1; i < parts.length; i++) {
	                            if (i > 1) {
	                                str += System.lineSeparator();
	                            }
	                            str += parts[i];
	                        }
	                        buffer = str.getBytes(controller.getAppConfig().getCsvEncoding(true));
	                        int counter = 0;
	                        while(counter < buffer.length && buffer[counter] != 0) {
	                            counter++;
	                        }
	                        len = counter;
	                        firstLineSkipped = true;
	                    }
	                }
	            }
	            if (firstLineSkipped) {
	                bos.write(buffer, 0, len);
	            }
	        }
        	bis.close();
        	bos.flush();
        	bos.close();
        } catch (IOException e) {
            throw new AsyncApiException("Failed to get " + resultsType + " for job " + jobId, AsyncExceptionCode.ClientInputError, e);
        }
    }
}

class BulkV2Error implements Serializable {
    private static final long serialVersionUID = 3L;
    public String errorCode = "";
    public String message = "";
}
