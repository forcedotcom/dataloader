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

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.salesforce.dataloader.action.AbstractExtractAction;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.ExtractException;
import com.salesforce.dataloader.exception.ExtractExceptionOnServer;
import com.salesforce.dataloader.exception.OperationException;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.QueryResultList;

/**
 * Query visitor for bulk api extract operations.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public class BulkV1QueryVisitor extends AbstractBulkQueryVisitor {

    private BatchInfo[] batches;

    public BulkV1QueryVisitor(AbstractExtractAction action, Controller controller, ILoaderProgress monitor, DataWriter queryWriter,
            DataWriter successWriter, DataWriter errorWriter) {
        super(action, controller, monitor, queryWriter, successWriter, errorWriter);
    }

    @Override
    protected int executeQuery(String soql) throws AsyncApiException, OperationException {
        final BulkApiVisitorUtil jobUtil = new BulkApiVisitorUtil(getController(), getProgressMonitor(),
                getRateCalculator(), false);
        jobUtil.createJob();
        try {
            jobUtil.createBatch(new ByteArrayInputStream(soql.getBytes(AppConfig.BULK_API_ENCODING)));
        } catch (final UnsupportedEncodingException e) {
            throw new ExtractException(e);
        }
        jobUtil.awaitCompletionAndCloseJob();
        if (!this.getConfig().isBulkV2APIEnabled()) {
            this.batches = jobUtil.getBatches().getBatchInfo();
        }
        return jobUtil.getRecordsProcessed();
    }

    @Override
    protected void writeExtraction() throws AsyncApiException, ExtractException, DataAccessObjectException {
        for (BatchInfo b : this.batches) {
            writeExtractionForBatch(b);
        }
    }
    private void writeExtractionForBatch(BatchInfo batch) throws AsyncApiException, ExtractException, DataAccessObjectException {
        if (batch.getState() == BatchStateEnum.Failed)
            throw new ExtractExceptionOnServer("Batch failed: " + batch.getStateMessage());
        final QueryResultList results = getController().getBulkV1Client().getConnection()
                .getQueryResultList(batch.getJobId(), batch.getId());

        for (final String resultId : results.getResult()) {
            if (getProgressMonitor().isCanceled()) return;
            try {
                final InputStream serverResultStream = getController().getBulkV1Client().getConnection()
                        .getQueryResultStream(batch.getJobId(), batch.getId(), resultId);
                writeExtractionForServerStream(serverResultStream);
            }  catch (final IOException e) {
                throw new ExtractExceptionOnServer(e);
            }
        }
    }
}
