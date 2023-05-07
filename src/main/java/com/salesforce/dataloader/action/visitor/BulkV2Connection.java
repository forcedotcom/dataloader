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

package com.salesforce.dataloader.action.visitor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dataloader.client.HttpTransportInterface;
import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.MessageHandler;
import com.sforce.ws.MessageHandlerWithHeaders;
import com.sforce.ws.bind.CalendarCodec;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;
import com.sforce.ws.util.FileUtil;

enum HttpMethod {
    GET,
    POST,
    PATCH,
    PUT
}

public class BulkV2Connection  {
    private static final String URI_STEM_QUERY = "query/";
    private static final String URI_STEM_INGEST = "ingest/";
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer ";
    public static final String NAMESPACE = "http://www.force.com/2009/06/asyncapi/dataload";
    public static final String SESSION_ID = "X-SFDC-Session";
    public static final String XML_CONTENT_TYPE = "application/xml";
    public static final String CSV_CONTENT_TYPE = "text/csv";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String ZIP_XML_CONTENT_TYPE = "zip/xml";
    public static final String ZIP_CSV_CONTENT_TYPE = "zip/csv";
    public static final String ZIP_JSON_CONTENT_TYPE = "zip/json";
    public static final String UTF_8 = StandardCharsets.UTF_8.name();
    public static final String INGEST_RESULTS_SUCCESSFUL = "successfulResults";
    public static final String INGEST_RESULTS_UNSUCCESSFUL = "failedResults";
    public static final String INGEST_RECORDS_UNPROCESSED = "unprocessedrecords";
    public static final QName JOB_QNAME = new QName(NAMESPACE, "jobInfo");

    private String authHeaderValue = "";
    private String queryLocator = "";
    private int numberOfRecordsInQueryResult = 0;
    private ConnectorConfig config;
    private HashMap<String, String> headers = new HashMap<String, String>();

    public static final TypeMapper typeMapper = new TypeMapper(null, null, false);

    /**********************************
     * 
     * public, common methods 
     * 
     **********************************/
    public BulkV2Connection(ConnectorConfig connectorConfig) throws AsyncApiException {
        this.config = connectorConfig;
        this.authHeaderValue = AUTH_HEADER_VALUE_PREFIX + getConfig().getSessionId();
    }
    
    public JobInfo createJob(JobInfo job) throws AsyncApiException {
        ContentType type = job.getContentType();
        if (type != null && type != ContentType.CSV) {
            throw new AsyncApiException("Unsupported Content Type", AsyncExceptionCode.FeatureNotEnabled);
        }
        return createJob(job, ContentType.CSV);
    }
    
    public JobInfo getJobStatus(String jobId, boolean isQuery) throws AsyncApiException {
        return getJobStatus(jobId, isQuery, ContentType.JSON);
    }
    
