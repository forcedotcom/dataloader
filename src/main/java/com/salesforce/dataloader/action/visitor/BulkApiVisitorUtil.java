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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ExtractException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.util.LoadRateCalculator;
import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
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

class BulkApiVisitorUtil {

    private static final Logger logger = LogManager.getLogger(BulkApiVisitorUtil.class);

    private final BulkClientConnection client;

    private JobInfo jobInfo;
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
    private Config config = null;
    private Controller controller;
    private File bulkV2LoadUploadFile;
    private OutputStream bulkV2LoadUploadWriter = null;
    private int bulkV2LoadBatchCount = 0;
    private boolean bulkV2LoadContentUploaded = false;

    BulkApiVisitorUtil(Controller ctl, ILoaderProgress monitor, LoadRateCalculator rateCalc, boolean updateProgress) {
        this.config = ctl.getConfig();
        this.controller = ctl;
        if (isBulkV2QueryJob() || isBulkV2LoadJob()) {
            this.client = new BulkClientConnection(ctl.getBulkV2Client().getClient());
        	try {
				bulkV2LoadUploadFile = new File(getStagingFileInOutputStatusDir("bulkV2LoadUpload_", ".csv"));
				bulkV2LoadUploadWriter = new FileOutputStream(this.bulkV2LoadUploadFile);
			} catch (IOException e) {
				this.config.setValue(Config.BULK_API_ENABLED, true);
				this.config.setValue(Config.BULKV2_API_ENABLED, false);
			}
        } else {
            this.client = new BulkClientConnection(ctl.getBulkClient().getClient());
        }

        try {
            // getLong will return 0 if no value is provided
            long checkStatusInt = ctl.getConfig().getLong(Config.BULK_API_CHECK_STATUS_INTERVAL);
            this.checkStatusInterval = checkStatusInt > 0 ? checkStatusInt
                    : Config.DEFAULT_BULK_API_CHECK_STATUS_INTERVAL;
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
    	String statusOutputDir = config.getString(Config.OUTPUT_STATUS_DIR);

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
        final OperationEnum op = this.config.getOperationInfo().getOperationEnum();
        job.setOperation(op);
        if (op == OperationEnum.upsert) {
            job.setExternalIdFieldName(this.config.getString(Config.EXTERNAL_ID_FIELD));
        }
        job.setObject(this.config.getString(Config.ENTITY));
        job.setContentType(this.config.getBoolean(Config.BULK_API_ZIP_CONTENT) && op != OperationEnum.query ? ContentType.ZIP_CSV
                : ContentType.CSV);
        job.setConcurrencyMode(this.config.getBoolean(Config.BULK_API_SERIAL_MODE) ? ConcurrencyMode.Serial
                : ConcurrencyMode.Parallel);

        if (op == OperationEnum.update || op == OperationEnum.upsert || op == OperationEnum.insert) {
            final String assRule = this.config.getString(Config.ASSIGNMENT_RULE);
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
                this.client.addHeader("Sforce-Enable-PKChunking", 
                                      "chunkSize=" + this.queryChunkSize + startRowParam);
            }
        }
        if (isBulkV2QueryJob()) {
            job.setObject(this.config.getString(Config.EXTRACT_SOQL));
            logger.info("going to create BulkV2 query job");
        }
        job = this.client.createJob(job);
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

    BatchInfo createBatch(InputStream batchContent) throws AsyncApiException {
        BatchInfo batch = null;
        if (isBulkV2QueryJob()) {
            return null;
        } else if (isBulkV2LoadJob()) {
        	processBulkV2LoadBatch(batchContent);
        	batch = new BatchInfo();
        	batch.setId("BULKV2_LOAD_BATCH_" + this.bulkV2LoadBatchCount++);
        	return batch;
        } else { // Bulk v1 job
	        BulkConnection connectionClient = this.controller.getBulkClient().getClient();
	        if (this.jobInfo.getContentType() == ContentType.ZIP_CSV) {
	            batch = connectionClient.createBatchWithInputStreamAttachments(this.jobInfo, batchContent, this.attachments);
	        } else {
	            batch = connectionClient.createBatchFromStream(this.jobInfo, batchContent);
	        }
	        logger.info(Messages.getMessage(getClass(), "logBatchLoaded", batch.getId()));
	        return batch;
        }
    }
    
    void processBulkV2LoadBatch(InputStream batchContent) throws AsyncApiException {
        try {
	        //download batch to be uploaded into the buffering file
	        int bytesRead;
	        byte[] buffer = new byte[8 * 1024];
	        while ((bytesRead = batchContent.read(buffer)) != -1) {
	        	this.bulkV2LoadUploadWriter.write(buffer, 0, bytesRead);
	        }
        } catch (IOException e) {
			throw new AsyncApiException(e.getMessage(), AsyncExceptionCode.Unknown);
		}
    }

    void uploadJobContent() throws AsyncApiException {
    	try {
			this.bulkV2LoadUploadWriter.flush();
	    	this.bulkV2LoadUploadWriter.close();
	    	this.bulkV2LoadContentUploaded = true;
	    	BulkV2Connection v2conn = this.controller.getBulkV2Client().getClient();
	    	this.jobInfo = v2conn.startIngest(this.getJobId(), this.bulkV2LoadUploadFile.getAbsolutePath());
		} catch (IOException e) {
			throw new AsyncApiException(e.getMessage(), AsyncExceptionCode.Unknown);
		}
    }

    long periodicCheckStatus() throws AsyncApiException {
    	if (isBulkV2LoadJob() && !this.bulkV2LoadContentUploaded) {
    		uploadJobContent();
    	}

        if (this.monitor.isCanceled()) return 0;
        final long timeRemaining = this.checkStatusInterval - (System.currentTimeMillis() - this.lastStatusUpdate);
        int retryCount = 0;
        int maxAttemptsCount = 0;
        
        try {
            // limit the number of max retries in case limit is exceeded
            maxAttemptsCount = 1 + Math.min(Config.MAX_RETRIES_LIMIT, this.config.getInt(Config.MAX_RETRIES));
        } catch (ParameterLoadException e) {
            maxAttemptsCount = 1 + Config.DEFAULT_MAX_RETRIES;
        }
        if (timeRemaining <= 0) {
            while (retryCount++ < maxAttemptsCount) {
                try {
                    this.jobInfo = this.client.getJobStatus(getJobId(), this.jobInfo.getOperation() == OperationEnum.query);
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
        if (this.bulkV2LoadUploadFile != null) {
        	try {
				this.bulkV2LoadUploadWriter.close();
				this.bulkV2LoadUploadFile.delete();
				this.bulkV2LoadUploadWriter = null;
				this.bulkV2LoadUploadFile = null;
			} catch (IOException e) {
				logger.warn("Unable to close and delete bulk v2 Load staging file");
			}
        	this.bulkV2LoadUploadFile = null;
        }
    }
    
    private boolean isBulkV2QueryJob() {
        final OperationEnum op = this.config.getOperationInfo().getOperationEnum();
        return (op == OperationEnum.query || op == OperationEnum.queryAll)
        && this.config.isBulkV2APIEnabled();
    }
    
    private boolean isBulkV2LoadJob() {
        final OperationEnum op = this.config.getOperationInfo().getOperationEnum();
        return op != OperationEnum.query && op != OperationEnum.queryAll
        && this.config.isBulkV2APIEnabled();
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
        this.jobInfo = this.client.getJobStatus(getJobId(), this.jobInfo.getOperation() == OperationEnum.query);
        updateJobStatus();
        awaitJobCompletion();
        if (!isBulkV2QueryJob() && !isBulkV2LoadJob()) {
        	this.jobInfo = this.client.closeJob(getJobId(), this.jobInfo.getOperation() == OperationEnum.query);
        }
    }

    private void updateJobStatus() {
        if (updateProgress) {
            this.monitor.worked(this.jobInfo.getNumberRecordsProcessed() - this.recordsProcessed);
            this.monitor.setSubTask(this.rateCalc.calculateSubTask(this.jobInfo.getNumberRecordsProcessed(),
                    this.jobInfo.getNumberRecordsFailed()));
        }
        this.recordsProcessed = this.jobInfo.getNumberRecordsProcessed();
        this.lastStatusUpdate = System.currentTimeMillis();
        logger.info(Messages.getMessage(getClass(), "logJobStatus", this.jobInfo.getNumberBatchesQueued(),
                this.jobInfo.getNumberBatchesInProgress(), this.jobInfo.getNumberBatchesCompleted(),
                this.jobInfo.getNumberBatchesFailed()));
    }

    BatchInfoList getBatches() throws AsyncApiException {
        BulkConnection connectionClient = this.controller.getBulkClient().getClient();
        return connectionClient.getBatchInfoList(getJobId());
    }

    CSVReader getBatchResults(String batchId) throws AsyncApiException {
        BulkConnection connectionClient = this.controller.getBulkClient().getClient();
        return new CSVReader(connectionClient.getBatchResultStream(getJobId(), batchId));
    }
    
    int getRecordsProcessed() throws ExtractException, AsyncApiException {
        if (!isBulkV2QueryJob()) { 
            // Bulk v1 job. check if a batch failed, and if so, throw an ExtractException.
            final BatchInfo[] batchInfoArray = getBatches().getBatchInfo();
            for (BatchInfo batchInfo : batchInfoArray) {
                if (batchInfo.getState() == BatchStateEnum.Failed) {
                    throw new ExtractException("Batch failed: " + batchInfo.getStateMessage());
                }
            }
        }
        return this.jobInfo.getNumberRecordsProcessed();
    }
    
    void getBulkV2LoadSuccessResults(String filename) throws AsyncApiException {
    	this.controller.getBulkV2Client().getClient().saveIngestSuccessResults(this.getJobId(), filename);
    }
    
    void getBulkV2LoadErrorResults(String filename) throws AsyncApiException {
    	this.controller.getBulkV2Client().getClient().saveIngestFailureResults(this.getJobId(), filename);
    }
    
    void getBulkV2LoadUnprocessedRecords(String filename) throws AsyncApiException {
    	this.controller.getBulkV2Client().getClient().saveIngestUnprocessedRecords(this.getJobId(), filename);
    }
}
