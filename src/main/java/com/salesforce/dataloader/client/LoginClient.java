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
import com.salesforce.dataloader.exception.PasswordExpiredException;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.LimitInfo;
import com.sforce.soap.partner.LimitInfoHeader_element;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

import org.apache.logging.log4j.Logger;
import com.salesforce.dataloader.util.DLLogManager;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class LoginClient extends ClientBase<PartnerConnection> {

    private static Logger LOG = DLLogManager.getLogger(LoginClient.class);

    private ConnectorConfig connectorConfig = null;

    private static interface ClientOperation<RESULT, ARG> {
        String getName();

        RESULT run(ARG arg) throws ConnectionException;
    }

    private final ClientOperation<LoginResult, PartnerConnection> LOGIN_OPERATION = new ClientOperation<LoginResult, PartnerConnection>() {
        @Override
        public String getName() {
            return "login";
        }

        @Override
        public LoginResult run(PartnerConnection client) throws ConnectionException {
            ConnectorConfig cc = client.getConfig();
            return client.login(cc.getUsername(), cc.getPassword());
        }
    };

    private LoginClient(Controller controller) {
        super(controller, LOG);
    }

    public boolean connect() throws ConnectionException {
        return login();
    }
    
    @Override
    protected boolean connectPostLogin(ConnectorConfig cc) {
        if (getConnection() == null)
            throw new IllegalStateException("Client should be logged in already");

        getConnection().setCallOptions(ClientBase.getClientName(this.appConfig), null);

        return true;
    }
    
    protected <R, A> R runOperation(ClientOperation<R, A> op, A arg) throws ConnectionException {
        logger.debug(Messages.getFormattedString("Client.beginOperation", op.getName())); //$NON-NLS-1$
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

    boolean isSessionValid() {
        if (appConfig.getBoolean(AppConfig.PROP_SFDC_INTERNAL) && appConfig.getBoolean(AppConfig.PROP_SFDC_INTERNAL_IS_SESSION_ID_LOGIN)) {
            return true;
        }
        if (appConfig.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN) != null && appConfig.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN).trim().length() > 0) {
            return true;
        }
        return isLoggedIn();
    }

    private boolean login() throws ConnectionException, ApiFault {
        disconnect();
        String origEndpoint = new String(appConfig.getAuthEndpointForCurrentEnv());
        try {
            dologin();
            logger.debug("able to successfully invoke server APIs of version " + getAPIVersionForTheSession());
        } catch (UnexpectedErrorFault fault) {
            // attempt login with previous API version
            if (fault.getExceptionCode() == ExceptionCode.UNSUPPORTED_API_VERSION
                    && getAPIVersionForTheSession() != null 
                    && !getAPIVersionForTheSession().equals(getPreviousAPIVersionInWSC())) {
                logger.error("Failed to successfully invoke server APIs of version " + getAPIVersionForTheSession());
                setAPIVersionForTheSession(getPreviousAPIVersionInWSC());
                login(); 
            } else {
                logger.error("Failed to get user info using manually configured session id", fault);
                throw fault;
            }
        } catch (ConnectionException e) {
            String exceptionMessage = e.getMessage();
            if (e instanceof LoginFault) {
                LoginFault lf = (LoginFault)e;
                exceptionMessage = lf.getExceptionMessage();
            }
            logger.warn(Messages.getMessage(this.getClass(), "failedUsernamePasswordAuth", 
                                            origEndpoint, appConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT), exceptionMessage));
            // attempt login with the default endpoint for the selected environment
            if (!appConfig.isDefaultAuthEndpointForCurrentEnv(origEndpoint)) {
                // retry with default endpoint URL only if user is attempting production login
                appConfig.setAuthEndpointForCurrentEnv(appConfig.getDefaultAuthEndpointForCurrentEnv());
                logger.info(Messages.getMessage(this.getClass(), "retryUsernamePasswordAuth", appConfig.getDefaultAuthEndpointForCurrentEnv(), appConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT)));
                login();
            } else {
                logger.error("Failed to get user info using manually configured session id", e);
                throw e;   
            }
        } finally {
            // restore original value of Config.ENDPOINT property
            appConfig.setAuthEndpointForCurrentEnv(origEndpoint);
        }
        return true; // exception thrown if there is an issue with login
    }
    
    private boolean dologin() throws ConnectionException, ApiFault {
        // Attempt the login giving the user feedback
        logger.info(Messages.getString("Client.sforceLogin")); //$NON-NLS-1$
        String oauthAccessToken = appConfig.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN);
        String serverUrl = appConfig.getAuthEndpointForCurrentEnv();
        if (oauthAccessToken != null && oauthAccessToken.trim().length() > 0) {
            serverUrl = appConfig.getString(AppConfig.PROP_OAUTH_INSTANCE_URL);
        }
        final ConnectorConfig cc = getLoginConnectorConfig(serverUrl);
        boolean savedIsTraceMessage = cc.isTraceMessage();
        cc.setTraceMessage(false);
        // NOTE
        // Create PartnerConnection instance only once to avoid issues with API restricted editions
        // such as Professional Edition.
        final PartnerConnection conn = Connector.newConnection(cc);
        // identify the client as dataloader
        conn.setCallOptions(ClientBase.getClientName(this.appConfig), null);

        try {
            if (oauthAccessToken != null && oauthAccessToken.trim().length() > 0) {
                setConfiguredSessionId(conn, oauthAccessToken, null);
            } else if (appConfig.getBoolean(AppConfig.PROP_SFDC_INTERNAL) && appConfig.getBoolean(AppConfig.PROP_SFDC_INTERNAL_IS_SESSION_ID_LOGIN)) {
                setConfiguredSessionId(conn, appConfig.getString(AppConfig.PROP_SFDC_INTERNAL_SESSION_ID), null);
            } else {
                setSessionRenewer(conn);
                loginInternal(conn);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            cc.setTraceMessage(savedIsTraceMessage);
        }
        return true;

    }

    private void setSessionRenewer(final PartnerConnection conn) {
        conn.getConfig().setSessionRenewer(new SessionRenewer() {
            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                loginInternal(conn);
                return null;
            }
        });
    }

    private void setConfiguredSessionId(PartnerConnection conn, String sessionId, GetUserInfoResult userInfo) throws ConnectionException {
        logger.info("Using manually configured session id to bypass login");
        conn.setSessionHeader(sessionId);
        if (userInfo == null) {
            userInfo = conn.getUserInfo(); // check to make sure we have a good connection
        }
        loginSuccess(conn, getAuthenticationHostDomainUrl(appConfig.getAuthEndpointForCurrentEnv()), userInfo);
    }

    private void loginInternal(final PartnerConnection conn) throws ConnectionException, PasswordExpiredException {
        final ConnectorConfig cc = conn.getConfig();
        cc.setRequestHeader(AppConfig.CLIENT_ID_HEADER_NAME, appConfig.getClientIDForCurrentEnv());
        try {
            logger.info(Messages.getMessage(getClass(), "sforceLoginDetail", cc.getAuthEndpoint(), cc.getUsername()));
            LoginResult loginResult = runOperation(LOGIN_OPERATION, conn);
            // if password has expired, throw an exception
            if (loginResult.getPasswordExpired()) {
                throw new PasswordExpiredException(Messages
                        .getString("Client.errorExpiredPassword")); //$NON-NLS-1$
            }
            // update session id and service endpoint based on response
            conn.setSessionHeader(loginResult.getSessionId());
            String serverUrl = loginResult.getServerUrl();
            String server = getAuthenticationHostDomainUrl(serverUrl);
            if (appConfig.getBoolean(AppConfig.PROP_RESET_URL_ON_LOGIN)) {
                cc.setServiceEndpoint(serverUrl);
            }
            loginSuccess(conn, server, loginResult.getUserInfo());
        } catch (ConnectionException ex) {
            String exceptionMessage = ex.getMessage();
            if (ex instanceof LoginFault) {
                LoginFault lf = (LoginFault)ex;
                exceptionMessage = lf.getExceptionMessage();
            }
            logger.error(Messages.getMessage(getClass(), "loginError", cc.getAuthEndpoint(), exceptionMessage), ex);
            throw ex;
        }
    }

    private void loginSuccess(PartnerConnection conn, String serv, GetUserInfoResult userInfo) {
    	// share the connection with PartnerClient instance
        controller.getPartnerClient().setConnection(conn);
        this.setConnection(conn);
        setSession(conn.getSessionHeader().getSessionId(), serv, userInfo);
    }

    private String getServerStringFromUrl(URL url) {
        return url.getProtocol() + "://" + url.getAuthority();
    }

    private String getAuthenticationHostDomainUrl(String serverUrl) {
        if (appConfig.getBoolean(AppConfig.PROP_RESET_URL_ON_LOGIN)) {
            try {
                return getServerStringFromUrl(URI.create(serverUrl).toURL());
            } catch (MalformedURLException e) {
                logger.fatal("Unexpected error", e);
                throw new RuntimeException(e);
            }
        }
        // Either RESET_URL_ON_LOGIN is false or input param serverURL 
        // is invalid. Try returning configured Auth endpoint instead.
        return appConfig.getAuthEndpointForCurrentEnv();
    }

    public boolean logout() {
        try {
            PartnerConnection pc = getConnection();
            if (pc != null) pc.logout();
            
        } catch (ConnectionException e) {
            // ignore
        } finally {
            disconnect();
            instance = null;
        }
        return true;
    }

    public void disconnect() {
        clearSession();
        setConnection(null);
    }


    /**
     * @return true if loggedIn
     */
    public boolean isLoggedIn() {
        return getSession().isSessionValid();
    }

    public void validateSession() {
        getSession().validate();
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
    
    public LimitInfo getAPILimitInfo() {
        LimitInfoHeader_element limitInfoElement = getConnection().getLimitInfoHeader();
        for (LimitInfo info : limitInfoElement.getLimitInfo()) {
            if ("API REQUESTS".equalsIgnoreCase(info.getType())) {
                return info;
            }
        }
        return null;
    }
    
    private synchronized ConnectorConfig getLoginConnectorConfig(String serverURL) {
        if (serverURL == null || serverURL.isEmpty()) {
            serverURL = appConfig.getAuthEndpointForCurrentEnv();
        }
        this.connectorConfig = getConnectorConfig();
        this.connectorConfig.setAuthEndpoint(serverURL + getServicePath());
        this.connectorConfig.setServiceEndpoint(serverURL + getServicePath());
        return this.connectorConfig;
    }
    
    private static LoginClient instance = null;
    public static LoginClient getInstance(Controller controller) {
    	if (instance == null || instance.controller != controller) {
			instance = new LoginClient(controller);
		}
    	return instance;
	}
    
    public static boolean isLoginClientInstantiated() {
		return instance != null;
	}
}