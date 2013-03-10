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

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.ExtractException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.mapping.SOQLMapper;
import com.salesforce.dataloader.model.Row;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Superclass for all query visitors
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
abstract class AbstractQueryVisitor extends AbstractVisitor implements IQueryVisitor {

    private final DataWriter queryWriter;
    private final String soql;
    private final List<Row> batchRows;
    private final List<String> batchIds;
    private final int batchSize;

    public AbstractQueryVisitor(Controller controller, ILoaderProgress monitor, DataWriter queryWriter,
            DataWriter successWriter, DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);
        this.queryWriter = queryWriter;
        this.soql = getConfig().getString(Config.EXTRACT_SOQL);
        this.batchRows = new LinkedList<Row>();
        this.batchIds = new LinkedList<String>();
        this.batchSize = getWriteBatchSize();
    }

    @Override
    public final void visit() throws DataAccessObjectException, OperationException {
        try {
            if (getProgressMonitor().isCanceled()) return;
            final int size = executeQuery(getSoql());
            if (size == 0) {
                getLogger().info(Messages.getMessage(getClass(), "noneReturned"));
            } else {
                if (getProgressMonitor().isCanceled()) return;
                startWriteExtraction(size);
                writeExtraction();
                flushResults();
            }
        } catch (final ApiFault e) {
            throw new ExtractException(e.getExceptionMessage(), e);
        } catch (final ConnectionException e) {
            throw new ExtractException(e.getMessage(), e);
        } catch (final AsyncApiException e) {
            throw new ExtractException(e.getExceptionMessage(), e);
        }
    }

    protected abstract void writeExtraction() throws AsyncApiException, ExtractException, DataAccessObjectException,
    ConnectionException;

    protected abstract int executeQuery(String soql) throws ConnectionException, AsyncApiException, OperationException;

    @Override
    protected boolean writeStatus() {
        return getConfig().getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT);
    }

    private String getSoql() {
        return this.soql;
    }

    private DataWriter getQueryWriter() {
        return this.queryWriter;
    }

    protected void addResultRow(Row row, String id) throws DataAccessObjectException {
        this.batchRows.add(row);
        this.batchIds.add(id);
        if (this.batchSize == this.batchRows.size()) {
            writeBatch();
        }
    }

    private void flushResults() throws DataAccessObjectException {
        if (!this.batchRows.isEmpty()) {
            writeBatch();
        }
    }

    private void writeBatch() throws DataAccessObjectException {
        if (getProgressMonitor().isCanceled()) return;
        try {
            if (getQueryWriter().writeRowList(this.batchRows)) {
                writeSuccesses();
            } else {
                writeErrors(Messages.getMessage(getClass(), "statusErrorNotWritten",
                        getConfig().getString(Config.DAO_NAME)));
            }
            getProgressMonitor().worked(this.batchRows.size());
            getProgressMonitor().setSubTask(getRateCalculator().calculateSubTask(getNumberOfRows(), getNumberErrors()));
        } catch (final DataAccessObjectInitializationException ex) {
            throw ex;
        } catch (final DataAccessObjectException ex) {
            writeErrors(Messages.getMessage(getClass(), "statusErrorNotWrittenException",
                    getConfig().getString(Config.DAO_NAME), ex.getMessage()));
        } finally {
            this.batchRows.clear();
            this.batchIds.clear();
        }
    }

    private void writeSuccesses() throws DataAccessObjectException {
        final String msg = Messages.getMessage(getClass(), "statusItemQueried");
        final Iterator<String> ids = this.batchIds.iterator();
        for (final Row row : this.batchRows) {
            writeSuccess(row, ids.next(), msg);
        }
    }

    private void writeErrors(String errorMessage) throws DataAccessObjectException {
        for (final Row row : this.batchRows) {
            writeError(row, errorMessage);
        }
    }

    protected int getWriteBatchSize() {
        int daoBatchSize;
        try {
            daoBatchSize = getConfig().getInt(Config.DAO_WRITE_BATCH_SIZE);
            if (daoBatchSize > Config.MAX_DAO_WRITE_BATCH_SIZE) {
                daoBatchSize = Config.MAX_DAO_WRITE_BATCH_SIZE;
            }
        } catch (final ParameterLoadException e) {
            // warn about getting batch size parameter, otherwise continue w/ default
            getLogger().warn(
                    Messages.getMessage(getClass(), "errorGettingBatchSize",
                            String.valueOf(Config.DEFAULT_DAO_WRITE_BATCH_SIZE), e.getMessage()));
            daoBatchSize = Config.DEFAULT_DAO_WRITE_BATCH_SIZE;
        }
        return daoBatchSize;
    }

    protected void startWriteExtraction(int size) {
        getRateCalculator().start();
        getRateCalculator().setNumRecords(size);
        // start the Progress Monitor
        getProgressMonitor().beginTask(Messages.getMessage(getClass(), "extracting"), size); //$NON-NLS-1$
    }

    @Override
    protected SOQLMapper getMapper() {
        return (SOQLMapper)super.getMapper();
    }

}
