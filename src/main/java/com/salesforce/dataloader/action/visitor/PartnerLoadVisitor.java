/*
 * Copyright (c) 2012, salesforce.com, inc.
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
import java.util.List;
import java.util.Map;

import com.salesforce.dataloader.model.Row;
import org.apache.commons.beanutils.DynaBean;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.*;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.*;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Base class for load visitors using the partner api client.
 *
 * @author Colin Jarvis
 * @since 17.0
 */
public abstract class PartnerLoadVisitor extends DAOLoadVisitor {

    public PartnerLoadVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);
    }

    @Override
    protected void loadBatch() throws DataAccessObjectException, LoadException {
        Object[] results = null;
        try {
            results = executeClientAction(getController().getPartnerClient(), dynaArray);
        } catch (ApiFault e) {
            handleException(e);
        } catch (ConnectionException e) {
            handleException(e);
        }

        // set the current processed
        int currentProcessed;
        try {
            currentProcessed = getConfig().getInt(LastRun.LAST_LOAD_BATCH_ROW);
        } catch (ParameterLoadException e) {
            // if there's a problem getting last batch row, start at the beginning
            currentProcessed = 0;
        }
        currentProcessed += results.length;
        getConfig().setValue(LastRun.LAST_LOAD_BATCH_ROW, currentProcessed);
        try {
            getConfig().saveLastRun();
        } catch (IOException e) {
            String errMsg = Messages.getString("LoadAction.errorLastRun");
            getLogger().error(errMsg, e);
            handleException(errMsg, e);
        }

        writeOutputToWriter(results, dataArray);

        // update Monitor
        getProgressMonitor().worked(results.length);
        getProgressMonitor().setSubTask(getRateCalculator().calculateSubTask(getNumberOfRows(), getNumberErrors()));

        // now clear the arrays
        clearArrays();

    }

    private void writeOutputToWriter(Object[] results, List<Row> dataArray)
            throws DataAccessObjectException, LoadException {

        if (results.length != dataArray.size()) {
            getLogger().fatal(Messages.getString("Visitor.errorResultsLength")); //$NON-NLS-1$
            throw new LoadException(Messages.getString("Visitor.errorResultsLength"));
        }

        // have to do this because although saveResult and deleteResult
        // are a) not the same class yet b) not subclassed
        for (int i = 0; i < results.length; i++) {
            Row dataRow = dataArray.get(i);
            String statusMsg = null;
            if (results instanceof SaveResult[]) {
                SaveResult saveRes = (SaveResult)results[i];
                if (saveRes.getSuccess()) {
                    if (OperationInfo.insert == getConfig().getOperationInfo()) {
                        statusMsg = Messages.getString("DAOLoadVisitor.statusItemCreated");
                    } else {
                        statusMsg = Messages.getString("DAOLoadVisitor.statusItemUpdated");
                    }
                }
                dataRow.put(Config.STATUS_COLUMN_NAME, statusMsg);
                processResult(dataRow, saveRes.getSuccess(), saveRes.getId(), saveRes.getErrors());
            } else if (results instanceof DeleteResult[]) {
                DeleteResult deleteRes = (DeleteResult)results[i];
                if (deleteRes.getSuccess()) {
                    statusMsg = Messages.getString("DAOLoadVisitor.statusItemDeleted");
                }
                dataRow.put(Config.STATUS_COLUMN_NAME, statusMsg);
                processResult(dataRow, deleteRes.getSuccess(), deleteRes.getId(), deleteRes.getErrors());
            } else if (results instanceof UpsertResult[]) {
                UpsertResult upsertRes = (UpsertResult)results[i];
                if (upsertRes.getSuccess()) {
                    statusMsg = upsertRes.getCreated() ? Messages.getString("DAOLoadVisitor.statusItemCreated")
                            : Messages.getString("DAOLoadVisitor.statusItemUpdated");
                }
                dataRow.put(Config.STATUS_COLUMN_NAME, statusMsg);
                processResult(dataRow, upsertRes.getSuccess(), upsertRes.getId(), upsertRes.getErrors());
            }
        }
    }

    private void processResult(Row dataRow, boolean isSuccess, String id, Error[] errors)
            throws DataAccessObjectException {
        // process success vs. error
        // extract error message from error result
        if (isSuccess) {
            writeSuccess(dataRow, id, null);
        } else {
            writeError(dataRow,
                    errors == null ? Messages.getString("Visitor.noErrorReceivedMsg") : errors[0].getMessage());
        }
    }

    /**
     * This method performs the actual client action. It must be implemented by all subclasses. It returns an object[]
     * because of saveResult[] and deleteResult[], while do the exact same thing, are two different classes without
     * common inheritance. And we're stuck with it for legacy reasons.
     * 
     * @throws ConnectionException
     */
    protected abstract Object[] executeClientAction(PartnerClient client, List<DynaBean> data)
            throws ConnectionException;

}
