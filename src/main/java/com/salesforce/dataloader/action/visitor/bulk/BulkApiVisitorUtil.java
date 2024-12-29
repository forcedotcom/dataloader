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
import java.io.File;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ExtractException;
import com.salesforce.dataloader.exception.ExtractExceptionOnServer;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.DLLogManager;
import com.salesforce.dataloader.util.LoadRateCalculator;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchInfoList;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;

public class BulkApiVisitorUtil {

    private static final Logger logger = DLLogManager.getLogger(BulkApiVisitorUtil.class);

    private final BulkConnection connection;

    private JobInfo jobInfo = null;
    private int recordsProcessed;

    private final Map<String, InputStream> attachments = new HashMap<String, InputStream>();
    private int attachmentNum;

    private final long checkStatusInterval;
    private long lastStatusUpdate;

    private final ILoaderProgress monitor;
    private final LoadRateCalculator rateCalc;

    private final boolean updateProgress;
    
    private boolean enablePKchunking = false;
    private int queryChunkSize;
    private String queryChunkStartRow = "";
    private AppConfig appConfig = null;
    private Controller controller;
    private int bulkV2LoadBatchCount = 0;

    BulkApiVisitorUtil(Controller ctl, ILoaderProgress monitor, LoadRateCalculator rateCalc, boolean updateProgress) {
        this.appConfig = ctl.getAppConfig();
        this.controller = ctl;
        if (isBulkV2QueryJob() || isBulkV2LoadJob()) {
            this.connection = ctl.getBulkV2Client().getConnection();
        } else {
            this.connection = ctl.getBulkV1Client().getConnection();
        }

        try {
            // getLong will return 0 if no value is provided
            long checkStatusInt = ctl.getAppConfig().getLong(AppConfig.PROP_BULK_API_CHECK_STATUS_INTERVAL);
            this.checkStatusInterval = checkStatusInt > 0 ? checkStatusInt
                    : AppConfig.DEFAULT_BULK_API_CHECK_STATUS_INTERVAL;
        } catch (ParameterLoadException e) {
            throw new RuntimeException("Failed to initialize check status interval", e);
        }
        
        /*
         * ======== Start code block to support PK chunking
         *
        this.enablePKchunking = ctl.getConfig().getBoolean(Config.ENABLE_BULK_QUERY_PK_CHUNKING);
        if (this.enablePKchunking) {
            try {
                int chunkSize = ctl.getConfig().getInt(Config.BULK_QUERY_PK_CHUNK_SIZE);
                if (chunkSize < 1 || chunkSize > Config.MAX_BULK_QUERY_PK_CHUNK_SIZE) {
                    chunkSize = Config.DEFAULT_BULK_QUERY_PK_CHUNK_SIZE;
                }
                this.queryChunkSize = chunkSize;
            } catch (ParameterLoadException e) {
                throw new RuntimeException("Failed to initialize bulk query chunk size", e);
            }
            queryChunkStartRow = ctl.getConfig().getString(Config.BULK_QUERY_PK_CHUNK_START_ROW);
            if (queryChunkStartRow == null) {
                queryChunkStartRow = "";
            } 
        }
        /*
         * ======== End code block to support PK chunking
         */
        this.monitor = monitor;
        this.rateCalc = rateCalc;
        this.updateProgress = updateProgress;
    }

    BulkApiVisitorUtil(Controller ctl, ILoaderProgress monitor, LoadRateCalculator rateCalc) {
        this(ctl, monitor, rateCalc, true);
    }

    String getJobId() {
        return this.jobInfo.getId();
    }
    
    public String getStagingFileInOutputStatusDir(String prefix, String suffix) {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MMddyyhhmmssSSS"); //$NON-NLS-1$
        String timestamp = format.format(currentTime);
    	String statusOutputDir = appConfig.getString(AppConfig.PROP_OUTPUT_STATUS_DIR);

        File stagingFile = new File(statusOutputDir, prefix + timestamp + suffix);
        return stagingFile.getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$

    }
    
    public JobInfo getJobInfo() {
        return this.jobInfo;
    }
    
    public void setJobInfo(JobInfo jinfo) {
        this.jobInfo = jinfo;
    }

