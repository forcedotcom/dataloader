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
import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.visitor.rest.RESTConnection;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.SaveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class CompositeRESTClient extends RESTClient<RESTConnection> {
    public static enum ACTION_ENUM {UPDATE, DELETE};
    private static Logger LOG = DLLogManager.getLogger(BulkV2Client.class);

    public CompositeRESTClient(Controller controller) {
        super(controller, LOG);
    }
    
    @Override
    protected boolean connectPostLogin(ConnectorConfig cc) {
        try {
            // Set up a connection object with the given config
            setConnection(new RESTConnection(cc, controller));
        } catch (AsyncApiException e) {
            logger.error(Messages.getMessage(getClass(), "loginError", cc.getAuthEndpoint(), e.getExceptionMessage()),
                    e);
            // Wrap exception. Otherwise, we'll have to change lots of signatures
            throw new RuntimeException(e.getExceptionMessage(), e);
        }
        return true;
    }

    public static String getServicePath() {
        return "/services/data/v" + getAPIVersionForTheSession() + "/composite/sobjects/";
    }

    public SaveResult[] loadAction(ACTION_ENUM action, List<DynaBean> dynabeans) throws ConnectionException {
        return getConnection().loadAction(getSession(), action, dynabeans);
    }

    @Override
    public String getServiceURLPath() {
        return getServicePath();
    }
}