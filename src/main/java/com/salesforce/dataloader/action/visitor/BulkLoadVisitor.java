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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
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
import org.apache.logging.log4j.LogManager;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.LoadException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.model.NACalendarValue;
import com.salesforce.dataloader.model.NADateOnlyCalendarValue;
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

    private static final Logger logger = LogManager.getLogger(BulkLoadVisitor.class);

    private static final String SUCCESS_RESULT_COL = "Success";
    private static final String ERROR_RESULT_COL = "Error";
    private static final String ID_RESULT_COL = "Id";
    private static final String CREATED_RESULT_COL = "Created";

    private final boolean isDelete;
    private static final DateFormat DATE_FMT;
    private int batchCountForJob = 0;

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
            if (!this.jobUtil.hasJob()) this.jobUtil.createJob();
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
        int processedRecordsCount = 0;
        final List<String> userColumns = getController().getDao().getColumnNames();
        List<String> headerColumns = null;
        int maxBatchBytes = this.getConfig().isBulkV2APIEnabled() ? Config.MAX_BULKV2_API_JOB_BYTES : Config.MAX_BULK_API_BATCH_BYTES;
        for (int i = 0; i < rows.size(); i++) {
            final DynaBean row = rows.get(i);

            if (processedRecordsCount == 0) {
                headerColumns = addBatchRequestHeader(out, row, userColumns);
            }
            writeRow(row, out, processedRecordsCount, headerColumns);
            processedRecordsCount++;

            if (os.size() > maxBatchBytes) {
            	createBatch(os, processedRecordsCount); // resets outputstream
                // reset for the next batch
            	processedRecordsCount = 0;
            }
        }
        if (processedRecordsCount > 0) createBatch(os, processedRecordsCount);
        this.jobUtil.periodicCheckStatus();
    }

    private void writeRow(DynaBean row, PrintStream out, int recordsInBatch,
            List<String> header) throws LoadException {
        boolean notFirst = false;
        for (final String sfdcColumn : header) {
            if (notFirst) {
                out.print(',');
            } else {
                notFirst = true;
            }
            writeSingleColumn(out, sfdcColumn, row.get(sfdcColumn));
        }
        out.println();
    }

    private void writeSingleColumn(PrintStream out, String fieldName, Object fieldValue) throws LoadException {
        if (fieldValue != null) {
            Object col = fieldValue;
            if (fieldValue instanceof NACalendarValue || fieldValue instanceof NADateOnlyCalendarValue) {
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
            getLogger().debug(Messages.getMessage(getClass(), "noFieldVal", fieldName));
        }
    }

    private void writeColumnToCsv(PrintStream out, Object val) {
        out.print('"');
        out.print(val.toString().replace("\"", "\"\""));
        out.print('"');
    }

    private List<String> addBatchRequestHeader(PrintStream serverRequestOutput, DynaBean row, List<String> columns)
            throws LoadException {
        boolean first = true;
        final List<String> cols = new ArrayList<String>();
        final Set<String> addedCols = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (final String userColumn : columns) {
            final String sfdcColList = getMapper().getMapping(userColumn);
            // if the column is not mapped, don't send it
            if (sfdcColList == null || sfdcColList.length() == 0) {
                // TODO: we should make it more obvious to users when we omit a column
                getLogger().warn("Cannot find mapping for column: " + userColumn + ".  Omitting column");
                continue;
            }
            String[] sfdcColArray = sfdcColList.split(",");
            for (String sfdcColumn : sfdcColArray) {
                sfdcColumn = sfdcColumn.strip();
                // TODO we don't really need to be this strict about a delete CSV file.. as long as the IDS are there
                if (this.isDelete && (!first || !"id".equalsIgnoreCase(sfdcColumn)))
                    throw new LoadException(Messages.getMessage(getClass(), "deleteCsvError"));
                addFieldToBatchRequestHeader(serverRequestOutput, sfdcColumn, cols, addedCols, first);
            }
            if (first) first = false;
        }
        
        // Handle constant field mappings in the field mapping file (.sdl)
        for (DynaProperty dynaProperty : row.getDynaClass().getDynaProperties()) {
            final String name = dynaProperty.getName();
            if (row.get(name) != null && !addedCols.contains(name)) {
                addFieldToBatchRequestHeader(serverRequestOutput, name, cols, addedCols, first);
            }
        }
        serverRequestOutput.println();
        return Collections.unmodifiableList(cols);
    }

    private static void addFieldToBatchRequestHeader(PrintStream serverRequestOutput, String sfdcColumn, List<String> cols, Set<String> addedCols,
            boolean first) {
        if (!first) {
            serverRequestOutput.print(',');
        }
        serverRequestOutput.print(sfdcColumn.replace(':', '.'));
        cols.add(sfdcColumn);
        addedCols.add(sfdcColumn);
    }

    private void writeServerLoadBatchDataToCSV(ByteArrayOutputStream os) {
        String filenamePrefix = "uploadedToServer";
        String filename = generateBatchCSVFilename(filenamePrefix, batchCountForJob);
        File uploadedToServerCSVFile = new File(filename);
        final byte[] request = os.toByteArray();
        try {
            FileOutputStream outputStream = new FileOutputStream(uploadedToServerCSVFile);
            outputStream.write(request);
            outputStream.close();
        } catch (Exception ex) {
            logger.info("unable to create file " + filename);
        }
    }
    
    private void writeRawResultsToCSV(CSVReader serverResultsReader, int batchNum) {
        String filenamePrefix = "rawResultsFromServer";
        String filename = generateBatchCSVFilename(filenamePrefix, batchNum);
        File rawBatchResultsCSVFile = new File(filename);
        try {
            FileOutputStream outputStream = new FileOutputStream(rawBatchResultsCSVFile);
            PrintStream printOutput = new PrintStream(outputStream);
            List<String> row = serverResultsReader.nextRecord();
            while (row != null && !row.isEmpty()) {
                int cellIdx = 0;
                for (String cell : row) {
                    if (cellIdx != 0) {
                        printOutput.print(", ");
                    }
                    cellIdx++;
                    printOutput.print(cell);
                }
                printOutput.println("");
                row = serverResultsReader.nextRecord();
            }
            printOutput.close();
            outputStream.close();
        } catch (Exception ex) {
            logger.info("unable to create file " + filename);
        }
    }
    
    private String generateBatchCSVFilename(String prefix, int batchNum) {
        String successResultsFilename = controller.getConfig().getString(Config.OUTPUT_SUCCESS);
        int parentDirLocation = successResultsFilename.lastIndexOf(System.getProperty("file.separator"));
        String resultsDir = successResultsFilename.substring(0, parentDirLocation);
        return resultsDir 
                + System.getProperty("file.separator")
                + prefix
                + "_Batch" + batchNum + "_"
                + controller.getFormattedCurrentTimestamp() + ".csv";
    }

    private void createBatch(ByteArrayOutputStream os, int numRecords) throws AsyncApiException {
        if (numRecords <= 0) return;
        final byte[] request = os.toByteArray();
        if (controller.getConfig().getBoolean(Config.SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV)) {
            this.batchCountForJob++;
            writeServerLoadBatchDataToCSV(os);
        }
        os.reset();
        BatchInfo bi = this.jobUtil.createBatch(new ByteArrayInputStream(request, 0, request.length));
        this.allBatchesInOrder.add(new BatchData(bi.getId(), numRecords));
    }

    @Override
    public void flushRemaining() throws OperationException, DataAccessObjectException {
        super.flushRemaining();
        if (this.jobUtil.hasJob()) {
            try {
                this.jobUtil.awaitCompletionAndCloseJob();
            } catch (final AsyncApiException e) {
                logger.warn("Failed to close job", e);
            }
            try {
            	if (this.getConfig().isBulkAPIEnabled())
                getResults();
            } catch (AsyncApiException e) {
                throw new LoadException("Failed to get batch results", e);
            }
        }
    }
    
    private long transferCSVContent(String fromFileName, String toFileName) throws OperationException {
        RandomAccessFile fromFile, toFile;
        long contentRowCount = 0;
		try {
			fromFile = new RandomAccessFile(fromFileName, "rw");
			toFile = new RandomAccessFile(toFileName, "rw");
			//first line is the header row                                  
			fromFile.readLine();
			toFile.readLine();

			// go to end of to file
			while (toFile.readLine() != null) {
				contentRowCount++;
			}
			String contentRow = null;
			while ((contentRow = fromFile.readLine()) != null) {
				contentRow += System.lineSeparator();
				toFile.write(contentRow.getBytes());
				contentRowCount++;
			}
			toFile.close();
			
			// truncate the fromFile
			fromFile.setLength(0);
			fromFile.close();
			return contentRowCount;
		} catch (FileNotFoundException e) {
			throw new OperationException(e.getMessage());
		} catch (IOException e) {
			throw new OperationException(e.getMessage());
		}          
    }

    private void getBulkV2LoadJobResults() throws AsyncApiException, OperationException, DataAccessObjectException {
    	this.getSuccessWriter().close();
    	this.getErrorWriter().close();
    	
    	Config config = this.getConfig();
    	String successWriterFile = config.getString(Config.OUTPUT_SUCCESS);
    	String errorWriterFile = config.getString(Config.OUTPUT_ERROR);
    	// TODO for unprocessed records. Also uncomment in Controller.java to set the right value
    	// for Config.OUTPUT_UNPROCESSED_RECORDS
    	// String unprocessedRecordsWriterFile = config.getString(Config.OUTPUT_UNPROCESSED_RECORDS);

        File tmpFile = new File(this.jobUtil.getStagingFileInOutputStatusDir("temp", ".csv"));
        String tmpFileName = tmpFile.getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$

    	this.jobUtil.getBulkV2LoadSuccessResults(successWriterFile);
    	CSVFileReader csvReader = new CSVFileReader(new File(successWriterFile), config, true, false);
    	this.setSuccesses(csvReader.getTotalRows());

    	this.jobUtil.getBulkV2LoadErrorResults(tmpFileName);
    	// Append error results to the errors found by data loader before uploading and stored
    	// in errorWriterFile.
    	long rowCount = transferCSVContent(tmpFileName, errorWriterFile);
    	this.setErrors(rowCount);

        // TODO for unprocessed records
    	// this.jobUtil.getBulkV2LoadUnprocessedRecords(tmpFileName);
    	// transferCSVContent(tmpFileName, unprocessedRecordsWriterFile);

    	tmpFile.delete();
    }

    private void getResults() throws AsyncApiException, OperationException, DataAccessObjectException {

        getProgressMonitor().setSubTask(Messages.getMessage(getClass(), "retrievingResults"));

        if (this.getConfig().isBulkV2APIEnabled()) {
        	getBulkV2LoadJobResults();
        	return;
        }
        DataReader dataReader = null;
        if (!controller.getConfig().getBoolean(Config.PROCESS_BULK_CACHE_DATA_FROM_DAO)) {
            dataReader = resetDAO();
        }
        // create a map of batch infos by batch id. Each batchinfo has the final processing state of the batch
        final Map<String, BatchInfo> batchInfoMap = createBatchInfoMap();

        // go through all the batches we sent to sfdc in the same order and process the batch results for
        // each one by looking them up in batchInfoMap
        this.batchCountForJob = 0;
        int uploadedRowCount = 0;
        for (final BatchData clientBatchInfo : this.allBatchesInOrder) {
            processResults(dataReader, 
                    batchInfoMap.get(clientBatchInfo.batchId), clientBatchInfo, uploadedRowCount);
            uploadedRowCount += clientBatchInfo.numRows;
        }
    }
    

    private int firstDAORowForCurrentBatch = 0;

    private void processResults(final DataReader dataReader, final BatchInfo batch, 
            BatchData clientBatchInfo, final int firstRowInBatch)
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

        int lastRowInCurrentBatch = firstRowInBatch + clientBatchInfo.numRows - 1;
        int lastDAORowForCurrentBatch = this.batchRowToDAORowList.get(lastRowInCurrentBatch);
        
        final int totalRowsInDAOInCurrentBatch = lastDAORowForCurrentBatch - this.firstDAORowForCurrentBatch + 1;
        List<Row> rows;
        if (controller.getConfig().getBoolean(Config.PROCESS_BULK_CACHE_DATA_FROM_DAO)) {
            rows = new ArrayList<Row>();
            for (int i=0; i<totalRowsInDAOInCurrentBatch; i++) {
                rows.add(i, this.daoRowList.get(i + this.firstDAORowForCurrentBatch));
            }
        } else {
            rows = dataReader.readRowList(totalRowsInDAOInCurrentBatch);
        }
        if (batch.getState() == BatchStateEnum.Completed || batch.getNumberRecordsProcessed() > 0) {
            try {
                processBatchResults(batch, errorMessage, batch.getState(), rows, this.firstDAORowForCurrentBatch);
            } catch (IOException e) {
                throw new LoadException("IOException while reading batch results", e);
            }
        } else {
            for (final Row row : rows) {
                writeError(row, errorMessage);
            }
        }
        // update to process the next batch
        this.firstDAORowForCurrentBatch = lastDAORowForCurrentBatch + 1;
    }

    private void processBatchResults(final BatchInfo batch, final String errorMessage, 
            final BatchStateEnum state, final List<Row> rows, final int firstDataReaderRowInBatch) throws DataAccessObjectException, IOException, AsyncApiException {

        // get the batch csv result stream from sfdc
        final CSVReader resultRdr = this.jobUtil.getBatchResults(batch.getId());

        if (controller.getConfig().getBoolean(Config.SAVE_BULK_SERVER_LOAD_AND_RAW_RESULTS_IN_CSV)) {
            this.batchCountForJob++;
            writeRawResultsToCSV(this.jobUtil.getBatchResults(batch.getId()), this.batchCountForJob);
        }
        // read in the result csv header and note the column indices
        Map<String, Integer> hdrIndices = mapHeaderIndices(resultRdr.nextRecord());
        final int successIdx = hdrIndices.get(SUCCESS_RESULT_COL);
        final int createdIdx = isDelete ? -1 : hdrIndices.get(CREATED_RESULT_COL);
        final int idIdx = hdrIndices.get(ID_RESULT_COL);
        final int errIdx = hdrIndices.get(ERROR_RESULT_COL);
        hdrIndices = null;
        int dataReaderRowCount = 0;
        int skippedRowsCount = 0;
        try {
            skippedRowsCount = controller.getConfig().getInt(Config.LOAD_ROW_TO_START_AT);
        } catch (ParameterLoadException e) {
            // @ignored
        }

        for (final Row row : rows) {
            boolean conversionSuccessOfRow = isRowConversionSuccessful(skippedRowsCount 
                        + this.firstDAORowForCurrentBatch + dataReaderRowCount++);
            if (!conversionSuccessOfRow && !controller.getConfig().getBoolean(Config.PROCESS_BULK_CACHE_DATA_FROM_DAO)) {
                continue; // this DAO row failed to convert and was not part of the batch sent to the server. Go to the next one
            }
            final List<String> res = resultRdr.nextRecord();

            // no result for this column. In this case it failed, and we should use the batch state message
            if (state == BatchStateEnum.Failed || errorMessage != null) {
                getLogger().warn(
                        Messages.getMessage(getClass(), "logBatchInfoWithMessage", batch.getId(), state, errorMessage));
                writeError(row, errorMessage);
            } else if (res == null || res.isEmpty()) {
                String msg = Messages.getMessage(getClass(), "noResultForRow", row.toString(), batch.getId());
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
    }

    @Override
    public Map<String, InputStream> getAttachments() {
        return this.jobUtil.getAttachments();
    }
}
