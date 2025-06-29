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

package com.salesforce.dataloader.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.salesforce.dataloader.client.transport.SimplePostFactory;
import com.salesforce.dataloader.client.transport.SimplePostInterface;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.exception.OAuthBrowserLoginRunnerException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.model.OAuthToken;
import com.salesforce.dataloader.ui.URLUtil;
//import com.salesforce.dataloader.ui.UIUtils;

public class OAuthBrowserDeviceLoginRunner {
    public enum LoginStatus { WAIT, FAIL, SUCCESS };
    protected static Logger logger = DLLogManager.getLogger(OAuthBrowserDeviceLoginRunner.class);
    private volatile LoginStatus loginResult = LoginStatus.WAIT;
    private String verificationURLStr = null;
    String userCodeStr;
    String deviceCode;
    String oAuthTokenURLStr;
    AppConfig appConfig;
    Thread checkLoginThread;

    public OAuthBrowserDeviceLoginRunner(AppConfig appConfig, boolean skipUserCodePage) throws IOException, ParameterLoadException, OAuthBrowserLoginRunnerException {
        String origEndpoint = new String(appConfig.getAuthEndpointForCurrentEnv());
        try {
            startBrowserLogin(appConfig, skipUserCodePage);
        } catch (Exception ex) {
            logger.warn(Messages.getMessage(this.getClass(), "failedAuthStart", origEndpoint, ex.getMessage()));
            if (!appConfig.isDefaultAuthEndpointForCurrentEnv(origEndpoint)) {
                // retry with default endpoint URL only if user is attempting production login
                retryBrowserLoginWithDefaultURL(appConfig, skipUserCodePage);
            }
        } finally {
            // restore original value of Config.ENDPOINT property
            appConfig.setAuthEndpointForCurrentEnv(origEndpoint);
        }
    }
    
    private void retryBrowserLoginWithDefaultURL(AppConfig appConfig, boolean skipUserCodePage)  throws IOException, ParameterLoadException, OAuthBrowserLoginRunnerException {
        logger.info(Messages.getMessage(this.getClass(), "retryAuthStart", appConfig.getDefaultAuthEndpointForCurrentEnv()));
        appConfig.setAuthEndpointForCurrentEnv(appConfig.getDefaultAuthEndpointForCurrentEnv());
        startBrowserLogin(appConfig, skipUserCodePage);
    }
    
    // Browser login uses OAuth 2.0 Device Flow - https://help.salesforce.com/s/articleView?id=sf.remoteaccess_oauth_device_flow.htm&type=5
    private void startBrowserLogin(AppConfig appConfig, boolean skipUserCodePage) throws IOException, ParameterLoadException, OAuthBrowserLoginRunnerException {
        setLoginStatus(LoginStatus.WAIT);
        this.appConfig = appConfig;
        appConfig.setServerEnvironment(appConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT));
        oAuthTokenURLStr = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/token";
        SimplePostInterface client = SimplePostFactory.getInstance(appConfig, oAuthTokenURLStr,
               new BasicNameValuePair("response_type", "device_code"),
               new BasicNameValuePair(AppConfig.CLIENT_ID_HEADER_NAME, appConfig.getEffectiveClientIdForCurrentEnv()),
               new BasicNameValuePair("scope", "api")
        );
        client.post();
        InputStream in = client.getInput();
        if (!client.isSuccessful()) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = in.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String response = result.toString(StandardCharsets.UTF_8.name());
            result.close();
            logger.error(response);
            throw new OAuthBrowserLoginRunnerException(response);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Map<?, ?> responseMap = mapper.readValue(in, Map.class);
        userCodeStr = (String) responseMap.get("user_code");
        deviceCode = (String)responseMap.get("device_code");
        logger.debug("User Code: " + userCodeStr);
        verificationURLStr = (String) responseMap.get("verification_uri")
               + "?user_code=" + userCodeStr;
        logger.debug("Verification URL: " + verificationURLStr);
        
