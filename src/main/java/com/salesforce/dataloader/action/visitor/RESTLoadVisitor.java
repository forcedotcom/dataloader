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


import java.util.List;

import org.apache.commons.beanutils.DynaBean;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.CompositeRESTClient;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.LoadException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.model.Row;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;

public abstract class RESTLoadVisitor extends DAOLoadVisitor {

    public RESTLoadVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);
    }

    protected void loadBatch() throws DataAccessObjectException, OperationException {
        Object[] results = null;
        setHeaders();
        try {
            // executeClientAction() is implemented by concrete subclasses
            results = executeClientAction(getController().getRESTClient(), dynaArray);
        } catch (ApiFault e) {
            handleException(e);
        } catch (ConnectionException e) {
            handleException(e);
        }

        writeOutputToWriter(results);
        setLastRunProperties(results);

        // update Monitor
        getProgressMonitor().worked(results.length);
        getProgressMonitor().setSubTask(getRateCalculator().calculateSubTask(getNumberOfRows(), getNumberErrors()));

        // now clear the arrays
        clearArrays();
    }

    private void writeOutputToWriter(Object[] results)
            throws DataAccessObjectException, LoadException {
        // have to do this because although saveResult and deleteResult
        // are a) not the same class yet b) not subclassed
        int batchRowCounter = 0;
        for (int i = 0; i < this.daoRowList.size(); i++) {
            Row daoRow = this.daoRowList.get(i);
            if (!isRowConversionSuccessful()) {
                continue;
            }
            String statusMsg = null;
            if (results instanceof SaveResult[]) {
                SaveResult saveRes = (SaveResult)results[batchRowCounter];
                if (saveRes.getSuccess()) {
                    if (OperationInfo.insert == getConfig().getOperationInfo()) {
                        statusMsg = Messages.getString("DAOLoadVisitor.statusItemCreated");
                    } else {
                        statusMsg = Messages.getString("DAOLoadVisitor.statusItemUpdated");
                    }
                }
                daoRow.put(Config.STATUS_COLUMN_NAME, statusMsg);
                processResult(daoRow, saveRes.getSuccess(), saveRes.getId(), saveRes.getErrors());
            }
            batchRowCounter++;
            if (results.length < batchRowCounter) {
                getLogger().fatal(Messages.getString("Visitor.errorResultsLength")); //$NON-NLS-1$
                throw new LoadException(Messages.getString("Visitor.errorResultsLength"));
            }
        }
        if (results.length > batchRowCounter) {
            getLogger().fatal(Messages.getString("Visitor.errorResultsLength")); //$NON-NLS-1$
            throw new LoadException(Messages.getString("Visitor.errorResultsLength"));
        }        
    }

    private void setHeaders() {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * This method performs the actual client action. It must be implemented by all subclasses. It returns an object[]
     * because of saveResult[] and deleteResult[], while do the exact same thing, are two different classes without
     * common inheritance. And we're stuck with it for legacy reasons.
     * 
     * @throws ConnectionException
     */
    protected abstract Object[] executeClientAction(CompositeRESTClient client, List<DynaBean> data)
            throws ConnectionException;
}