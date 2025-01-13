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
package com.salesforce.dataloader.action.visitor.bulk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriterInterface;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.exception.BatchSizeLimitException;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.LoadException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.util.DLLogManager;
import com.salesforce.dataloader.util.LoadRateCalculator;
import com.sforce.async.AsyncApiException;

public class BulkV2LoadVisitor extends BulkLoadVisitor {
    private static final Logger logger = DLLogManager.getLogger(BulkV2LoadVisitor.class);
    private boolean gotUploadResultsFromServer = false;
    private boolean isFirstJob;
    
    public BulkV2LoadVisitor(Controller controller, ILoaderProgress monitor, DataWriterInterface successWriter,
            DataWriterInterface errorWriter, LoadRateCalculator rateCalculator, boolean isFirstJob) {
        super(controller, monitor, successWriter, errorWriter, rateCalculator);
        this.isFirstJob = isFirstJob;
    }
    
    protected void doOneBatch(PrintStream out, ByteArrayOutputStream os, List<DynaBean> rows) throws OperationException, FileNotFoundException, AsyncApiException, BatchSizeLimitException {
        super.doOneBatch(out, os, rows);
        try {
            closeJob();
        } catch (DataAccessObjectException e) {
            throw new LoadException("Failed to get batch results", e);
        }
    }
    
    protected void getResults() throws AsyncApiException, OperationException, DataAccessObjectException {
        if (gotUploadResultsFromServer) {
            // Bulk v2 job has only one batch.
            return;
        }
        this.getSuccessWriter().close();
        this.getErrorWriter().close();
        
        AppConfig appConfig = this.getConfig();
        String successWriterFile = appConfig.getString(AppConfig.PROP_OUTPUT_SUCCESS);
        String errorWriterFile = appConfig.getString(AppConfig.PROP_OUTPUT_ERROR);
        // TODO for unprocessed records. Also uncomment in Controller.java to set the right value
        // for Config.OUTPUT_UNPROCESSED_RECORDS
        // String unprocessedRecordsWriterFile = config.getString(Config.OUTPUT_UNPROCESSED_RECORDS);

        this.getVisitorUtil().getBulkV2LoadSuccessResults(successWriterFile, !this.isFirstJob);
        CSVFileReader csvReader = new CSVFileReader(new File(successWriterFile), appConfig, true, false);
        this.setSuccesses(csvReader.getTotalRows());
        this.getLoadRateCalculator().setNumSuccessesAcrossCompletedJobs(csvReader.getTotalRows());
        csvReader.close();
        
        this.getVisitorUtil().getBulkV2LoadErrorResults(errorWriterFile);
        csvReader = new CSVFileReader(new File(errorWriterFile), appConfig, true, false);
        this.setErrors(csvReader.getTotalRows());
        this.getLoadRateCalculator().setNumErrorsAcrossCompletedJobs(csvReader.getTotalRows());
        csvReader.close();
        gotUploadResultsFromServer = true;
    }
}
