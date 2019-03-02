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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.ExtractException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.model.Row;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.JobInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.CSVReader;
import com.sforce.async.QueryResultList;

/**
 * Query visitor for bulk api extract operations.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public class BulkQueryVisitor extends AbstractQueryVisitor {

    private BatchInfo batch;
    private BulkApiVisitorUtil jobUtil;

    public BulkQueryVisitor(Controller controller, ILoaderProgress monitor, DataWriter queryWriter,
            DataWriter successWriter, DataWriter errorWriter) {
        super(controller, monitor, queryWriter, successWriter, errorWriter);
    }

    @Override
    protected int executeQuery(String soql) throws AsyncApiException, OperationException {
        jobUtil = new BulkApiVisitorUtil(getController(), getProgressMonitor(),
                getRateCalculator(), false);
        jobUtil.createJob(getConfig());
        try {
            jobUtil.createBatch(new ByteArrayInputStream(soql.getBytes(Config.BULK_API_ENCODING)));
        } catch (final UnsupportedEncodingException e) {
            throw new ExtractException(e);
        }
        jobUtil.closeJob();
        final BatchInfo b = jobUtil.getBatches().getBatchInfo()[0];
        final JobInfo j = jobUtil.getJobInfo();
        if (b.getState() == BatchStateEnum.Failed) throw new ExtractException("Batch failed: " + b.getStateMessage());
        this.batch = b;
        return j.getNumberRecordsProcessed();
    }

    @Override
    protected void writeExtraction() throws AsyncApiException, ExtractException, DataAccessObjectException {
        for(BatchInfo b : jobUtil.getBatches().getBatchInfo()){
            if (b.getState() == BatchStateEnum.Failed)
            throw new ExtractException("Batch failed: " + this.batch.getStateMessage());
            if (b.getState() == BatchStateEnum.NotProcessed) continue;
            final QueryResultList results = getController().getBulkClient().getClient()
                .getQueryResultList(b.getJobId(), b.getId());
             getLogger().info(results);
            for (String resultId : results.getResult()) {
                if (getProgressMonitor().isCanceled()) return;

                try {
                    InputStream resultStream = getController().getBulkClient().getClient()
                            .getQueryResultStream(b.getJobId(), b.getId(), resultId);
                    try {
                        CSVReader rdr = new CSVReader(resultStream, Config.BULK_API_ENCODING);
                        rdr.setMaxCharsInFile(Integer.MAX_VALUE);
                        rdr.setMaxRowsInFile(Integer.MAX_VALUE);
                        List<String> headers;
                        headers = rdr.nextRecord();
                        List<String> csvRow;
                        while ((csvRow = rdr.nextRecord()) != null) {
                            final StringBuilder id = new StringBuilder();
                            final Row daoRow = getDaoRow(headers, csvRow, id);
                            addResultRow(daoRow, id.toString());
                        }
                    } finally {
                        resultStream.close();
                    }
                } catch (final IOException e) {
                    throw new ExtractException(e);
                }
            }
        }
        
    }

    private Row getDaoRow(List<String> headers, List<String> csvRow, StringBuilder id) {
        return getMapper().mapCsvRowSfdcToLocal(headers, csvRow, id);
    }

}