    void createJob() throws AsyncApiException {
        JobInfo job = new JobInfo();
        final OperationEnum op = this.appConfig.getOperationInfo().getBulkOperationEnum();
        job.setOperation(op);
        if (op == OperationEnum.upsert) {
            job.setExternalIdFieldName(this.appConfig.getString(AppConfig.PROP_IDLOOKUP_FIELD));
        }
        job.setObject(this.appConfig.getString(AppConfig.PROP_ENTITY));
        ContentType jobContentType = ContentType.CSV;
        if (this.appConfig.getBoolean(AppConfig.PROP_BULK_API_ZIP_CONTENT) 
                && op != OperationEnum.query
                && !this.appConfig.isBulkV2APIEnabled()) {
            // ZIP CSV content is supported only for Bulk V1
            jobContentType = ContentType.ZIP_CSV;
        }
        job.setContentType(jobContentType);
        
        ConcurrencyMode jobConcurrencyMode = ConcurrencyMode.Parallel;
        if (this.appConfig.getBoolean(AppConfig.PROP_BULK_API_SERIAL_MODE) 
                && !this.appConfig.isBulkV2APIEnabled()) {
            // Serial mode is supported only for Bulk V1
           jobConcurrencyMode = ConcurrencyMode.Serial;
        }
        job.setConcurrencyMode(jobConcurrencyMode);
        
        if (op == OperationEnum.update || op == OperationEnum.upsert || op == OperationEnum.insert) {
            final String assRule = this.appConfig.getString(AppConfig.PROP_ASSIGNMENT_RULE);
            if (assRule != null && (assRule.length() == 15 || assRule.length() == 18)) {
                job.setAssignmentRuleId(assRule);
            }
        } else if (op == OperationEnum.query || op == OperationEnum.queryAll) {
            if (this.enablePKchunking) {
                String startRowParam = "";
                // startRow parameter of "Sforce-Enable-PKChunking" header has to be a valid
                // 15 or 18 char ID.
                if (this.queryChunkStartRow != null 
                    && (this.queryChunkStartRow.length() == 15 || this.queryChunkStartRow.length() == 18)) {
                    startRowParam = "; startRow=" + this.queryChunkStartRow;
                }
                this.connection.addHeader("Sforce-Enable-PKChunking", 
                                      "chunkSize=" + this.queryChunkSize + startRowParam);
            }
        }
        if (isBulkV2QueryJob()) {
            job.setObject(this.appConfig.getString(AppConfig.PROP_EXTRACT_SOQL));
            logger.info("going to create BulkV2 query job");
        }
        job = this.connection.createJob(job);
        logger.info(Messages.getMessage(getClass(), "logJobCreated", job.getId()));
        this.jobInfo = job;
    }

    private static final NumberFormat FILE_NUM_FMT;
    static {
        final NumberFormat fmt = NumberFormat.getIntegerInstance();
        fmt.setGroupingUsed(false);
        fmt.setMinimumIntegerDigits(3);
        FILE_NUM_FMT = fmt;
    }

    String addAttachment(byte[] fileContents) {
        final String name = "attachment_" + FILE_NUM_FMT.format(this.attachmentNum++);
        this.attachments.put(name, new ByteArrayInputStream(fileContents));
        return "#" + name;
    }
    
    public Map<String, InputStream> getAttachments() {
        return this.attachments;
    }

    public static final String MEMORY_USE_TAG_CREATE_BULK_UPLOAD_= "createBulkUploadBatch";
    BatchInfo createBatch(InputStream batchContent) throws AsyncApiException {
        BatchInfo batch = null;
        if (isBulkV2QueryJob()) {
            return null;
        } else if (isBulkV2LoadJob()) {
        	processBulkV2LoadBatch(batchContent);
        	batch = new BatchInfo();
        	batch.setId("BULKV2_LOAD_BATCH_" + this.bulkV2LoadBatchCount++);
        } else { // Bulk v1 job
	        BulkConnection connectionClient = this.controller.getBulkV1Client().getConnection();
	        if (this.jobInfo.getContentType() == ContentType.ZIP_CSV) {
	            batch = connectionClient.createBatchWithInputStreamAttachments(this.jobInfo, batchContent, this.attachments);
	        } else {
	            batch = connectionClient.createBatchFromStream(this.jobInfo, batchContent);
	        }
	        logger.info(Messages.getMessage(getClass(), "logBatchLoaded", batch.getId()));
        }
        AppUtil.captureUsedHeap(MEMORY_USE_TAG_CREATE_BULK_UPLOAD_);

        // Done creating a batch. Clear attachments map in preparation for the next batch
        this.attachments.clear();
        this.attachmentNum = 0;
        return batch;
    }
    
