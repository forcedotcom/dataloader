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
import java.io.InputStream;
import java.text.NumberFormat;
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

    BulkApiVisitorUtil(Controller ctl, ILoaderProgress monitor, LoadRateCalculator rateCalc, boolean updateProgress) {
        this.config = ctl.getConfig();
        this.controller = ctl;
        if (isBulkV2QueryJob()) {
            this.client = new BulkClientConnection(ctl.getBulkV2Client().getClient());
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
        }
        BulkConnection connectionClient = this.controller.getBulkClient().getClient();
        if (this.jobInfo.getContentType() == ContentType.ZIP_CSV) {
            batch = connectionClient.createBatchWithInputStreamAttachments(this.jobInfo, batchContent, this.attachments);
        } else {
            batch = connectionClient.createBatchFromStream(this.jobInfo, batchContent);
        }
        logger.info(Messages.getMessage(getClass(), "logBatchLoaded", batch.getId()));
        return batch;
    }

    long periodicCheckStatus() throws AsyncApiException {
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
                    this.jobInfo = this.client.getJobStatus(getJobId());
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
        final OperationEnum op = this.config.getOperationInfo().getOperationEnum();
        return (op == OperationEnum.query || op == OperationEnum.queryAll)
        && this.config.getBoolean(Config.ENABLE_BULK_V2_QUERY);
    }
    
    private boolean isJobCompleted() {
        if (isBulkV2QueryJob()) {
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
        this.jobInfo = this.client.getJobStatus(getJobId());
        updateJobStatus();
        awaitJobCompletion();
        if (!isBulkV2QueryJob()) {
        	this.jobInfo = this.client.closeJob(getJobId());
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
            final BatchInfo[] batchInfoArray = getBatches().getBatchInfo();
            for (BatchInfo batchInfo : batchInfoArray) {
                if (batchInfo.getState() == BatchStateEnum.Failed) {
                    throw new ExtractException("Batch failed: " + batchInfo.getStateMessage());
                }
                this.recordsProcessed += batchInfo.getNumberRecordsProcessed();
            }
        }
        return this.recordsProcessed;
    }
}
