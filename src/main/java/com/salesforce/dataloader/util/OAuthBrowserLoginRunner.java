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

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dataloader.client.SimplePost;
import com.salesforce.dataloader.client.SimplePostFactory;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.OAuthBrowserLoginRunnerException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.ui.OAuthFlow;

public class OAuthBrowserLoginRunner {
    public enum LoginStatus { WAIT, FAIL, SUCCESS };
    protected static Logger logger = LogManager.getLogger(OAuthBrowserLoginRunner.class);
    private static LoginStatus loginResult = LoginStatus.WAIT;
    private String verificationURLStr = null;
    final String userCodeStr;
    final String deviceCode;
    final String oAuthTokenURLStr;
    final Config config;
    final Thread checkLoginThread;

    public OAuthBrowserLoginRunner(Config config, boolean skipUserCodePage) throws IOException, ParameterLoadException, OAuthBrowserLoginRunnerException {
        setLoginStatus(LoginStatus.WAIT);
        this.config = config;
        config.setOAuthEnvironment(config.getString(Config.OAUTH_ENVIRONMENT));
        oAuthTokenURLStr = config.getString(Config.OAUTH_SERVER) + "/services/oauth2/token";
        SimplePost client = SimplePostFactory.getInstance(config, oAuthTokenURLStr,
               new BasicNameValuePair("response_type", "device_code"),
               new BasicNameValuePair("client_id", config.getString(Config.OAUTH_CLIENTID)),
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
            String response = result.toString("UTF-8");
            logger.error(response);
            throw new OAuthBrowserLoginRunnerException(response);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Map responseMap = mapper.readValue(in, Map.class);
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
        client = SimplePostFactory.getInstance(config, verificationURLStr,
                new BasicNameValuePair("", "")
        );
        client.post();
        in = client.getInput();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = in.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        
        String response = result.toString("UTF-8");
        if (!client.isSuccessful()) {
            // did not succeed in skipping the page with pre-filled user code, show it
            logger.error(response);
            openURL(verificationURLStr);
        }
        
        List<BasicNameValuePair> nvPairList = parseTokenPageHTML(response);
        
        client = SimplePostFactory.getInstance(config, responseMap.get("verification_uri").toString(),
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
            openURL(redirectURL);
        } else {
            // did not succeed in skipping the page with pre-filled user code, show it
            openURL(verificationURLStr);
        }
    }
       
    public void openURL(String url) {
       if (Desktop.isDesktopSupported()) {
           Desktop desktop = Desktop.getDesktop();
           try {
               logger.debug("trying to use desktop.browse() method");
               desktop.browse(new URI(url));
           } catch (IOException | URISyntaxException e) {
               logger.debug(e.getMessage());
               openURLUsingNativeCommand(url);
           }
       } else {
           logger.debug("trying to use native command");
           openURLUsingNativeCommand(url);
       }
   }
   
   private void openURLUsingNativeCommand(String url) {
       Runtime runtime = Runtime.getRuntime();
       String osName = System.getProperty("os.name");
       try {
           if (osName.toLowerCase().indexOf("mac") >= 0) {
               logger.debug("trying to use open command on mac");
               runtime.exec("open " + url);
           }
           else if (osName.toLowerCase().indexOf("win") >= 0) {
               logger.debug("trying to use rundll32 command on windows");
               runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
           } else { //assume Unix or Linux
               try {
                   logger.debug("trying to use xdg-open command on linux");
                   runtime.exec("xdg-open " + url);
               } catch (IOException e) {
                   logger.debug(e.getMessage());
                   logger.debug("trying to browser-specific command on linux");
                   String[] browsers = {
                           "firefox", "chrome", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                   String browser = null;
                   for (int count = 0; count < browsers.length && browser == null; count++)
                       if (runtime.exec(
                               new String[] {"which", browsers[count]}).waitFor() == 0) {
                           browser = browsers[count];
                       }
                   if (browser == null) {
                       throw new Exception("Could not find web browser");
                   } else {
                       runtime.exec(new String[] {browser, url});
                   }
               }
           }
       }
       catch (Exception e) {
           logger.error(e.getMessage());
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
               SimplePost client;
               InputStream in;
               while (elapsedTimeInSec <= maxPollingTimeInSec) {
                   try {
                       Thread.sleep(pollingIntervalInSec * 1000);
                   } catch (InterruptedException e) {
                       // do nothing
                   }
                   elapsedTimeInSec += pollingIntervalInSec;
                   client = SimplePostFactory.getInstance(config, oAuthTokenURLStr,
                           new BasicNameValuePair("grant_type", "device"),
                           new BasicNameValuePair("client_id", config.getString(Config.OAUTH_CLIENTID)),
                           new BasicNameValuePair("code", deviceCode)
                   );
                   try {
                       client.post();
                   } catch (ParameterLoadException e) {
                       logger.error(e.getMessage());;
                       setLoginStatus(LoginStatus.FAIL);
                       return;
                   } catch (IOException e) {
                       logger.error(e.getMessage());
                       setLoginStatus(LoginStatus.FAIL);
                       return;
                   }
                   in = client.getInput();
                   if (client.isSuccessful()) {
                       try {
                           OAuthFlow.processSuccessfulLogin(client.getInput(), config);
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
                           Map responseMap = mapper.readValue(in, Map.class);
                           String errorStr = (String)responseMap.get("error");
                           String errorDesc = (String)responseMap.get("error_description");
                           if ("authorization_pending".equalsIgnoreCase(errorStr)) {
                               // waiting for the user to login
                               logger.debug(errorStr + " - " + errorDesc);
                           } else {
                               // a failure occurred. Exit.
                               logger.error(errorStr + " - " + errorDesc);
                               setLoginStatus(LoginStatus.FAIL);
                               break;
                           }
                       } catch (IOException e) {
                           logger.debug(e.getMessage());
                           continue;
                       }
                   }
               } // while loop
               logger.error("User closed the dialog or timed out waiting for login");
               setLoginStatus(LoginStatus.FAIL);
           }
       };
       successfulLogincheckerThread.start();
       return successfulLogincheckerThread;
   }
   
   private synchronized void setLoginStatus(LoginStatus value) {
       loginResult = value;
   }
   
   public LoginStatus getLoginStatus() {
       return loginResult;
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
}