    public JobInfo getJobStatus(String jobId, boolean isQuery, ContentType contentType) throws AsyncApiException {
        String urlString = constructRequestURL(jobId, isQuery);
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
        String urlString = constructRequestURL(jobId, isQuery);
        HashMap<String, String> headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);
    	HashMap<Object, Object> requestBodyMap = new HashMap<Object, Object>();
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
        return getJobStatus(jobId, true);
    }        

    public InputStream getQueryResultStream(String jobId, String locator) throws AsyncApiException {
    	String urlString =  constructRequestURL(jobId, true) + "results/";
        if (locator != null && !locator.isEmpty() && !"null".equalsIgnoreCase(locator)) {
        	urlString += "?locator=" + locator;
        }
        try {
            return doGetQueryResultStream(new URL(urlString), getHeaders(JSON_CONTENT_TYPE, CSV_CONTENT_TYPE));
        } catch (IOException e) {
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
    public JobInfo startIngest(String jobId, String csvFileName) throws AsyncApiException {
    	File csvFile = new File(csvFileName);
    	if (!csvFile.exists()) {
    		throw new AsyncApiException(csvFileName + " not found.", AsyncExceptionCode.ClientInputError);
    	}
    	// Bulk V2 ingest does not accept CSV exceeding 150 MB in size
    	if (csvFile.length() > 150 * 1024 * 1024) {
    		throw new AsyncApiException(csvFileName + " size exceeds the max file size accepted by Bulk V2 (150 MB)", AsyncExceptionCode.ClientInputError);
    	}
    	
        String urlString = constructRequestURL(jobId, false) + "batches/";
        HashMap<String, String> headers = getHeaders(CSV_CONTENT_TYPE, JSON_CONTENT_TYPE);
        try {
        	HttpTransportInterface transport = (HttpTransportInterface)getConfig().createTransport();
            transport.connect(urlString, headers, true, HttpTransportInterface.SupportedHttpMethodType.PUT, new FileInputStream(csvFile), CSV_CONTENT_TYPE);

            // Following is needed to actually send the request to the server
            InputStream serverResponseStream = transport.getContent();
            if (!transport.isSuccessful()) {
	            parseAndThrowException(serverResponseStream, ContentType.JSON);
            }
        }catch (IOException e) {
            throw new AsyncApiException("Failed to send contents of " + csvFileName + " to server for job " + jobId, AsyncExceptionCode.ClientInputError, e);
        } catch (ConnectionException e) {
            throw new AsyncApiException("Failed to send contents of " + csvFileName + " to server for job " + jobId, AsyncExceptionCode.ClientInputError, e);
        }
        
        // Mark upload as completed
        urlString = constructRequestURL(jobId, false);
        headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);

    	setJobState(jobId, false, JobStateEnum.UploadComplete, "Failed to mark completion of the upload");
    	return getIngestJobStatus(jobId);
    }
    
    public JobInfo getIngestJobStatus(String jobId) throws AsyncApiException {
        return getJobStatus(jobId, false);
    }
    
    public void saveIngestSuccessResults(String jobId, String filename) throws AsyncApiException {
    	doSaveIngestResults(jobId, filename, INGEST_RESULTS_SUCCESSFUL);
    }
    
    public void saveIngestFailureResults(String jobId, String filename) throws AsyncApiException {
    	doSaveIngestResults(jobId, filename, INGEST_RESULTS_UNSUCCESSFUL);
    }
    
    public void saveIngestUnprocessedRecords(String jobId, String filename) throws AsyncApiException {
    	doSaveIngestResults(jobId, filename, INGEST_RECORDS_UNPROCESSED);
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
    private String constructRequestURL(String jobId, boolean isQuery) {
        String urlString = getConfig().getRestEndpoint();
        if (jobId == null) {
        	jobId = "";
        }
        if (isQuery) {
        	urlString += URI_STEM_QUERY + jobId + "/";
        } else {
        	urlString += URI_STEM_INGEST + jobId + "/";
        }
        return urlString;
    }
    
    private JobInfo createJob(JobInfo job, ContentType contentType) throws AsyncApiException {
        ContentType type = job.getContentType();
        if (type != null && type != ContentType.CSV) {
            throw new AsyncApiException("Unsupported Content Type", AsyncExceptionCode.FeatureNotEnabled);
        }
        OperationEnum operation = job.getOperation();
        String urlString = constructRequestURL(job.getId(), operation.equals(OperationEnum.query));
        HashMap<String, String>headers = null;
        
    	HashMap<Object, Object> requestBodyMap = new HashMap<Object, Object>();
    	requestBodyMap.put("operation", job.getOperation().toString());
        if (operation.equals(OperationEnum.query)) {
        	headers = getHeaders(JSON_CONTENT_TYPE, CSV_CONTENT_TYPE);
        	requestBodyMap.put("query", job.getObject());        	
        } else {
        	headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);
        	requestBodyMap.put("object", job.getObject());
        	requestBodyMap.put("contentType", "CSV");
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
    		HashMap<Object, Object> requestBodyMap,
    		boolean processServerResponse,
    		String exceptionMessageString) throws AsyncApiException 
    {
    	if (headers == null) {
            headers = getHeaders(JSON_CONTENT_TYPE, JSON_CONTENT_TYPE);
    	}
		try {
	        InputStream in;
	        boolean successfulRequest = true;
	        if (requestMethod == HttpMethod.GET) {
	        	if (requestBodyMap != null && !requestBodyMap.isEmpty()) {
	        		Set<Object> paramNameSet = requestBodyMap.keySet();
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
	            HttpURLConnection httpConnection = openHttpConnection(new URL(urlString), headers);
	            in = doHttpGet(httpConnection, new URL(urlString));
	        } else {
	        	HttpTransportInterface transport = (HttpTransportInterface) getConfig().createTransport();
		        OutputStream out;
		        if (requestMethod == HttpMethod.PATCH) {
		        	out = transport.connect(urlString, headers, true, HttpTransportInterface.SupportedHttpMethodType.PATCH);
		        } else if (requestMethod == HttpMethod.PUT) {
		        	out = transport.connect(urlString, headers, true, HttpTransportInterface.SupportedHttpMethodType.PUT);
		        } else { // assume post method
		        	out = transport.connect(urlString, headers, true, HttpTransportInterface.SupportedHttpMethodType.POST);
		        }
	    		String requestContent = serializeToJson(requestBodyMap);
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
	                result = deserializeJsonToObject(in, JobInfo.class);
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
    
	private ConnectorConfig getConfig() {
	    return config;
	}
	
	static void parseAndThrowException(InputStream in, ContentType type) throws AsyncApiException {
	    try {
	        AsyncApiException exception;
	        BulkV2Error[] errorList = deserializeJsonToObject(in, BulkV2Error[].class);
	        if (errorList[0].message.contains("Aggregate Relationships not supported in Bulk Query")) {
	            exception = new AsyncApiException(errorList[0].message, AsyncExceptionCode.FeatureNotEnabled);
	        } else {
	            exception = new AsyncApiException(errorList[0].errorCode + " : " + errorList[0].message, AsyncExceptionCode.Unknown);
	        }
	        throw exception;
	    } catch (IOException e) {
	        throw new AsyncApiException("Failed to parse exception", AsyncExceptionCode.ClientInputError, e);
	    }
	}
	
	private HashMap<String, String> getHeaders(String requestContentType, String acceptContentType) {
	    HashMap<String, String> newMap = new HashMap<String, String>();
	    newMap.put("Content-Type", requestContentType);
	    newMap.put("ACCEPT", acceptContentType);
	    newMap.put(AUTH_HEADER, this.authHeaderValue);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
        }
	    return newMap;
	}
	
	static String serializeToJson(HashMap<Object, Object> nameValueMap) throws JsonProcessingException {
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.setDateFormat(CalendarCodec.getDateFormat());
	    return mapper.writeValueAsString(nameValueMap);
	}
	
	static <T> T deserializeJsonToObject (InputStream in, Class<T> tmpClass) throws IOException {
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	    // By default, ObjectMapper generates Calendar instances with UTC TimeZone.
	    // Here, override that to "GMT" to better match the behavior of the WSC XML parser.
	    mapper.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return mapper.readValue(in, tmpClass);
	}
	
    private HttpURLConnection openHttpConnection(URL url, HashMap<String, String> headers) throws IOException {
        HttpURLConnection connection = getConfig().createConnection(url, null);
        SSLContext sslContext = getConfig().getSslContext();
        if (sslContext != null && connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection)connection).setSSLSocketFactory(sslContext.getSocketFactory());
        }
        if (headers != null && !headers.isEmpty()) {
        	Set<String> headerNameSet = headers.keySet();
        	for (String headerName : headerNameSet) {
        		connection.setRequestProperty(headerName, headers.get(headerName));
        	}
        }
        connection.setRequestProperty(AUTH_HEADER, this.authHeaderValue);
        return connection;
    }
        
    private InputStream doHttpGet(HttpURLConnection connection, URL url) throws IOException, AsyncApiException {
        boolean success = true;
        InputStream in;
        try {
            in = connection.getInputStream();
        } catch (IOException e) {
            success = false;
            in = connection.getErrorStream();
        }

        String encoding = connection.getHeaderField("Content-Encoding");
        if ("gzip".equals(encoding)) {
            in = new GZIPInputStream(in);
        }

        if (getConfig().isTraceMessage() || getConfig().hasMessageHandlers()) {
            byte[] bytes = FileUtil.toBytes(in);
            in = new ByteArrayInputStream(bytes);

            if (getConfig().hasMessageHandlers()) {
                Iterator<MessageHandler> it = getConfig().getMessagerHandlers();
                while (it.hasNext()) {
                    MessageHandler handler = it.next();
                    if (handler instanceof MessageHandlerWithHeaders) {
                        ((MessageHandlerWithHeaders)handler).handleRequest(url, new byte[0], null);
                        ((MessageHandlerWithHeaders)handler).handleResponse(url, bytes, connection.getHeaderFields());
                    } else {
                        handler.handleRequest(url, new byte[0]);
                        handler.handleResponse(url, bytes);
                    }
                }
            }

            if (getConfig().isTraceMessage()) {
                getConfig().getTraceStream().println(url.toExternalForm());

                Map<String, List<String>> headers = connection.getHeaderFields();
                for (Map.Entry<String, List<String>>entry : headers.entrySet()) {
                    StringBuffer sb = new StringBuffer();
                    List<String> values = entry.getValue();

                    if (values != null) {
                        for (String v : values) {
                            sb.append(v);
                        }
                    }

                    getConfig().getTraceStream().println(entry.getKey() + ": " + sb.toString());
                }

                getConfig().teeInputStream(bytes);
            }
        }

        if (!success) {
            ContentType type = null;
            String contentTypeHeader = connection.getContentType();
            if (contentTypeHeader != null) {
                if (contentTypeHeader.contains(XML_CONTENT_TYPE)) {
                    type = ContentType.XML;
                } else if (contentTypeHeader.contains(JSON_CONTENT_TYPE)) {
                    type = ContentType.JSON;
                }
            }
            parseAndThrowException(in, type);
        }
        return in;
    }
    
    /**********************************
     * 
     * private, extract (aka query) methods 
     * 
     **********************************/
    private InputStream doGetQueryResultStream(URL resultsURL, HashMap<String, String> headers) throws IOException, AsyncApiException {
        HttpURLConnection httpConnection = openHttpConnection(resultsURL, headers);
        InputStream is = doHttpGet(httpConnection, resultsURL);
        this.queryLocator = httpConnection.getHeaderField("Sforce-Locator");
        this.numberOfRecordsInQueryResult = Integer.valueOf(httpConnection.getHeaderField("Sforce-NumberOfRecords"));
        return is;
    }
    
    /**********************************
     * 
     * private, ingest methods 
     * @throws AsyncApiException 
     * 
     **********************************/

    private InputStream doGetIngestResultsStream(String jobId, String resultsType) throws AsyncApiException {
        String resultsURLString = constructRequestURL(jobId, false) + resultsType;
        try {
        	URL resultsURL = new URL(resultsURLString);
            HttpURLConnection httpConnection = openHttpConnection(resultsURL, getHeaders(JSON_CONTENT_TYPE, CSV_CONTENT_TYPE));
            return doHttpGet(httpConnection, resultsURL);
        } catch (IOException e) {
            throw new AsyncApiException("Failed to get " + resultsType + " for job id " + jobId, AsyncExceptionCode.ClientInputError, e);
        }
    }
    
    private void doSaveIngestResults(String jobId, String filename, String resultsType) throws AsyncApiException {
    	BufferedOutputStream bos;
    	try {
    		bos = new BufferedOutputStream(new FileOutputStream(filename));
    	} catch (FileNotFoundException e) {
	        throw new AsyncApiException("File " + filename + " not found", AsyncExceptionCode.ClientInputError, e);
    	}
    	BufferedInputStream bis = new BufferedInputStream(doGetIngestResultsStream(jobId, resultsType));
        try {
            byte[] buffer = new byte[2048];
	        for(int len; (len = bis.read(buffer)) > 0;) {
	            bos.write(buffer, 0, len);
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
