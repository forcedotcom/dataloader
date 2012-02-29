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

package com.salesforce.dataloader.action;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.maven.wagon.ConnectionException;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.visitor.IVisitor;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObject;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.exception.*;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.fault.ApiFault;

/**
 * Abstract class for all dataloader actions.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
abstract class AbstractAction implements IAction {

    private final ILoaderProgress monitor;
    private final Controller controller;
    private final IVisitor visitor;

    private final DataWriter successWriter;
    private final DataWriter errorWriter;
    private final DataAccessObject dao;

    private final Logger logger;

    protected AbstractAction(Controller controller, ILoaderProgress monitor)
            throws DataAccessObjectInitializationException {
        this.logger = Logger.getLogger(getClass());
        this.monitor = monitor;
        this.controller = controller;
        checkDao(getController().getDao());
        this.dao = getController().getDao();
        if (writeStatus()) {
            this.successWriter = getSuccessWriter(getConfig().getBoolean(Config.WRITE_UTF8));
            this.errorWriter = getErrorWriter(getConfig().getBoolean(Config.WRITE_UTF8));
        } else {
            this.successWriter = null;
            this.errorWriter = null;
        }
        this.visitor = createVisitor();
    }

    /** This method should throw an error if the data access object is configured incorrectly */
    protected abstract void checkDao(DataAccessObject dao) throws DataAccessObjectInitializationException;

    /** @return a new IVisitor object to be used by this action */
    protected abstract IVisitor createVisitor();

    /** flushes any remaining records to or from the dao */
    protected abstract void flush() throws OperationException, DataAccessObjectException;

    /** subclasses should do operation specific initialization here */
    protected abstract void initOperation() throws DataAccessObjectInitializationException, OperationException,
    MappingInitializationException, DataAccessObjectException;

    /** @return a list of the data columns that will be in the success and error files */
    protected abstract List<String> getStatusColumns() throws OperationException;

    /**
     * Process some records.
     * 
     * @return true If there are more rows to process
     * @throws com.sforce.ws.ConnectionException 
     */
    protected abstract boolean visit() throws OperationException, DataAccessObjectException, ParameterLoadException,
    ConnectionException, AsyncApiException, com.sforce.ws.ConnectionException;

    /**
     * @return true if error and success files should be used in this operation
     */
    protected abstract boolean writeStatus();

    @Override
    public final void execute() {
        try {
            getLogger().info(getMessage("loading", getConfig().getString(Config.OPERATION)));
            getDao().open();
            initOperation();
            if (writeStatus()) {
                final List<String> statusColumns = getStatusColumns();
                openSuccessWriter(statusColumns);
                openErrorWriter(statusColumns);
            }

            while (!getMonitor().isCanceled() && visit()) {}

            flush();
            // need to close up here for sync message.
            closeAll();

            final Object[] args = { String.valueOf(getVisitor().getNumberSuccesses()),
                    getConfig().getString(Config.OPERATION), String.valueOf(getVisitor().getNumberErrors()) };

            // set the monitor to done
            if (getMonitor().isCanceled()) {
                getMonitor().doneSuccess(getMessage("cancel", args)); //$NON-NLS-1$
            } else {
                getMonitor().doneSuccess(getMessage("success", args)); //$NON-NLS-1$
            }

        } catch (final Exception e) {
            handleException(e);
        } finally {
            closeAll();
        }
    }

    private void closeAll() {
        getDao().close();
        if (writeStatus()) {
            getSuccessWriter().close();
            getErrorWriter().close();
        }
    }

    protected Config getConfig() {
        return getController().getConfig();
    }

    protected Controller getController() {
        return this.controller;
    }

    protected DataAccessObject getDao() {
        return this.dao;
    }

    protected DataWriter getErrorWriter() {
        return this.errorWriter;
    }

    protected Logger getLogger() {
        return this.logger;
    }

    protected String getMessage(String key, Object... args) {
        final String msg = Messages.getMessage(getClass(), key, true, args);
        return msg == null ? Messages.getMessage("Action", key, false, args) : msg;
    }

    protected ILoaderProgress getMonitor() {
        return this.monitor;
    }

    protected DataWriter getSuccessWriter() {
        return this.successWriter;
    }

    protected IVisitor getVisitor() {
        return this.visitor;
    }

    protected void handleException(Exception e) {
        String errMsg = e.getMessage();
        if (e instanceof ApiFault) {
            errMsg = ((ApiFault)e).getExceptionMessage();
        } else if (e instanceof AsyncApiException) {
            errMsg = ((AsyncApiException)e).getExceptionMessage();
        }
        getLogger().error(getMessage("exception"), e);
        getMonitor().doneError(errMsg);
    }

    /**
     * @return Error Writer
     * @throws DataAccessObjectInitializationException
     */
    private DataWriter getErrorWriter(boolean writeUtf8) throws DataAccessObjectInitializationException {
        final String filename = getConfig().getString(Config.OUTPUT_ERROR);
        if (filename == null || filename.length() == 0)
            throw new DataAccessObjectInitializationException(getMessage("errorMissingErrorFile"));
        // TODO: Make sure that specific DAO is not mentioned: use DataReader, DataWriter, or DataAccessObject
        return new CSVFileWriter(filename, writeUtf8, true);
    }

    /**
     * @return Success Writer
     * @throws DataAccessObjectInitializationException
     */
    private DataWriter getSuccessWriter(boolean writeUtf8) throws DataAccessObjectInitializationException {
        final String filename = getConfig().getString(Config.OUTPUT_SUCCESS);
        if (filename == null || filename.length() == 0)
            throw new DataAccessObjectInitializationException(getMessage("errorMissingSuccessFile"));
        // TODO: Make sure that specific DAO is not mentioned: use DataReader, DataWriter, or DataAccessObject
        return new CSVFileWriter(filename, writeUtf8, true);
    }

    private void openErrorWriter(List<String> headers) throws OperationException {
        headers = new LinkedList<String>(headers);

        // add the ERROR column
        headers.add(Config.ERROR_COLUMN_NAME);
        try {
            getErrorWriter().open();
            getErrorWriter().setColumnNames(headers);
        } catch (final DataAccessObjectInitializationException e) {
            throw new OperationException(
                    getMessage("errorOpeningErrorFile", getConfig().getString(Config.OUTPUT_ERROR)), e);
        }
    }

    private void openSuccessWriter(List<String> headers) throws LoadException {
        headers = new LinkedList<String>(headers);

        // add the ID column if not there already
        if (!Config.ID_COLUMN_NAME.equals(headers.get(0))) {
            headers.add(0, Config.ID_COLUMN_NAME);
        }
        headers.add(Config.STATUS_COLUMN_NAME);
        try {
            getSuccessWriter().open();
            getSuccessWriter().setColumnNames(headers);
        } catch (final DataAccessObjectInitializationException e) {
            throw new LoadException(
                    getMessage("errorOpeningSuccessFile", getConfig().getString(Config.OUTPUT_SUCCESS)), e);
        }
    }

}
