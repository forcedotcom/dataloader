package com.salesforce.dataloader.action.progress;

import org.apache.log4j.Logger;

/**
 * Created by d.sipasseuth on 9/28/15.
 */
public class ReportCountProgressAdapter implements ILoaderProgress {

    /**
     * Success item count
     */
    private int successCount = 0;
    /**
     * Error item count
     */
    private int errorCount = 0;
    /**
     * Worked item count
     */
    private int workedCount = 0;

    /**
     * Total batch count reported.
     */
    private int totalBatchCount = 0;

    /**
     * Total Work Count
     */
    private int totalWorkCount = 0;

    /** Task Name */
    private String taskName;

    /** Error Exception */
    private Exception errorException;

    //logger
    private final Logger logger = Logger.getLogger(getClass());

    @Override
    public void beginTask(String name, int totalWork) {
        this.taskName = name;
        this.totalWorkCount = totalWork;
    }

    @Override
    public void doneError(String errorMsg) {
        logger.error(errorMsg);
    }

    @Override
    public void setErrorException(Exception errorException) {
        this.errorException = errorException;
    }

    @Override
    public Exception getErrorException() {
        return errorException;
    }

    @Override
    public void doneSuccess(String msg) {
        logger.info(msg);
    }

    @Override
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    @Override
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    @Override
    public void worked(int worked) {
        this.workedCount += worked;
    }

    @Override
    public Integer getWorkedCount() {
        return workedCount;
    }

    @Override
    public void setSubTask(String name) {
        logger.info(name);
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setNumberBatchesTotal(int numberBatchesTotal) {
        this.totalBatchCount = numberBatchesTotal;
    }

    @Override
    public Integer getNumberBatchesTotal() {
        return totalBatchCount;
    }

    /**
     * @return the reported success count.
     */
    @Override
    public Integer getSuccessCount() {
        return successCount;
    }

    /**
     * @return the reported error count.
     */
    @Override
    public Integer getErrorCount() {
        return errorCount;
    }

    /**
     * @return the total work count reported at beginning of task.
     */
    @Override
    public Integer getTotalWorkCount() {
        return totalWorkCount;
    }
}
