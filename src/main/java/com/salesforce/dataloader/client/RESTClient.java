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

import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.visitor.RESTConnection;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class RESTClient extends ClientBase<RESTConnection> {
    private static Logger LOG = LogManager.getLogger(BulkV2Client.class);
    private RESTConnection client;
    private ConnectorConfig connectorConfig = null;

    public RESTClient(Controller controller) {
        super(controller, LOG);
    }
    
    public RESTConnection getConnection() {
        return client;
    }
    
    @Override
    protected boolean connectPostLogin(ConnectorConfig cc) {
        try {
            // Set up a connection object with the given config
            this.client = new RESTConnection(cc, controller);

        } catch (AsyncApiException e) {
            logger.error(Messages.getMessage(getClass(), "loginError", cc.getAuthEndpoint(), e.getExceptionMessage()),
                    e);
            // Wrap exception. Otherwise, we'll have to change lots of signatures
            throw new RuntimeException(e.getExceptionMessage(), e);
        }
        return true;
    }

    public synchronized ConnectorConfig getConnectorConfig() {
        this.connectorConfig = super.getConnectorConfig();
        
        // override the restEndpoint value set in the superclass
        String server = getSession().getServer();
        if (server != null) {
            this.connectorConfig.setRestEndpoint(server + getServicePath());
        }
        return this.connectorConfig;
    }

    protected static String getServicePath() {
        return "/services/data/v" + getAPIVersionForTheSession() + "/";
    }

    public Object[] loadUpdates(List<DynaBean> dynabeans) throws ConnectionException {
        logger.debug(Messages.getFormattedString("Client.beginOperation", "update")); //$NON-NLS-1$
        int totalAttempts = 1 + (isRetriesEnabled() ? getMaxRetries() : 0);
        ConnectionException connectionException = null;
        for (int tryNum = 0; tryNum < totalAttempts; tryNum++) {
        try {
            SaveResult[] result = null;
            if (result == null)
                logger.info(Messages.getString("Client.resultNull")); //$NON-NLS-1$
            this.getSession().performedSessionActivity(); // reset session activity timer
            throw new ConnectionException();
        } catch (ConnectionException ex) {
            logger.error(
                    Messages.getFormattedString(
                            "Client.operationError", new String[]{"update", ex.getMessage()}), ex); //$NON-NLS-1$
            if (ex instanceof ApiFault) {
                ApiFault fault = (ApiFault)ex;
                String faultMessage = fault.getExceptionMessage();
                logger.error(
                        Messages.getFormattedString(
                                "Client.operationError", new String[]{"update", faultMessage}), fault); //$NON-NLS-1$

            }
            // check retries
            if (!checkConnectionException(ex, "update", tryNum)) throw ex;
            connectionException = ex;
        }
    }
    throw connectionException;
    }
    
    /**
     * Checks whether retry makes sense for the given exception and given the number of current vs. max retries. If
     * retry makes sense, then before returning, this method will put current thread to sleep before allowing another
     * retry.
     *
     * @param ex
     * @param operationName
     * @return true if retry should be executed for operation. false if there's no retry.
     */
    private boolean checkConnectionException(ConnectionException ex, String operationName, int retryNum) {
        if (!isRetriesEnabled()) return false;
        final String msg = ex.getMessage();
        if (msg != null && msg.toLowerCase().indexOf("connection reset") >= 0) {
            retrySleep("update", retryNum);
            return true;
        }
        return false;
    }
    
    /**
     * @param operationName
     */
    private void retrySleep(String operationName, int retryNum) {
        int sleepSecs;
        try {
            sleepSecs = config.getInt(Config.MIN_RETRY_SLEEP_SECS);
        } catch (ParameterLoadException e1) {
            sleepSecs = Config.DEFAULT_MIN_RETRY_SECS;
        }
        // sleep between retries is based on the retry attempt #. Sleep for longer periods with each retry
        sleepSecs = sleepSecs + (retryNum * 10); // sleep for MIN_RETRY_SLEEP_SECS + 10, 20, 30, etc.

        logger.info(Messages.getFormattedString("Client.retryOperation", new String[]{Integer.toString(retryNum + 1),
                operationName, Integer.toString(sleepSecs)}));
        try {
            Thread.sleep(sleepSecs * 1000);
        } catch (InterruptedException e) { // ignore
        }
    }
}
