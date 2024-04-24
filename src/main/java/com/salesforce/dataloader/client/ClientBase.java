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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.salesforce.dataloader.client.SessionInfo.NotLoggedInException;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.ws.ConnectorConfig;

/**
 * Base class for API client wrapper classes
 * 
 * @author Colin Jarvis
 * @since 17.0
 */
public abstract class ClientBase<ConnectionType> {

    private static Logger LOG = LogManager.getLogger(PartnerClient.class);

    protected static final URL DEFAULT_AUTH_ENDPOINT_URL;
    static {
        URL loginUrl;
        try {
            loginUrl = new URL(Connector.END_POINT);
        } catch (MalformedURLException ex) {
            LOG.error(ex);
            throw new RuntimeException(ex);
        }
        DEFAULT_AUTH_ENDPOINT_URL = loginUrl;
    }

    private static String apiVersionForTheSession = getCurrentAPIVersionInWSC();

    protected final Logger logger;
    protected final Controller controller;
    protected final Config config;

    private SessionInfo session = new SessionInfo();

    private final boolean enableRetries;
    private final int maxRetries;

    protected abstract boolean connectPostLogin(ConnectorConfig connectorConfig);

    public abstract ConnectionType getConnection();

    protected ClientBase(Controller controller, Logger logger) {
        this.controller = controller;
        this.config = controller.getConfig();
        this.logger = logger;
        String apiVersionStr = config.getString(Config.API_VERSION_PROP);
        if (apiVersionStr != null && !apiVersionStr.isEmpty()) {
            apiVersionForTheSession = apiVersionStr;
        }
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
    
    protected int getMaxRetries() {
        return this.maxRetries;
    }
    
    protected boolean isRetriesEnabled() {
        return this.enableRetries;
    }

    public final boolean connect(SessionInfo sess) {
        setSession(sess);
        return connectPostLogin(getConnectorConfig());
    }

    private static final String BASE_CLIENT_NAME = "DataLoader";
    private static final String BULK_API_CLIENT_TYPE = "Bulk";
    private static final String PARTNER_API_CLIENT_TYPE = "Partner";
    private static final String BATCH_CLIENT_STRING = "Batch";
    private static final String UI_CLIENT_STRING = "UI";

    public static String getClientName(Config cfg) {
        String apiType = cfg.isBulkAPIEnabled() ? BULK_API_CLIENT_TYPE : PARTNER_API_CLIENT_TYPE;
        final String interfaceType = cfg.isBatchMode() ? BATCH_CLIENT_STRING : UI_CLIENT_STRING;
        if (cfg.isBulkAPIEnabled() && cfg.isBulkV2APIEnabled()) {
            apiType = apiType + "v2";
        }
        return new StringBuilder(32).append(BASE_CLIENT_NAME).append(apiType).append(interfaceType)
                .append("/")
                .append(Controller.APP_VERSION)
                .toString(); //$NON-NLS-1$
    }
    
    public static synchronized String getAPIVersionForTheSession() {
        return apiVersionForTheSession;
    }
    
    public static synchronized void setAPIVersionForTheSession(String version) {
        apiVersionForTheSession = version;
    }

    public ConnectorConfig getConnectorConfig() {
        ConnectorConfig cc = new ConnectorConfig();
        cc.setTransport(HttpClientTransport.class);
        cc.setSessionId(getSessionId());
        cc.setRequestHeader("Sforce-Call-Options",
                "client=" + ClientBase.getClientName(this.config));      
        // set authentication credentials
        // blank username is not acceptible
        String username = config.getString(Config.USERNAME);
        boolean isManualSession = config.getBoolean(Config.SFDC_INTERNAL) && config.getBoolean(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN);
        boolean isOAuthSession = config.getString(Config.OAUTH_ACCESSTOKEN) != null && config.getString(Config.OAUTH_ACCESSTOKEN).trim().length() > 0;
        if (!isManualSession && !isOAuthSession && (username == null || username.length() == 0)) {
            String errMsg = Messages.getMessage(getClass(), "emptyUsername", Config.USERNAME);
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        }

        cc.setUsername(username);
        cc.setPassword(config.getString(Config.PASSWORD));

        // proxy properties
        try {
            String proxyHost = config.getString(Config.PROXY_HOST);
            int proxyPort = config.getInt(Config.PROXY_PORT);
            if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
                logger.info(Messages.getFormattedString(
                        "Client.sforceLoginProxyDetail", new String[] { proxyHost, String.valueOf(proxyPort) })); //$NON-NLS-1$
                cc.setProxy(proxyHost, proxyPort);

                String proxyUsername = config.getString(Config.PROXY_USERNAME);
                if (proxyUsername != null && proxyUsername.length() > 0) {
                    logger.info(Messages.getFormattedString("Client.sforceLoginProxyUser", proxyUsername)); //$NON-NLS-1$
                    cc.setProxyUsername(proxyUsername);

                    String proxyPassword = config.getString(Config.PROXY_PASSWORD);
                    if (proxyPassword != null && proxyPassword.length() > 0) {
                        logger.info(Messages.getString("Client.sforceLoginProxyPassword")); //$NON-NLS-1$
                        cc.setProxyPassword(proxyPassword);
                    } else {
                        cc.setProxyPassword("");
                    }
                }

                String proxyNtlmDomain = config.getString(Config.PROXY_NTLM_DOMAIN);
                if (proxyNtlmDomain != null && proxyNtlmDomain.length() > 0) {
                    logger.info(Messages.getFormattedString("Client.sforceLoginProxyNtlm", proxyNtlmDomain)); //$NON-NLS-1$
                    cc.setNtlmDomain(proxyNtlmDomain);
                }
            }

        } catch (ParameterLoadException e) {
            logger.error(e.getMessage());
        }

        // Time out after 5 seconds for connection
        int connTimeoutSecs;
        try {
            connTimeoutSecs = config.getInt(Config.CONNECTION_TIMEOUT_SECS);
        } catch (ParameterLoadException e1) {
            connTimeoutSecs = Config.DEFAULT_CONNECTION_TIMEOUT_SECS;
        }
        cc.setConnectionTimeout(connTimeoutSecs * 1000);

        // Time out after 1 minute 10 sec for login response
        // set timeout for operations based on config
        int timeoutSecs;
        try {
            timeoutSecs = config.getInt(Config.TIMEOUT_SECS);
        } catch (ParameterLoadException e) {
            timeoutSecs = Config.DEFAULT_TIMEOUT_SECS;
        }
        cc.setReadTimeout((timeoutSecs * 1000));

        // use compression or turn it off
        if (config.contains(Config.NO_COMPRESSION)) {
            cc.setCompression(!config.getBoolean(Config.NO_COMPRESSION));
        }

        if (config.getBoolean(Config.DEBUG_MESSAGES)) {
            cc.setTraceMessage(true);
            cc.setPrettyPrintXml(true);
            String filename = config.getString(Config.DEBUG_MESSAGES_FILE);
            if (filename.length() > 0) {
                try {
                    cc.setTraceFile(filename);
                } catch (FileNotFoundException e) {
                    logger.warn(Messages.getFormattedString("Client.errorMsgDebugFilename", filename));
                }
            }
        }
        String server = getSession().getServer();
        if (server != null) {
            cc.setAuthEndpoint(server + PartnerClient.getServicePath());
            cc.setServiceEndpoint(server + PartnerClient.getServicePath()); // Partner SOAP service
            cc.setRestEndpoint(server + BulkV1Client.getServicePath());  // REST service: Bulk v1
        }
        cc.setTraceMessage(config.getBoolean(Config.WIRE_OUTPUT));

        return cc;
    }
    
    public static String getCurrentAPIVersionInWSC() {
        String[] connectURLArray = Connector.END_POINT.split("\\/");
        return connectURLArray[connectURLArray.length-1];
    }
    
    public static String getPreviousAPIVersionInWSC() {
        String currentAPIVersion = getCurrentAPIVersionInWSC();
        String[] versionStrArray = currentAPIVersion.split("\\.");
        String currentMajorVerStr = versionStrArray[0];
        int currentMajorVer = Integer.parseInt(currentMajorVerStr);
        return Integer.toString(currentMajorVer-1) + ".0";
    }

    public SessionInfo getSession() {
        return this.session;
    }

    protected void clearSession() {
        setSession(new SessionInfo());
    }

    protected void setSession(String sessionId, String server, GetUserInfoResult userInfo) {

        setSession(new SessionInfo(sessionId, server, userInfo));
    }

    private void setSession(SessionInfo sess) {
        this.session = sess;
    }

    protected String getSessionId() throws NotLoggedInException {
        return getSession().getSessionId();
    }

}
