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

package com.salesforce.dataloader.action.visitor.partner;

import java.util.List;

import com.salesforce.dataloader.model.TableRow;

import org.apache.commons.beanutils.DynaBean;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.visitor.DAOLoadVisitor;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.*;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriterInterface;
import com.salesforce.dataloader.exception.*;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.OwnerChangeOption;
import com.sforce.soap.partner.OwnerChangeOptionType;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UndeleteResult;
import com.sforce.soap.partner.UpsertResult;
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

    public PartnerLoadVisitor(Controller controller, ILoaderProgress monitor, DataWriterInterface successWriter,
            DataWriterInterface errorWriter) {
        super(controller, monitor, successWriter, errorWriter);
    }

    @Override
    protected void loadBatch() throws DataAccessObjectException, LoadException {
        Object[] results = null;
        setHeaders();
        try {
            results = executeClientAction(getController().getPartnerClient(), dynaArray);
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
    
    private void setHeaders() {
        setKeepAccountTeamHeader();
    }
    
    private void setKeepAccountTeamHeader() {
        AppConfig appConfig = this.controller.getAppConfig();
        OwnerChangeOption keepAccountTeamOption = new OwnerChangeOption();
        OwnerChangeOption[] ownerChangeOptionArray;
        if (appConfig.getBoolean(AppConfig.PROP_PROCESS_KEEP_ACCOUNT_TEAM) 
                && "Account".equalsIgnoreCase(appConfig.getString(AppConfig.PROP_ENTITY))) {
            // Support for Keeping Account keepAccountTeam during Account ownership change
            // More details at https://developer.salesforce.com/docs/atlas.en-us.api.meta/api/sforce_api_header_ownerchangeoptions.htm
            keepAccountTeamOption.setExecute(true);
            keepAccountTeamOption.setType(OwnerChangeOptionType.KeepAccountTeam); // Transfer Open opportunities owned by the account's owner
            ownerChangeOptionArray = new OwnerChangeOption[] {keepAccountTeamOption};
        } else {
            // clear ownerChangeOptions from the existing connection otherwise.
            ownerChangeOptionArray = new OwnerChangeOption[] {};
        }
        this.controller.getPartnerClient().getConnection().setOwnerChangeOptions(ownerChangeOptionArray);
    }

    private void writeOutputToWriter(Object[] results)
            throws DataAccessObjectException, LoadException {

        // have to do this because although saveResult and deleteResult
        // are a) not the same class yet b) not subclassed
        int batchRowCounter = 0;
        for (int i = 0; i < this.daoRowList.size(); i++) {
            TableRow daoRow = this.daoRowList.get(i);
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
                daoRow.put(AppConfig.STATUS_COLUMN_NAME, statusMsg);
                processResult(daoRow, saveRes.getSuccess(), saveRes.getId(), saveRes.getErrors());
            } else if (results instanceof DeleteResult[]) {
                DeleteResult deleteRes = (DeleteResult)results[batchRowCounter];
                if (deleteRes.getSuccess()) {
                    statusMsg = Messages.getString("DAOLoadVisitor.statusItemDeleted");
                }
                daoRow.put(AppConfig.STATUS_COLUMN_NAME, statusMsg);
                processResult(daoRow, deleteRes.getSuccess(), deleteRes.getId(), deleteRes.getErrors());
            } else if (results instanceof UndeleteResult[]) {
                UndeleteResult undeleteRes = (UndeleteResult)results[batchRowCounter];
                if (undeleteRes.getSuccess()) {
                    statusMsg = Messages.getString("DAOLoadVisitor.statusItemUndeleted");
                }
                daoRow.put(AppConfig.STATUS_COLUMN_NAME, statusMsg);
                processResult(daoRow, undeleteRes.getSuccess(), undeleteRes.getId(), undeleteRes.getErrors());
            } else if (results instanceof UpsertResult[]) {
                UpsertResult upsertRes = (UpsertResult)results[batchRowCounter];
                if (upsertRes.getSuccess()) {
                    statusMsg = upsertRes.getCreated() ? Messages.getString("DAOLoadVisitor.statusItemCreated")
                            : Messages.getString("DAOLoadVisitor.statusItemUpdated");
                }
                daoRow.put(AppConfig.STATUS_COLUMN_NAME, statusMsg);
                processResult(daoRow, upsertRes.getSuccess(), upsertRes.getId(), upsertRes.getErrors());
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

    /**
     * This method performs the actual client action. It must be implemented by all subclasses. It returns an object[]
     * because of saveResult[] and deleteResult[], while do the exact same thing, are two different classes without
     * common inheritance. And we're stuck with it for legacy reasons.
     * 
     * @throws ConnectionException
     */
    protected abstract Object[] executeClientAction(PartnerClient client, List<DynaBean> data)
            throws ConnectionException;

    @Override
    protected int getMaxBytesInBatch() {
        return  AppConfig.MAX_SOAP_API_IMPORT_BATCH_BYTES;
    }
    @Override
    protected int getBytesInBean(DynaBean dynaBean) {
        return dynaBean.toString().length();
    }
}