    void processBulkV2LoadBatch(InputStream batchContent) throws AsyncApiException {
        BulkV2Connection v2conn = this.controller.getBulkV2Client().getConnection();
        this.jobInfo = v2conn.startIngest(this.getJobId(), batchContent);
    }

    long periodicCheckStatus() throws AsyncApiException {
        if (this.monitor.isCanceled()) return 0;
        final long timeRemaining = this.checkStatusInterval - (System.currentTimeMillis() - this.lastStatusUpdate);
        int retryCount = 0;
        int maxAttemptsCount = 0;
        
        try {
            // limit the number of max retries in case limit is exceeded
            maxAttemptsCount = 1 + Math.min(AppConfig.MAX_RETRIES_LIMIT, this.appConfig.getInt(AppConfig.PROP_MAX_RETRIES));
        } catch (ParameterLoadException e) {
            maxAttemptsCount = 1 + AppConfig.DEFAULT_MAX_RETRIES;
        }
        if (timeRemaining <= 0) {
            while (retryCount++ < maxAttemptsCount) {
                try {
                    this.jobInfo = this.connection.getJobStatus(getJobId());
                    updateJobStatus();
                    return this.checkStatusInterval;
                } catch (AsyncApiException ex) {
                    if (retryCount < maxAttemptsCount) {
                        try {
                            Thread.sleep(this.checkStatusInterval);
                        } catch (final InterruptedException e) {}
                    } else {
                        throw ex;
                    }
                }          
            }
        }
        monitor.setNumberBatchesTotal(jobInfo.getNumberBatchesTotal());
        return timeRemaining;
    }

    private void awaitJobCompletion() throws AsyncApiException {
        long sleepTime = periodicCheckStatus();
        
        while (!isJobCompleted()) {
            if (this.monitor.isCanceled()) return;
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {}
            sleepTime = periodicCheckStatus();
        }
    }
    
    private boolean isBulkV2QueryJob() {
        final OperationEnum op = this.appConfig.getOperationInfo().getBulkOperationEnum();
        return (op == OperationEnum.query || op == OperationEnum.queryAll)
        && this.appConfig.isBulkV2APIEnabled();
    }
    
    private boolean isBulkV2LoadJob() {
        final OperationEnum op = this.appConfig.getOperationInfo().getBulkOperationEnum();
        return op != OperationEnum.query && op != OperationEnum.queryAll
        && this.appConfig.isBulkV2APIEnabled();
    }

    private boolean isJobCompleted() {
        if (isBulkV2QueryJob() || isBulkV2LoadJob()) {
            return this.jobInfo.getState() == JobStateEnum.JobComplete;
        } else { // bulk v1 flavor
            return this.jobInfo.getNumberBatchesQueued() == 0 
                    && this.jobInfo.getNumberBatchesInProgress() == 0;
        }
    }

    boolean hasJob() {
        return this.jobInfo != null;
    }
    
    void awaitCompletionAndCloseJob() throws AsyncApiException {
        this.jobInfo = this.connection.getJobStatus(getJobId());
        updateJobStatus();
        awaitJobCompletion();
        if (!isBulkV2QueryJob() && !isBulkV2LoadJob()) {
        	this.jobInfo = this.connection.closeJob(getJobId());
        }
    }

