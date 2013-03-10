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

import java.io.*;
import java.util.List;
import java.util.Map;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.model.Row;
import com.sforce.async.*;

/**
 * Query visitor for bulk api extract operations.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public class BulkQueryVisitor extends AbstractQueryVisitor {

    private BatchInfo batch;

    public BulkQueryVisitor(Controller controller, ILoaderProgress monitor, DataWriter queryWriter,
            DataWriter successWriter, DataWriter errorWriter) {
        super(controller, monitor, queryWriter, successWriter, errorWriter);
    }

    @Override
    protected int executeQuery(String soql) throws AsyncApiException, OperationException {
        final BulkApiVisitorUtil jobUtil = new BulkApiVisitorUtil(getController(), getProgressMonitor(),
                getRateCalculator(), false);
        jobUtil.createJob(getConfig());
        try {
            jobUtil.createBatch(new ByteArrayInputStream(soql.getBytes(BulkApiVisitorUtil.ENCODING)));
        } catch (final UnsupportedEncodingException e) {
            throw new ExtractException(e);
        }
        jobUtil.closeJob();
        final BatchInfo b = jobUtil.getBatches().getBatchInfo()[0];
        if (b.getState() == BatchStateEnum.Failed) throw new ExtractException("Batch failed: " + b.getStateMessage());
        this.batch = b;
        return b.getNumberRecordsProcessed();
    }

    @Override
    protected void writeExtraction() throws AsyncApiException, ExtractException, DataAccessObjectException {
        if (this.batch.getState() == BatchStateEnum.Failed)
            throw new ExtractException("Batch failed: " + this.batch.getStateMessage());
        final QueryResultList results = getController().getBulkClient().getClient()
                .getQueryResultList(this.batch.getJobId(), this.batch.getId());

        for (final String resultId : results.getResult()) {
            if (getProgressMonitor().isCanceled()) return;
            try {
                final InputStream resultStream = getController().getBulkClient().getClient()
                        .getQueryResultStream(this.batch.getJobId(), this.batch.getId(), resultId);
                try {
                    final CSVReader rdr = new CSVReader(resultStream, getConfig().getCsvWriteEncoding());
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

    private Row getDaoRow(List<String> headers, List<String> csvRow, StringBuilder id) {
        return getMapper().mapCsvRowSfdcToLocal(headers, csvRow, id);
    }

}
