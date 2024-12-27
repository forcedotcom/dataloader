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

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.mapping.Mapper;
import com.salesforce.dataloader.model.TableRow;
import com.salesforce.dataloader.util.LoadRateCalculator;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Map;

import com.salesforce.dataloader.util.DLLogManager;

public abstract class AbstractVisitor implements IVisitor {

    private final Logger logger;

    protected final Controller controller;
    private final ILoaderProgress monitor;
    private DataWriter successWriter;
    private DataWriter errorWriter;
    private long errors;
    private long successes;
    private LoadRateCalculator rateCalculator;

    public AbstractVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        this(controller, monitor, successWriter, errorWriter, null);
    }
    
    public AbstractVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter, LoadRateCalculator rateCalculator) {
        this.logger = DLLogManager.getLogger(getClass());
        this.controller = controller;
        this.monitor = monitor;
        this.successWriter = successWriter;
        this.errorWriter = errorWriter;
        if (rateCalculator == null) {
            this.rateCalculator = new LoadRateCalculator();
        } else {
            this.rateCalculator = rateCalculator;
        }
    }
    
    public LoadRateCalculator getLoadRateCalculator() {
        return this.rateCalculator;
    }

    protected abstract boolean writeStatus();
    
    protected void setSuccessWriter(DataWriter successWriter) {
        this.successWriter = successWriter;
    }
    protected void setErrorWriter(DataWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    protected void addSuccess() {
        this.successes++;
    }
    
    protected void addErrors() {
        this.errors++;
    }
    
    protected void setSuccesses(long num) {
    	this.successes = num;
    }
    
    protected void setErrors(long num) {
    	this.errors = num;
    }

    @Override
    public long getNumberOfRows() {
        return getNumberErrors() + getNumberSuccesses();
    }

    @Override
    public long getNumberErrors() {
        return this.errors;
    }

    @Override
    public long getNumberSuccesses() {
        return this.successes;
    }

    protected Controller getController() {
        return this.controller;
    }

    protected Logger getLogger() {
        return this.logger;
    }

    protected ILoaderProgress getProgressMonitor() {
        return this.monitor;
    }

    protected AppConfig getConfig() {
        return getController().getAppConfig();
    }

    protected Mapper getMapper() {
        return getController().getMapper();
    }
    
    protected DataWriter getErrorWriter() {
    	return this.errorWriter;
    }
    
    protected DataWriter getSuccessWriter() {
    	return this.successWriter;
    }

    protected void writeSuccess(TableRow row, String id, String message) throws DataAccessObjectException {
        if (writeStatus()) {
            if (id != null && id.length() > 0) {
                row.put(AppConfig.ID_COLUMN_NAME, id);
            }
            if (message != null && message.length() > 0) {
                row.put(AppConfig.STATUS_COLUMN_NAME, message);
            }
            this.successWriter.writeRow(row);
        }
        addSuccess();
    }

    protected void writeError(TableRow row, String errorMessage) throws DataAccessObjectException {
        if (writeStatus()) {
            if (row == null) {
                row = TableRow.singleEntryImmutableRow(AppConfig.ERROR_COLUMN_NAME, errorMessage);
            } else {
                row.put(AppConfig.ERROR_COLUMN_NAME, errorMessage);
            }
            this.errorWriter.writeRow(row);
        }
        addErrors();
    }

    protected LoadRateCalculator getRateCalculator() {
        return this.rateCalculator;
    }
    
    // Subclasses that support attachments (e.g. BulkLoadVisitor) need to override the method
    @Override
    public Map<String, InputStream> getAttachments() {
        return null;
    }

}