        // start checking for login
        int pollingIntervalInSec = 5;           
        try {
           pollingIntervalInSec = ((Integer)responseMap.get("interval")).intValue();
        } catch (NumberFormatException e) {
            // fail silently
        }
        checkLoginThread = startLoginCheck(pollingIntervalInSec);

        if (!skipUserCodePage) {
            return;
        }

        // try to skip the page with pre-filled user code.
        client = SimplePostFactory.getInstance(appConfig, verificationURLStr,
                new BasicNameValuePair("", "")
        );
        client.post();
        in = client.getInput();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = in.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        
        String response = result.toString(StandardCharsets.UTF_8.name());
        if (!client.isSuccessful()) {
            // did not succeed in skipping the page with pre-filled user code, show it
            logger.error(response);
            URLUtil.openURL(verificationURLStr);
        }
        
        List<BasicNameValuePair> nvPairList = parseTokenPageHTML(response);
        
        client = SimplePostFactory.getInstance(appConfig, responseMap.get("verification_uri").toString(),
                new BasicNameValuePair("save", "Connect")
        );
        for (BasicNameValuePair pair : nvPairList) {
            client.addBasicNameValuePair(pair);
        }
        client.post();
        in = client.getInput();
        result = new ByteArrayOutputStream();
        buffer = new byte[1024];
        for (int length; (length = in.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        if (client.getStatusCode() == 302) {
            Header[] locationHeaders = client.getResponseHeaders("Location");
            String redirectURL = locationHeaders[0].getValue();
            URLUtil.openURL(redirectURL);
        } else {
            // did not succeed in skipping the page with pre-filled user code, show it
            URLUtil.openURL(verificationURLStr);
        }

    }

   private Thread startLoginCheck(final int pollingIntervalInSec) {
       Thread successfulLogincheckerThread = new Thread() {
           public void run() {
               // Poll for 20 minutes.
               // Expiry of user code is detected by server returning an error 
               // other than 'authorization_pending'.
               int maxPollingTimeInSec = 1200; 
               int elapsedTimeInSec = 0;
               SimplePostInterface client;
               InputStream in;
               try {
                   while (elapsedTimeInSec <= maxPollingTimeInSec && loginResult == LoginStatus.WAIT) {
                       try {
                           Thread.sleep(pollingIntervalInSec * 1000);
                       } catch (InterruptedException e) {
                           setLoginStatus(LoginStatus.FAIL);
                           return;
                       }
                       elapsedTimeInSec += pollingIntervalInSec;
                       // Build token request parameters for device flow
                       List<BasicNameValuePair> tokenParams = new ArrayList<>();
                       tokenParams.add(new BasicNameValuePair("grant_type", "device"));
                       tokenParams.add(new BasicNameValuePair(AppConfig.CLIENT_ID_HEADER_NAME, appConfig.getEffectiveClientIdForCurrentEnv()));
                       tokenParams.add(new BasicNameValuePair("code", deviceCode));
                       
                       // Add client secret if using External Client App (confidential client)
                       String clientSecret = appConfig.getEffectiveClientSecretForCurrentEnv();
                       if (appConfig.isExternalClientAppConfigured() && clientSecret != null && !clientSecret.trim().isEmpty()) {
                           tokenParams.add(new BasicNameValuePair("client_secret", clientSecret));
                       }
                       
                       client = SimplePostFactory.getInstance(appConfig, oAuthTokenURLStr,
                               tokenParams.toArray(new BasicNameValuePair[0])
                       );
                       try {
                           client.post();
                       } catch (ParameterLoadException | IOException e) {
                           logger.error(e.getMessage());
                           setLoginStatus(LoginStatus.FAIL);
                           return;
                       }
                       in = client.getInput();
                       if (client.isSuccessful()) {
                           try {
                               processSuccessfulLogin(client.getInput(), appConfig);
                           } catch (IOException e) {
                               logger.error(e.getMessage());
                               setLoginStatus(LoginStatus.FAIL);
                               return;
                           }
                           // got the session id => SUCCESSful login
                           setLoginStatus(LoginStatus.SUCCESS);
                           return; 
                       } else { // read the error message and log it
                           ObjectMapper mapper = new ObjectMapper();
                           mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                           try {
                               Map<?, ?> responseMap = mapper.readValue(in, Map.class);
                               String errorStr = (String)responseMap.get("error");
                               String errorDesc = (String)responseMap.get("error_description");
                               if ("authorization_pending".equalsIgnoreCase(errorStr)) {
                                   // waiting for the user to login
                                   logger.debug(errorStr + " - " + errorDesc);
                               } else {
                                   // a failure occurred. Exit.
                                   logger.error(errorStr + " - " + errorDesc);
                                   setLoginStatus(LoginStatus.FAIL);
                                   return;
                               }
                           } catch (IOException e) {
                               logger.debug(e.getMessage());
                               setLoginStatus(LoginStatus.FAIL);
                               return;
                           }
                       }
                   } // while loop
                   logger.error("User closed the dialog or timed out waiting for login");
                   setLoginStatus(LoginStatus.FAIL);
               } catch (Exception e) {
                   logger.error("Unexpected error in device flow polling thread", e);
                   setLoginStatus(LoginStatus.FAIL);
               }
           }
       };
       successfulLogincheckerThread.start();
       return successfulLogincheckerThread;
   }
   
   private synchronized void setLoginStatus(LoginStatus value) {
       this.loginResult = value;
   }
   
   public LoginStatus getLoginStatus() {
       return this.loginResult;
   }
   
   public String getUserCode() {
       return userCodeStr;
   }
   
   public String getDeviceCode() {
       return deviceCode;
   }
   
   public String getVerificationURLStr() {
       return verificationURLStr;
   }
   
   public String getOAuthTokenURL() {
       return oAuthTokenURLStr;
   }
   
   public boolean isLoginProcessCompleted() {
       return !this.checkLoginThread.isAlive();
   }
   
   private List<BasicNameValuePair> parseTokenPageHTML(String response) {
       String id, value;
       List<BasicNameValuePair> nvPairList = new ArrayList<BasicNameValuePair>();
       
       Pattern pattern = Pattern.compile("<(?!!)(?!/)\\s*([a-zA-Z0-9]+)(.*?)>");
       Matcher matcher = pattern.matcher(response);
       while (matcher.find()) {
           String tagName = matcher.group(1);
           String attributes = matcher.group(2);
           if (!tagName.equalsIgnoreCase("input")) {
               continue;
           }
           Pattern attributePattern = Pattern.compile("(\\S+)=['\"]{1}([^>]*?)['\"]{1}");
           Matcher attributeMatcher = attributePattern.matcher(attributes);
           id = "";
           value = "";
           while(attributeMatcher.find()) {
               String attributeName = attributeMatcher.group(1);
               String attributeValue = attributeMatcher.group(2);
               if (attributeName.equalsIgnoreCase("id")) {
                   id = attributeValue;
               }
               if (attributeName.equalsIgnoreCase("value")) {
                   value = attributeValue;
               }
           }
           if (!id.equals("")) {
               nvPairList.add(new BasicNameValuePair(id, value));
           }
       }

       return nvPairList;
   }
   
   
   public static void processSuccessfulLogin(InputStream httpResponseInputStream, AppConfig appConfig) throws IOException {

       StringBuilder builder = new StringBuilder();
       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponseInputStream, StandardCharsets.UTF_8.name()));
       for (int c = bufferedReader.read(); c != -1; c = bufferedReader.read()) {
           builder.append((char) c);
       }

       String jsonTokenResult = builder.toString();
       Gson gson = new GsonBuilder()
               .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
               .create();
       OAuthToken token = gson.fromJson(jsonTokenResult, OAuthToken.class);
       appConfig.setAuthEndpointForCurrentEnv(token.getInstanceUrl());
       appConfig.setValue(AppConfig.PROP_OAUTH_ACCESSTOKEN, token.getAccessToken());
       appConfig.setValue(AppConfig.PROP_OAUTH_REFRESHTOKEN, token.getRefreshToken());
       appConfig.setValue(AppConfig.PROP_OAUTH_INSTANCE_URL, token.getInstanceUrl());
   }
}