    private void updateJobStatus() {
        if (updateProgress) {
            this.monitor.worked(getNumRecordsProcessedInJob() - this.recordsProcessed);
            this.monitor.setSubTask(this.rateCalc.calculateSubTask(
                    getNumRecordsProcessedInJob(),
                    getNumRecordsFailedInJob()));
        }
        this.recordsProcessed = getNumRecordsProcessedInJob();
        this.lastStatusUpdate = System.currentTimeMillis();
        logger.info(Messages.getMessage(getClass(), "logJobStatus", this.jobInfo.getNumberBatchesQueued(),
                this.jobInfo.getNumberBatchesInProgress(), 
                this.jobInfo.getNumberBatchesCompleted(),
                this.jobInfo.getNumberBatchesFailed()));
    }
    
    // hack because jobInfo is not updated if an entire batch fails
    private int getNumRecordsProcessedInJob() {
        int numRecordsProcessedInJob = this.jobInfo.getNumberRecordsProcessed();
        if (appConfig.isBulkAPIEnabled() || appConfig.isBulkV2APIEnabled()) {
            // Bulk v2 counts all processed records in the total
            numRecordsProcessedInJob -= this.jobInfo.getNumberRecordsFailed();
        }
        int numRecordsPerBatch = 0;
        try {
            numRecordsPerBatch = this.appConfig.getInt(AppConfig.PROP_IMPORT_BATCH_SIZE);
        } catch (ParameterLoadException e) {
            logger.warn("Incorrectly configured " + AppConfig.PROP_IMPORT_BATCH_SIZE);
        }
        if (numRecordsProcessedInJob == 0 && this.jobInfo.getNumberBatchesCompleted() > 0) {
            numRecordsProcessedInJob = numRecordsPerBatch 
                    * this.jobInfo.getNumberBatchesCompleted();
        }
        return numRecordsProcessedInJob + getNumRecordsFailedInJob();
    }
    
    // hack because jobInfo is not updated if an entire batch fails
    private int getNumRecordsFailedInJob() {
        int numRecordsFailedInJob = this.jobInfo.getNumberRecordsFailed();
        int numRecordsPerBatch = 0;
        try {
            numRecordsPerBatch = this.appConfig.getInt(AppConfig.PROP_IMPORT_BATCH_SIZE);
        } catch (ParameterLoadException e) {
            logger.warn("Incorrectly configured " + AppConfig.PROP_IMPORT_BATCH_SIZE);
        }
        if (numRecordsFailedInJob == 0 && this.jobInfo.getNumberBatchesFailed() > 0) {
            numRecordsFailedInJob = numRecordsPerBatch * jobInfo.getNumberBatchesFailed();
        }
        return numRecordsFailedInJob;
    }
    BatchInfoList getBatches() throws AsyncApiException {
        BulkV1Connection connectionClient = this.controller.getBulkV1Client().getConnection();
        return connectionClient.getBatchInfoList(getJobId());
    }

    CSVReader getBatchResults(String batchId) throws AsyncApiException {
        BulkV1Connection connectionClient = this.controller.getBulkV1Client().getConnection();
        return new CSVReader(connectionClient.getBatchResultStream(getJobId(), batchId));
    }
    
    int getRecordsProcessed() throws ExtractException, AsyncApiException {
        if (!isBulkV2QueryJob()) { 
            // Bulk v1 job. check if a batch failed, and if so, throw an ExtractException.
            final BatchInfo[] batchInfoArray = getBatches().getBatchInfo();
            for (BatchInfo batchInfo : batchInfoArray) {
                if (batchInfo.getState() == BatchStateEnum.Failed) {
                    throw new ExtractExceptionOnServer("Batch failed: " + batchInfo.getStateMessage());
                }
            }
        }
        return getNumRecordsProcessedInJob();
    }
    
    void getBulkV2LoadSuccessResults(String filename, boolean append) throws AsyncApiException {
    	this.controller.getBulkV2Client().getConnection().saveIngestSuccessResults(this.getJobId(), filename, append);
    }
    
    void getBulkV2LoadErrorResults(String filename) throws AsyncApiException {
    	this.controller.getBulkV2Client().getConnection().saveIngestFailureResults(this.getJobId(), filename);
    }
    
    void getBulkV2LoadUnprocessedRecords(String filename) throws AsyncApiException {
    	this.controller.getBulkV2Client().getConnection().saveIngestUnprocessedRecords(this.getJobId(), filename);
    }
}
