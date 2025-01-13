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

package com.salesforce.dataloader.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import com.salesforce.dataloader.util.DLLogManager;
import com.salesforce.dataloader.util.LoadRateCalculator;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.visitor.IVisitor;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObjectInterface;
import com.salesforce.dataloader.dao.DataWriterInterface;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.exception.BatchSizeLimitException;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.ExtractExceptionOnServer;
import com.salesforce.dataloader.exception.HttpClientTransportException;
import com.salesforce.dataloader.exception.LoadException;
import com.salesforce.dataloader.exception.LoadExceptionOnServer;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;

/**
 * Abstract class for all dataloader actions.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
abstract class AbstractAction implements IAction {

    private final ILoaderProgress monitor;
    private final Controller controller;
    private IVisitor visitor;

    protected DataWriterInterface successWriter;
    protected DataWriterInterface errorWriter;
    private final DataAccessObjectInterface dao;

    private final Logger logger;
    private boolean enableRetries;
    private int maxRetries;

    protected AbstractAction(Controller controller, ILoaderProgress monitor)
            throws DataAccessObjectInitializationException {
        this.logger = DLLogManager.getLogger(getClass());
        this.monitor = monitor;
        this.controller = controller;
        checkDao(getController().getDao());
        this.dao = getController().getDao();
        if (writeStatus()) {
            this.successWriter = createSuccesWriter();
            this.errorWriter = createErrorWriter();
        } else {
            this.successWriter = null;
            this.errorWriter = null;
        }
        // Let the IVisitor instance create LoadRateCalculator instance for the first job by passing
        // null as the first argument.
        this.visitor = createVisitor(null, true);
        int retries = -1;
        this.enableRetries = controller.getAppConfig().getBoolean(AppConfig.PROP_ENABLE_RETRIES);
        if (this.enableRetries) {
            try {
                // limit the number of max retries in case limit is exceeded
                retries = Math.min(AppConfig.MAX_RETRIES_LIMIT, controller.getAppConfig().getInt(AppConfig.PROP_MAX_RETRIES));
            } catch (ParameterLoadException e) {
                retries = AppConfig.DEFAULT_MAX_RETRIES;
            }
        }
        this.maxRetries = retries;
    }

    /** This method should throw an error if the data access object is configured incorrectly */
    protected abstract void checkDao(DataAccessObjectInterface dao) throws DataAccessObjectInitializationException;

    /** @return a new IVisitor object to be used by this action */
    protected abstract IVisitor createVisitor(LoadRateCalculator rateCalculator, boolean isFirstJob);

    /** flushes any remaining records to or from the dao 
     * @throws BatchSizeLimitException */
    protected abstract void flush() throws OperationException, DataAccessObjectException, BatchSizeLimitException;

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
        List<Exception> exceptions = null;
        for (int numAttempts = 0; numAttempts < this.maxRetries; numAttempts++) {
                exceptions = executeOperation();
                boolean doAttemptAgain = false;
                if (exceptions != null && exceptions.size() > 0) {
                    for (Exception e : exceptions) {
                        if (shouldRetryOperation(e, numAttempts)) {
                            doAttemptAgain = true;
                            break;
                        }
                    }
                }
                if (!doAttemptAgain) {
                    break; // stop the loop
                }
        }
        if (exceptions != null && exceptions.size() > 0) {
            exceptions.forEach(this::handleException);
        }
    }
    
    private List<Exception> executeOperation() {
        List<Exception> exceptions = new ArrayList<>();
        try {
            getLogger().info(getMessage("loading", getConfig().getString(AppConfig.PROP_OPERATION)));
            getDao().open();
            initOperation();
            if (writeStatus()) {
                final List<String> statusColumns = getStatusColumns();
                openSuccessWriter(statusColumns);
                openErrorWriter(statusColumns);
            }

            while (!getMonitor().isCanceled() && visit()) {}
            
        } catch (final Exception e) {
            exceptions.add(e);
        } finally {
            try {
                flush(); //make sure we don't early abort here
            } catch (Exception e) {
                exceptions.add(e);
            }
            try {
                closeAll(); //make sure we don't early abort here
            } catch (Exception e){
                exceptions.add(e);
            }
            try {
                if (this.errorWriter != null) {
                    getMonitor().setNumberRowsWithError(this.errorWriter.getCurrentRowNumber());
                }
                //if no exceptions occurred then display success/error
                if (exceptions.size() == 0) {
                    final Object[] args = {String.valueOf(getVisitor().getNumberSuccesses()),
                            getConfig().getString(AppConfig.PROP_OPERATION), String.valueOf(getVisitor().getNumberErrors())};

                    // set the monitor to done
                    if (getMonitor().isCanceled()) {
                        getMonitor().doneSuccess(getMessage("cancel", args)); //$NON-NLS-1$
                    } else {
                        getMonitor().doneSuccess(getMessage("success", args)); //$NON-NLS-1$
                    }
                }
            } catch (Exception e){
                exceptions.add(e);
            }
        }
        return exceptions;
    }
    
    private boolean shouldRetryOperation(Exception e, int numAttempts) {
        if (e instanceof HttpClientTransportException
                || e instanceof ExtractExceptionOnServer
                || e instanceof LoadExceptionOnServer
                || e instanceof ConnectionException) {
            if (numAttempts < this.maxRetries-1 && this.enableRetries) {
                // loop only if less than MAX_RETRIES
                logger.warn("Encountered an error on server when performing "
                        + controller.getAppConfig().getString(AppConfig.PROP_OPERATION) 
                        + " on attempt " 
                        + numAttempts );
                logger.warn(e.getMessage());
                retrySleep(numAttempts);
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param operationName
     */
    private void retrySleep(int retryNum) {
        int sleepSecs;
        try {
            sleepSecs = controller.getAppConfig().getInt(AppConfig.PROP_MIN_RETRY_SLEEP_SECS);
        } catch (ParameterLoadException e1) {
            sleepSecs = AppConfig.DEFAULT_MIN_RETRY_SECS;
        }
        // sleep between retries is based on the retry attempt #. Sleep for longer periods with each retry
        sleepSecs = sleepSecs + (retryNum * 10); // sleep for MIN_RETRY_SLEEP_SECS + 10, 20, 30, etc.

        logger.info(Messages.getFormattedString("Client.retryOperation", 
                new String[]{Integer.toString(retryNum + 1),
                getController().getAppConfig().getString(AppConfig.PROP_OPERATION), 
                Integer.toString(sleepSecs)}));
        try {
            Thread.sleep(sleepSecs * 1000);
        } catch (InterruptedException e) { // ignore
        }
    }

    private void closeAll() {
        getDao().close();
        if (this.successWriter != null) {
            this.successWriter.close();
        }
        if (this.errorWriter != null) {
            this.errorWriter.close();
        }
    }

    protected AppConfig getConfig() {
        return getController().getAppConfig();
    }

    protected Controller getController() {
        return this.controller;
    }

    protected DataAccessObjectInterface getDao() {
        return this.dao;
    }

    protected DataWriterInterface getErrorWriter() {
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

    protected DataWriterInterface getSuccessWriter() {
        return this.successWriter;
    }

    public IVisitor getVisitor() {
        return this.visitor;
    }
    
    protected void setVisitor(IVisitor newVisitor) {
        this.visitor = newVisitor;
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
    public DataWriterInterface createErrorWriter() throws DataAccessObjectInitializationException {
        final String filename = getConfig().getString(AppConfig.PROP_OUTPUT_ERROR);
        if (filename == null || filename.length() == 0)
            throw new DataAccessObjectInitializationException(getMessage("errorMissingErrorFile"));
        // TODO: Make sure that specific DAO is not mentioned: use DataReader, DataWriter, or DataAccessObject
        this.errorWriter = new CSVFileWriter(filename, getConfig(), AppUtil.COMMA);
        return this.errorWriter;
    }

    /**
     * @return Success Writer
     * @throws DataAccessObjectInitializationException
     */
    public DataWriterInterface createSuccesWriter() throws DataAccessObjectInitializationException {
        final String filename = getConfig().getString(AppConfig.PROP_OUTPUT_SUCCESS);
        if (filename == null || filename.length() == 0)
            throw new DataAccessObjectInitializationException(getMessage("errorMissingSuccessFile"));
        // TODO: Make sure that specific DAO is not mentioned: use DataReader, DataWriter, or DataAccessObject
        this.successWriter = new CSVFileWriter(filename, getConfig(), AppUtil.COMMA);
        return this.successWriter;
    }

    public void openErrorWriter(List<String> headers) throws OperationException {
        headers = new ArrayList<String>(headers);
        AppConfig appConfig = this.controller.getAppConfig();

        if (appConfig.isBulkV2APIEnabled()
        	&& !appConfig.getString(AppConfig.PROP_OPERATION).equals(OperationInfo.extract.name())
        	&& !appConfig.getString(AppConfig.PROP_OPERATION).equals(OperationInfo.extract_all.name())) {
            headers.add(0, AppConfig.ID_COLUMN_NAME);
        	headers.add(1, AppConfig.ERROR_COLUMN_NAME);
        } else {
	        // add the ERROR column
	        headers.add(AppConfig.ERROR_COLUMN_NAME);
        }
        try {
            getErrorWriter().open();
            getErrorWriter().setColumnNames(headers);
        } catch (final DataAccessObjectInitializationException e) {
            throw new OperationException(
                    getMessage("errorOpeningErrorFile", getConfig().getString(AppConfig.PROP_OUTPUT_ERROR)), e);
        }
    }

    public void openSuccessWriter(List<String> headers) throws LoadException {
        headers = new ArrayList<String>(headers);
        AppConfig appConfig = this.controller.getAppConfig();

        // add the ID column if not there already
        if (appConfig.isBulkV2APIEnabled()
        	&& !appConfig.getString(AppConfig.PROP_OPERATION).equals(OperationInfo.extract.name())
        	&& !appConfig.getString(AppConfig.PROP_OPERATION).equals(OperationInfo.extract_all.name())) {
            if (headers.size() == 0 || !AppConfig.ID_COLUMN_NAME.equals(headers.get(0))) {
                headers.add(0, AppConfig.ID_COLUMN_NAME);
            }
            headers.add(1, AppConfig.STATUS_COLUMN_NAME_IN_BULKV2);
        } else {
	        if (!AppConfig.ID_COLUMN_NAME.equals(headers.get(0))) {
	            headers.add(0, AppConfig.ID_COLUMN_NAME);
	        }
	        headers.add(AppConfig.STATUS_COLUMN_NAME);
        }
        try {
            getSuccessWriter().open();
            getSuccessWriter().setColumnNames(headers);
        } catch (final DataAccessObjectInitializationException e) {
            throw new LoadException(
                    getMessage("errorOpeningSuccessFile", getConfig().getString(AppConfig.PROP_OUTPUT_SUCCESS)), e);
        }
    }

}
