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
package com.salesforce.dataloader.action.visitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dataloader.client.HttpTransportInterface;
import com.salesforce.dataloader.client.HttpClientTransport;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectorConfig;


public class TestREST {
    private static String insertFilename = "./insertAccountCsv.csv";
    private static String deleteFilename = "./deleteAccountCsv.csv";
    private static String successFilename = "./ingestSuccessResults.csv";
    private static String failureFilename = "./ingestFailureResults.csv";
    private static String unprocessedFilename = "./ingestUnprocessedRecords.csv";
    private static String bulkQueryResultsFilename = "./queryResults.csv";
    private static String username = "dltest@dl.com";
    private static String password = "dataloader6";
    private static String myDomainURLString = "https://ashit-dev-ed.my.salesforce.com";
    private static String restEndpoint = myDomainURLString + "/services/data/v59.0/";

    public static void main(String[] args) {

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
                cc.setSessionId(conn.getSessionHeader().getSessionId());
                updateAccounts(cc, conn);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
    }
    
    public static void updateAccounts(ConnectorConfig cc, PartnerConnection conn) {
        System.out.println("\n_______________ Lead UPDATE _______________");
 
        //Notice, the id for the record to update is part of the URI, not part of the JSON
        // String uri = restEndpoint + "sobjects/Account/0014W00003QSWuSQAX";
        String uri = restEndpoint + "composite/sobjects/";
            //Create the JSON object containing the updated account last name
            //and the id of the account we are updating.
            HashMap<String, String> sobjectTypeMap = new HashMap<String, String>();
            sobjectTypeMap.put("type", "Account");
            HashMap<String, Object> record1 = new HashMap<String, Object>();
            record1.put("external_id__c", "firstexid");
            record1.put("id", "0014W00003QSWuSQAX");
            record1.put("attributes", sobjectTypeMap);
            HashMap<String, Object> record2 = new HashMap<String, Object>();
            record2.put("external_id__c", "extid2");
            record2.put("id", "0014W00003QSWuTQAX");
            record2.put("attributes", sobjectTypeMap);
            
            ArrayList<Object> recordList = new ArrayList<Object>();
            recordList.add(record1);
            recordList.add(record2);

            HashMap<String, Object> recordsMap = new HashMap<String, Object>();
            recordsMap.put("records", recordList);
            recordsMap.put("allOrNone", false);
            ObjectMapper mapper = new ObjectMapper();
            String json = "";
            try {
                json = mapper.writeValueAsString(recordsMap);
                System.out.println("JSON for update of account record:\n" + json);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/JSON");
            headers.put("ACCEPT", "application/JSON");
            headers.put("Authorization", "Bearer " + cc.getSessionId());
            HttpClientTransport transport = new HttpClientTransport(cc);
            try {
                OutputStream out = transport.connect(uri, headers, true, HttpTransportInterface.SupportedHttpMethodType.PATCH);
                out.write(json.getBytes(StandardCharsets.UTF_8.name()));
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(-1);
            }
            InputStream in = null;
            try {
                in = transport.getContent();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(-1);
            }
            boolean successfulRequest = transport.isSuccessful();
            if (successfulRequest) {
                try {
                    String result = IOUtils.toString(in, StandardCharsets.UTF_8);
                    System.out.println(result);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                try {
                    String result = IOUtils.toString(in, StandardCharsets.UTF_8);
                    System.out.println(result);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }



            //Set up the objects necessary to make the request.
            //DefaultHttpClient httpClient = new DefaultHttpClient();
/*
            HttpClient httpClient = HttpClientBuilder.create().build();
 
            HttpPatch httpPatch = new HttpPatch(uri);
            httpPatch.addHeader(oauthHeader);
            httpPatch.addHeader(prettyPrintHeader);
            StringEntity body = new StringEntity(account.toString(1));
            body.setContentType("application/json");
            httpPatch.setEntity(body);
 
            //Make the request
            HttpResponse response = httpClient.execute(httpPatch);
 
            //Process the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                System.out.println("Updated the lead successfully.");
            } else {
                System.out.println("Lead update NOT successfully. Status code is " + statusCode);
            }
            */
    }
}
class Account implements Serializable {
    private static final long serialVersionUID = -6580088324176619490L;
    String external_id__c = "";
    public Account() {
        
    }
}