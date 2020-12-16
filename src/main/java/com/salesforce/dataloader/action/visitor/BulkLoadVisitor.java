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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.LoadException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.model.NACalendarValue;
import com.salesforce.dataloader.model.NATextValue;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.util.DAORowUtil;
import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.CSVReader;

/**
 * Visitor for operations using the bulk API client
 * 
 * @author Jesper Joergensen, Colin Jarvis
 * @since 17.0
 */
public class BulkLoadVisitor extends DAOLoadVisitor {

    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(BulkLoadVisitor.class);

    private static final String SUCCESS_RESULT_COL = "Success";
    private static final String ERROR_RESULT_COL = "Error";
    private static final String ID_RESULT_COL = "Id";
    private static final String CREATED_RESULT_COL = "Created";
    private static final String SKIP_BATCH_ID = "SKIP";

    private final boolean isDelete;
    private static final DateFormat DATE_FMT;

    static {
        DATE_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DATE_FMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    private final BulkApiVisitorUtil jobUtil;

    // This keeps track of all the batches we send in order so that we know whats what when processsing results
    private final List<BatchData> allBatchesInOrder = new ArrayList<BatchData>();

    /** DataLoader uses this to help match batch results from SFDC to the rows in our input */
    private class BatchData {
        final String batchId;
        final int numRows;

        BatchData(String batchId, int numRows) {
            this.batchId = batchId;
            this.numRows = numRows;
        }
    }

    /** When we get batch CSV results back from sfdc they are converted into instances of RowResult */
    private static class RowResult {
        RowResult(boolean success, boolean created, String id, String error) {
            this.success = success;
            this.created = created;
            this.id = id;
            this.error = error;
        }

        final boolean success;
        final boolean created;
        final String id;
        final String error;
    }

    public BulkLoadVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);
        this.isDelete = getController().getConfig().getOperationInfo().isDelete();
        this.jobUtil = new BulkApiVisitorUtil(getController(), getProgressMonitor(), getRateCalculator());
    }

    @Override
    protected void loadBatch() throws DataAccessObjectException, OperationException {
        try {
            if (!this.jobUtil.hasJob()) this.jobUtil.createJob(getConfig());
            createBatches();
            clearArrays();
        } catch (final AsyncApiException e) {
            handleException(e);
        } catch (final IOException e) {
            handleException(e);
        }
    }

    /**
     * Throws a load exception
     */
    @Override
    protected void handleException(Throwable t) throws LoadException {
        handleException(getOverrideMessage(t), t);
    }

    private String getOverrideMessage(Throwable t) {
        if (t instanceof AsyncApiException) {

            final AsyncApiException aae = (AsyncApiException)t;
            final String hardDeleteNoPermsMessage = "hardDelete operation requires special user profile permission, please contact your system administrator";

            if (aae.getExceptionCode() == AsyncExceptionCode.FeatureNotEnabled
                    && aae.getExceptionMessage().contains(hardDeleteNoPermsMessage))
                return Messages.getMessage(getClass(), "hardDeleteNoPerm");
        }
        return null;
    }

    private void createBatches() throws OperationException, IOException, AsyncApiException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(os, true, Config.BULK_API_ENCODING);
        doOneBatch(out, os, this.dynaArray);
    }

    private void doOneBatch(PrintStream out, ByteArrayOutputStream os, List<DynaBean> rows) throws OperationException,
            AsyncApiException {
        int recordsInBatch = 0;
        final List<String> userColumns = getController().getDao().getColumnNames();
        List<String> headerColumns = null;
        for (int i = 0; i < rows.size(); i++) {
            final DynaBean row = rows.get(i);

            if (recordsInBatch == 0) {
                headerColumns = addHeader(out, os, row, userColumns);
            }
            writeRow(row, out, os, recordsInBatch, headerColumns);
            recordsInBatch++;

            if (os.size() > Config.MAX_BULK_API_BATCH_BYTES) {
                createBatch(os, recordsInBatch); // resets outputstream
                recordsInBatch = 0;
            }
        }
        if (recordsInBatch > 0) createBatch(os, recordsInBatch);
        this.jobUtil.periodicCheckStatus();
    }

    private void writeRow(DynaBean row, PrintStream out, ByteArrayOutputStream os, int recordsInBatch,
            List<String> header) throws LoadException {
        boolean notFirst = false;
        for (final String column : header) {
            if (notFirst) {
                out.print(',');
            } else {
                notFirst = true;
            }
            writeSingleColumn(out, column, row.get(column));
        }
        out.println();
    }

    private void writeSingleColumn(PrintStream out, String fieldName, Object fieldValue) throws LoadException {
        if (fieldValue != null) {
            Object col = fieldValue;
            if (fieldValue instanceof NACalendarValue) {
                col = fieldValue.toString();
            } else if (fieldValue instanceof Calendar) {
                col = DATE_FMT.format(((Calendar) fieldValue).getTime());
            } else if (fieldValue instanceof byte[]) {
                if (!getController().attachmentsEnabled())
                    throw new LoadException(Messages.getMessage("FinishPage", "cannotMapBase64ForBulkApi", fieldName));
                col = this.jobUtil.addAttachment((byte[])fieldValue);
            }
            writeColumnToCsv(out, col);
        } else {
            // all null values should be ignored when using bulk API
            getLogger().warn(Messages.getMessage(getClass(), "noFieldVal", fieldName));
        }
    }

    private void writeColumnToCsv(PrintStream out, Object val) {
        out.print('"');
        out.print(val.toString().replace("\"", "\"\""));
        out.print('"');
    }

    private List<String> addHeader(PrintStream out, ByteArrayOutputStream os, DynaBean row, List<String> columns)
            throws LoadException {
        boolean first = true;
        final List<String> cols = new ArrayList<String>();
        final Set<String> addedCols = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (final String userColumn : columns) {
            final String sfdcColumn = getMapper().getMapping(userColumn);
            // if the column is not mapped, don't send it
            if (sfdcColumn == null || sfdcColumn.length() == 0) {
                // TODO: we should make it more obvious to users when we omit a column
                getLogger().warn("Cannot find mapping for column: " + userColumn + ".  Omitting column");
                continue;
            }
            // TODO we don't really need to be this strict about a delete CSV file.. as long as the IDS are there
            if (this.isDelete && (!first || !"id".equalsIgnoreCase(sfdcColumn)))
                throw new LoadException(Messages.getMessage(getClass(), "deleteCsvError"));
            addFieldToHeader(out, sfdcColumn, cols, addedCols, first);
            if (first) first = false;
        }
        for (DynaProperty dynaProperty : row.getDynaClass().getDynaProperties()) {
            final String name = dynaProperty.getName();
            if (row.get(name) != null && !addedCols.contains(name)) {
                addFieldToHeader(out, name, cols, addedCols, first);
            }
        }
        out.println();
        return Collections.unmodifiableList(cols);
    }

    private static void addFieldToHeader(PrintStream out, String sfdcColumn, List<String> cols, Set<String> addedCols,
            boolean first) {
        if (!first) {
            out.print(',');
        }
        out.print(sfdcColumn.replace(':', '.'));
        cols.add(sfdcColumn);
        addedCols.add(sfdcColumn);
    }

    private void createBatch(ByteArrayOutputStream os, int numRecords) throws AsyncApiException {
        if (numRecords <= 0) return;
        final byte[] request = os.toByteArray();
        os.reset();
        BatchInfo bi = this.jobUtil.createBatch(new ByteArrayInputStream(request, 0, request.length));
        this.allBatchesInOrder.add(new BatchData(bi.getId(), numRecords));
    }

    @Override
    public void flushRemaining() throws OperationException, DataAccessObjectException {
        super.flushRemaining();
        if (this.jobUtil.hasJob()) {
            try {
                this.jobUtil.closeJob();
            } catch (final AsyncApiException e) {
                logger.warn("Failed to close job", e);
            }
            try {
                getResults();
            } catch (AsyncApiException e) {
                throw new LoadException("Failed to get batch results", e);
            }
        }
    }

    private void getResults() throws AsyncApiException, OperationException, DataAccessObjectException {

        getProgressMonitor().setSubTask(Messages.getMessage(getClass(), "retrievingResults"));

        final DataReader dataReader = resetDAO();

        // create a map of batch infos by batch id. Each batchinfo has the final processing state of the batch
        final Map<String, BatchInfo> batchInfoMap = createBatchInfoMap();

        // go through all the batches we sent to sfdc in the same order and process the batch results for
        // each one by looking them up in batchInfoMap
        for (final BatchData clientBatchInfo : this.allBatchesInOrder) {
            if (clientBatchInfo.batchId == SKIP_BATCH_ID) {
                skipDataRows(dataReader, clientBatchInfo.numRows);
            } else {
                processResults(dataReader, batchInfoMap.get(clientBatchInfo.batchId), clientBatchInfo);
            }
        }
    }

    private void processResults(final DataReader dataReader, final BatchInfo batch, BatchData clientBatchInfo)
            throws LoadException, DataAccessObjectException, AsyncApiException {
        // For Bulk API, we don't save any success or error until the end,
        // so we have to go through the original CSV from the beginning while
        // we go through the results from the server.
        // TODO we should save the ACTUAL rows/batches sent to the server

        // do some basic checks to make sure we are matching up the batches correctly
        sanityCheckBatch(clientBatchInfo, batch);

        // If there was an error processing the batch and the state isn't 'Completed' get stateMessage from batch 
        final String stateMessage = (batch.getState() == BatchStateEnum.Completed) ? null : batch.getStateMessage();
        final String errorMessage = stateMessage == null ? null : Messages.getMessage(getClass(), "batchError",
                stateMessage);

        final List<Row> rows = dataReader.readRowList(clientBatchInfo.numRows);
        if (batch.getState() == BatchStateEnum.Completed || batch.getNumberRecordsProcessed() > 0) {
            try {
                processBatchResults(batch, errorMessage, batch.getState(), rows);
            } catch (IOException e) {
                throw new LoadException("IOException while reading batch results", e);
            }
        } else {
            for (final Row row : rows) {
                writeError(row, errorMessage);
            }
        }
    }

    private void skipDataRows(DataReader dataReader, int numRows) throws DataAccessObjectException {
        List<Row> skippedRows = dataReader.readRowList(numRows);
        assert skippedRows.size() == numRows;
    }

    private void processBatchResults(final BatchInfo batch, final String errorMessage, final BatchStateEnum state,
            final List<Row> rows) throws DataAccessObjectException, IOException, AsyncApiException {

        // get the batch csv result stream from sfdc
        final CSVReader resultRdr = this.jobUtil.getBatchResults(batch.getId());

        // read in the result csv header and note the column indices
        Map<String, Integer> hdrIndices = mapHeaderIndices(resultRdr.nextRecord());
        final int successIdx = hdrIndices.get(SUCCESS_RESULT_COL);
        final int createdIdx = isDelete ? -1 : hdrIndices.get(CREATED_RESULT_COL);
        final int idIdx = hdrIndices.get(ID_RESULT_COL);
        final int errIdx = hdrIndices.get(ERROR_RESULT_COL);
        hdrIndices = null;

        for (final Row row : rows) {
            final List<String> res = resultRdr.nextRecord();

            // no result for this column. In this case it failed, and we should use the batch state message
            if (state == BatchStateEnum.Failed || errorMessage != null) {
                getLogger().warn(
                        Messages.getMessage(getClass(), "logBatchInfoWithMessage", batch.getId(), state, errorMessage));
                writeError(row, errorMessage);
            } else if (res == null || res.isEmpty()) {
                String msg = Messages.getMessage(getClass(), "noResultForRow", batch.getId(), state);
                writeError(row, msg);
                getLogger().warn(msg);
            } else {
                // convert the row into a RowResults so its easy to inspect
                final RowResult rowResult = new RowResult(Boolean.valueOf(res.get(successIdx)), isDelete ? false
                        : Boolean.valueOf(res.get(createdIdx)), res.get(idIdx), res.get(errIdx));
                writeRowResult(row, rowResult);
            }
        }
    }

    // returns a map of batchinfos indexed by batch id
    private Map<String, BatchInfo> createBatchInfoMap() throws AsyncApiException {
        Map<String, BatchInfo> batchInfoMap = new HashMap<String, BatchInfo>();
        for (BatchInfo bi : this.jobUtil.getBatches().getBatchInfo()) {
            batchInfoMap.put(bi.getId(), bi);
        }
        return batchInfoMap;
    }

    private DataReader resetDAO() throws DataAccessObjectInitializationException, LoadException {
        final DataReader dataReader = (DataReader)getController().getDao();
        dataReader.close();
        // TODO: doing this causes sql to be executed twice, for sql we should cache results in a local file
        dataReader.open();
        // when re-opening the dao we need to start at the same row in the input
        DAORowUtil.get().skipRowToStartOffset(getConfig(), dataReader, getProgressMonitor(), true);
        return dataReader;
    }

    private void writeRowResult(Row row, RowResult resultRow) throws DataAccessObjectException {
        if (resultRow.success) {
            String successMessage;
            switch (getConfig().getOperationInfo()) {
            case hard_delete:
                successMessage = "statusItemHardDeleted";//$NON-NLS-1$
                break;
            case delete:
                successMessage = "statusItemDeleted";//$NON-NLS-1$
                break;
            default:
                successMessage = resultRow.created ? "statusItemCreated"//$NON-NLS-1$
                        : "statusItemUpdated"; //$NON-NLS-1$
            }
            writeSuccess(row, resultRow.id, Messages.getMessage(getClass(), successMessage));
        } else {
            writeError(row, parseAsyncApiError(resultRow.error));
        }
    }

    // creates a map from the header strings in the result csv to the Integer index in the list
    private Map<String, Integer> mapHeaderIndices(final List<String> header) {
        final Map<String, Integer> indices = new HashMap<String, Integer>();
        for (int i = 0; i < header.size(); i++)
            indices.put(header.get(i), i);
        return indices;
    }

    private void sanityCheckBatch(BatchData clientBatchInfo, BatchInfo batch) throws LoadException {
        final String batchId = clientBatchInfo.batchId;

        assert (batchId != null && batchId.equals(batch.getId()));
        assert (jobUtil.getJobId().equals(batch.getJobId()));
        assert clientBatchInfo.numRows > 0;

        final int recordsProcessed = batch.getNumberRecordsProcessed();
        final BatchStateEnum state = batch.getState();

        if (state != BatchStateEnum.Completed && state != BatchStateEnum.Failed)
            sanityCheckError(batchId, "Expected batch state to be Completed or Failed, but was " + state);
    }

    private void sanityCheckError(String id, String errMsg) throws LoadException {
        throw new LoadException(id + ": " + errMsg);
    }

    private String parseAsyncApiError(final String errString) {
        final String sep = ":";
        final String suffix = "--";
        final int lastSep = errString.lastIndexOf(sep);
        if (lastSep > 0 && errString.endsWith(suffix)) {
            final String fields = errString.substring(lastSep + 1, errString.length() - suffix.length());
            final String start = errString.substring(0, lastSep);
            if (fields != null && fields.length() > 0)
                return new StringBuilder(start).append("\n").append("Error fields: ").append(fields).toString();
            return start;
        }
        return errString;
    }

    @Override
    protected void convertBulkAPINulls(Row row) {
        for (final Map.Entry<String, Object> entry : row.entrySet()) {
            if (NATextValue.isNA(entry.getValue())) {
                entry.setValue(NATextValue.getInstance());
            }
        }
    }

    @Override
    protected void conversionFailed(Row row, String errMsg) throws DataAccessObjectException,
            OperationException {
        super.conversionFailed(row, errMsg);
        getLogger().warn("Skipping results for row " + row + " which failed before upload to Saleforce.com");
        allBatchesInOrder.add(new BatchData(SKIP_BATCH_ID, 1));
    }
}
