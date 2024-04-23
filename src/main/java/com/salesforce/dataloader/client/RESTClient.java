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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.visitor.RESTConnection;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.sforce.async.AsyncApiException;
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
        if (this.connectorConfig == null || !this.config.getBoolean(Config.REUSE_CLIENT_CONNECTION)) {
            this.connectorConfig = super.getConnectorConfig();
            
            // override the restEndpoint value set in the superclass
            String server = getSession().getServer();
            if (server != null) {
                this.connectorConfig.setRestEndpoint(server + getServicePath());
            }
            this.connectorConfig.setTraceMessage(config.getBoolean(Config.WIRE_OUTPUT));
        }
        return this.connectorConfig;
    }

    protected static String getServicePath() {
        return "/services/data/v" + getAPIVersionForTheSession() + "/";
    }
}
