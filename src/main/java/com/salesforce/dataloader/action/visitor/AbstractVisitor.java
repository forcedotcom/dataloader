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
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.mapping.Mapper;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.util.LoadRateCalculator;
import org.apache.log4j.Logger;

public abstract class AbstractVisitor implements IVisitor {

    private final Logger logger;

    private final Controller controller;
    private final ILoaderProgress monitor;
    private final DataWriter successWriter;
    private final DataWriter errorWriter;
    private int errors;
    private int successes;
    private final LoadRateCalculator rateCalculator;

    public AbstractVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        this.logger = Logger.getLogger(getClass());
        this.controller = controller;
        this.monitor = monitor;
        this.successWriter = successWriter;
        this.errorWriter = errorWriter;
        this.rateCalculator = new LoadRateCalculator();
    }

    protected abstract boolean writeStatus();

    protected void addSuccess() {
        this.successes++;
    }

    @Override
    public int getNumberOfRows() {
        return getNumberErrors() + getNumberSuccesses();
    }

    @Override
    public int getNumberErrors() {
        return this.errors;
    }

    @Override
    public int getNumberSuccesses() {
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

    protected Config getConfig() {
        return getController().getConfig();
    }

    protected Mapper getMapper() {
        return getController().getMapper();
    }

    protected void writeSuccess(Row row, String id, String message) throws DataAccessObjectException {
        if (writeStatus()) {
            if (id != null && id.length() > 0) {
                row.put(Config.ID_COLUMN_NAME, id);
            }
            if (message != null && message.length() > 0) {
                row.put(Config.STATUS_COLUMN_NAME, message);
            }
            this.successWriter.writeRow(row);
        }
        addSuccess();
    }

    protected void writeError(Row row, String errorMessage) throws DataAccessObjectException {
        if (writeStatus()) {
            if (row == null) {
                row = Row.singleEntryImmutableRow(Config.ERROR_COLUMN_NAME, errorMessage);
            } else {
                row.put(Config.ERROR_COLUMN_NAME, errorMessage);
            }
            this.errorWriter.writeRow(row);
        }
        this.errors++;
    }

    protected LoadRateCalculator getRateCalculator() {
        return this.rateCalculator;
    }
}
