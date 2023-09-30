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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.salesforce.dataloader.action.AbstractExtractAction;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.mapping.SOQLMapper;
import com.salesforce.dataloader.model.Row;
import com.sforce.async.CSVReader;

/**
 * Query visitor for bulk api extract operations.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
abstract public class AbstractBulkQueryVisitor extends AbstractQueryVisitor {

    public AbstractBulkQueryVisitor(AbstractExtractAction action, Controller controller, ILoaderProgress monitor, DataWriter queryWriter,
            DataWriter successWriter, DataWriter errorWriter) {
        super(action, controller, monitor, queryWriter, successWriter, errorWriter);
    }
    
    protected void writeExtractionForServerStream(InputStream serverResultStream) throws IOException, DataAccessObjectException {
        File bufferingFile = null;
        final boolean bufferResults = getConfig().getBoolean(Config.BUFFER_UNPROCESSED_BULK_QUERY_RESULTS);
        OutputStream bufferingFileWriter = null;

        InputStream resultStream = serverResultStream; //read directly from server by default
        try {
            if (bufferResults) {
                //temp csv
                bufferingFile = File.createTempFile("sdl", ".csv");
                String bufferingFilePath = bufferingFile.getAbsolutePath();
                getLogger().info("Downloading result chunk to " + bufferingFilePath);
                
                //download results into the buffering file
                int bytesRead;
                byte[] buffer = new byte[8 * 1024];
                bufferingFileWriter = new FileOutputStream(bufferingFile);
                while ((bytesRead = resultStream.read(buffer)) != -1) {
                    bufferingFileWriter.write(buffer, 0, bytesRead);
                }
                bufferingFileWriter.close();
                serverResultStream.close();
                
                //stream from csv file
                resultStream = new FileInputStream(new File(bufferingFilePath));
            }
            try {
                final CSVReader rdr = new CSVReader(resultStream, Config.BULK_API_ENCODING);
                rdr.setMaxCharsInFile(Integer.MAX_VALUE);
                rdr.setMaxRowsInFile(Integer.MAX_VALUE);
                List<String> headers;
                headers = rdr.nextRecord();
                List<String> csvRow;
                boolean isFirstRowInBatch = true;
                while ((csvRow = rdr.nextRecord()) != null) {
                    final StringBuilder id = new StringBuilder();
                    final Row daoRow = getDaoRow(headers, csvRow, id, isFirstRowInBatch);
                    addResultRow(daoRow, id.toString());
                    isFirstRowInBatch = false;
                }
            } finally {
                resultStream.close();
            }
        } finally {
            if (bufferingFile != null) {
                bufferingFile.delete();
            }
        }
    }

    private Row getDaoRow(List<String> queryResultHeaders, List<String> csvRow, 
            StringBuilder id, boolean isFirstRowInBatch) throws DataAccessObjectInitializationException {
        if (isFirstRowInBatch 
            && !getConfig().getBoolean(Config.LIMIT_OUTPUT_TO_QUERY_FIELDS)) {
            SOQLMapper mapper = (SOQLMapper)this.controller.getMapper();
            mapper.initSoqlMappingFromResultFields(queryResultHeaders);
            final List<String> daoColumns = mapper.getDaoColumnsForSoql();
            if (getConfig().getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT)) {
                try {
                    if (this.getErrorWriter() == null) {
                        this.setErrorWriter(this.action.createErrorWriter());
                        this.action.openErrorWriter(daoColumns);
                    }
                    if (this.getSuccessWriter() == null) {
                        this.setSuccessWriter(this.action.createSuccesWriter());
                        this.action.openSuccessWriter(daoColumns);
                    }
                } catch (OperationException | DataAccessObjectInitializationException e) {
                    throw new DataAccessObjectInitializationException(e);
                }
            }
        }
        return getMapper().mapCsvRowSfdcToLocal(queryResultHeaders, csvRow, id);

    }

}
