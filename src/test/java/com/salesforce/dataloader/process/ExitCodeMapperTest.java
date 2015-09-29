package com.salesforce.dataloader.process;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.progress.ReportCountProgressAdapter;
import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.ExceptionCode;
import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Testing the exit code mapper.
 *
 * Created by d.sipasseuth on 9/29/15.
 */
public class ExitCodeMapperTest {

    private static final int ON_EXCEPTION_RETURN_CODE = 10;
    private static final int ON_NO_DATA_FOUND_CODE = 20;
    private static final int ON_PARTIAL_SUCCESS = 30;
    private static final int ON_NONE_SUCCEED_CODE = 40;
    private static final int ON_SUCCESS = 50;
    private static final int ON_CUSTOM_API_FAULT_EXCEPTION_CODE = 60;
    private static final int ON_CUSTOM_ASYNC_API_EXCEPTION_CODE = 70;

    private static final ExceptionCode TESTED_API_FAULT_EXCEPTION_CODE = ExceptionCode.INVALID_ID_FIELD;
    private static final AsyncExceptionCode TESTED_ASYNC_API_EXCEPTION_CODE = AsyncExceptionCode.ExceededQuota;

    private ExitCodeMapper testedInstance;

    @Before
    public void setUp() {
        testedInstance = new ExitCodeMapper();
        testedInstance.setOnException(ON_EXCEPTION_RETURN_CODE);
        testedInstance.setOnNoDataFound(ON_NO_DATA_FOUND_CODE);
        testedInstance.setOnPartialSuccess(ON_PARTIAL_SUCCESS);
        testedInstance.setOnNoneSucceed(ON_NONE_SUCCEED_CODE);
        testedInstance.setOnSuccess(ON_SUCCESS);

        Map<ExceptionCode, Integer> apiFaultMap = new HashMap<ExceptionCode, Integer>();
        apiFaultMap.put(TESTED_API_FAULT_EXCEPTION_CODE, ON_CUSTOM_API_FAULT_EXCEPTION_CODE);
        testedInstance.setApiFaultExitCodeMapper(apiFaultMap);

        Map<AsyncExceptionCode, Integer> asyncApiException = new HashMap<AsyncExceptionCode, Integer>();
        asyncApiException.put(TESTED_ASYNC_API_EXCEPTION_CODE, ON_CUSTOM_ASYNC_API_EXCEPTION_CODE);
        testedInstance.setAsyncApiExitCodeMapper(asyncApiException);
    }

    @Test
    public void should_return_ok() {
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        progressLoader.setErrorCount(0);
        progressLoader.setSuccessCount(1);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_SUCCESS);
    }

    @Test
    public void should_return_partial() {
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        progressLoader.setErrorCount(1);
        progressLoader.setSuccessCount(1);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_PARTIAL_SUCCESS);
    }

    @Test
    public void should_return_none_succeed() {
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        progressLoader.setErrorCount(1);
        progressLoader.setSuccessCount(0);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_NONE_SUCCEED_CODE);
    }

    @Test
    public void should_return_no_data() {
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        progressLoader.setErrorCount(0);
        progressLoader.setSuccessCount(0);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_NO_DATA_FOUND_CODE);
    }

    @Test
    public void should_return_custom_async_exception() {
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        AsyncApiException expectedException = new AsyncApiException();
        expectedException.setExceptionCode(TESTED_ASYNC_API_EXCEPTION_CODE);
        expectedException.setExceptionMessage("testing");
        progressLoader.setErrorException(expectedException);
        progressLoader.setErrorCount(1);
        progressLoader.setSuccessCount(1);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_CUSTOM_ASYNC_API_EXCEPTION_CODE);
    }

    @Test
    public void should_return_custom_api_exception() {
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        ApiFault expectedException = new ApiFault();
        expectedException.setExceptionCode(TESTED_API_FAULT_EXCEPTION_CODE);
        expectedException.setExceptionMessage("testing");
        progressLoader.setErrorException(expectedException);
        progressLoader.setErrorCount(1);
        progressLoader.setSuccessCount(1);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_CUSTOM_API_FAULT_EXCEPTION_CODE);
    }

    @Test
    public void should_return_generic_api_exception() {
        ExceptionCode usedExceptionCode = ExceptionCode.UNKNOWN_EXCEPTION;
        Assert.assertNotEquals(usedExceptionCode, TESTED_API_FAULT_EXCEPTION_CODE);
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        ApiFault expectedException = new ApiFault();
        expectedException.setExceptionCode(usedExceptionCode);
        expectedException.setExceptionMessage("testing");
        progressLoader.setErrorException(expectedException);
        progressLoader.setErrorCount(1);
        progressLoader.setSuccessCount(1);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_EXCEPTION_RETURN_CODE);
    }

    @Test
    public void should_return_generic_async_api_exception() {
        AsyncExceptionCode usedExceptionCode = AsyncExceptionCode.InvalidBatch;
        Assert.assertNotEquals(usedExceptionCode, TESTED_ASYNC_API_EXCEPTION_CODE);
        ILoaderProgress progressLoader = new ReportCountProgressAdapter();
        AsyncApiException expectedException = new AsyncApiException();
        expectedException.setExceptionCode(usedExceptionCode);
        expectedException.setExceptionMessage("testing");
        progressLoader.setErrorException(expectedException);
        progressLoader.setErrorCount(1);
        progressLoader.setSuccessCount(1);
        Assert.assertEquals(testedInstance.getExitCode(progressLoader), ON_EXCEPTION_RETURN_CODE);
    }
}