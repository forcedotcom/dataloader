package com.salesforce.dataloader.process;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.ExceptionCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Return the exit code based on information read from ILoaderProgress.
 *
 * By default all are 0, Because this is how it behaved.
 * Configuration is made via Spring. But you need to switch to ReportCountProgressAdapter.
 *
 * Created by d.sipasseuth on 9/28/15.
 */
public class ExitCodeMapper {

    /** Default Exit code for exception */
    private static int DEFAULT_EXCEPTION_EXIT_CODE = -1;
    /** Exit code on exception to use */
    private int onException = DEFAULT_EXCEPTION_EXIT_CODE;
    /** Exit code to use on success */
    private int onSuccess = 0;
    /** Exit code to use on partial success */
    private int onPartialSuccess = 0;
    /** Exit code to use when no record worked */
    private int onNoneSuccess = 0;
    /** Exit code when no data found (0 read / 0 write) */
    private int onNoDataFound = 0;

    /**
     * Map dedicated to Api Error
     */
    private Map<ExceptionCode, Integer> apiFaultExitCodeMapper = new HashMap<ExceptionCode, Integer>();
    /**
     * Map dedicated to Async Api Error
     */
    private Map<AsyncExceptionCode, Integer> asyncApiExitCodeMapper = new HashMap<AsyncExceptionCode, Integer>();

    private Integer getApiErrorExitCode(ApiFault e) {
        return apiFaultExitCodeMapper.get(e.getExceptionCode());
    }

    private Integer getAsyncApiErrorExitCode(AsyncApiException e) {
        return asyncApiExitCodeMapper.get(e.getExceptionCode());
    }

    private int getExitCodeFromException(Exception e) {
        Integer exitCode = null;
        if (e instanceof ApiFault) {
            exitCode = getApiErrorExitCode((ApiFault) e);
        } else if (e instanceof AsyncApiException) {
            exitCode = getAsyncApiErrorExitCode((AsyncApiException) e);
        }
        if (exitCode == null){
            exitCode = onException;
        }
        return exitCode;
    }

    public int getExitCode(ILoaderProgress progressMonitor) {
        // Check exceptions (if exception -> no success or error count.
        if (progressMonitor.getErrorException() != null) {
            return getExitCodeFromException(progressMonitor.getErrorException());
        }
        // Check 0 Data
        if (progressMonitor.getErrorCount() != null && progressMonitor.getSuccessCount() != null
                && progressMonitor.getErrorCount() == 0 && progressMonitor.getSuccessCount() == 0) {
            return onNoDataFound;
        }

        if (progressMonitor.getErrorCount() > 0) {
            if (progressMonitor.getSuccessCount() > 0) {
                return onPartialSuccess;
            } else {
                return onNoneSuccess;
            }
        }
        // Everything is ok.
        return onSuccess;
    }

    public void setOnException(int onException) {
        this.onException = onException;
    }

    public void setOnNoDataFound(int onNoDataFound) {
        this.onNoDataFound = onNoDataFound;
    }

    public void setOnNoneSucceed(int onNoneSuccess) {
        this.onNoneSuccess = onNoneSuccess;
    }

    public void setOnPartialSuccess(int onPartialSuccess) {
        this.onPartialSuccess = onPartialSuccess;
    }

    public void setOnSuccess(int exitCode) {
        this.onSuccess = exitCode;
    }

    public void setApiFaultExitCodeMapper(Map<ExceptionCode, Integer> map) {
        this.apiFaultExitCodeMapper = map;
    }

    public void setAsyncApiExitCodeMapper(Map<AsyncExceptionCode, Integer> map) {
        this.asyncApiExitCodeMapper = map;
    }
}