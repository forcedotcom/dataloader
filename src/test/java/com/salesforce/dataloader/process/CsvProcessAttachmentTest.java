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

package com.salesforce.dataloader.process;

import java.io.File;
import java.util.Map;

import junit.framework.TestSuite;

import com.salesforce.dataloader.*;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.exception.UnsupportedOperationException;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Test uploading attachments.
 *
 * @author 
 * @since 
 */
public class CsvProcessAttachmentTest extends ProcessTestBase {

    protected CsvProcessAttachmentTest(String name) {
        super(name);
    }
    
    public CsvProcessAttachmentTest(String name, Map<String, String> config) {
        super(name, config);
    }

    public static TestSuite suite() {
        return ConfigTestSuite.createSuite(CsvProcessAttachmentTest.class);
    }

    public static ConfigGenerator getConfigGenerator() {
        final ConfigGenerator parent = ProcessTestBase.getConfigGenerator();
        final ConfigGenerator withBulkApi = new ConfigSettingGenerator(parent, Config.USE_BULK_API,
                Boolean.TRUE.toString());
        final ConfigGenerator bulkApiZipContent = new ConfigSettingGenerator(withBulkApi, Config.BULK_API_ZIP_CONTENT,
                Boolean.TRUE.toString());
        final ConfigGenerator bulkApiSerialMode = new ConfigSettingGenerator(withBulkApi, Config.BULK_API_SERIAL_MODE,
                Boolean.TRUE.toString());
        return new UnionConfigGenerator(parent, withBulkApi, bulkApiSerialMode, bulkApiZipContent);
    }
    
    public void testCreateAttachment() throws ProcessInitializationException, DataAccessObjectException {
        // convert the template using the parent account id
        final String fileName = convertTemplateToInput(this.baseName + "Template.csv", this.baseName + ".csv",
                new AttachmentTemplateListener());

        final Map<String, String> argMap = getTestConfig(OperationInfo.insert, fileName, false);
        argMap.put(Config.ENTITY, "Attachment");

        // this feature does not work when bulk api is enabled but the zip content type is not
        final boolean bulkApi = isBulkAPIEnabled(argMap);
        final boolean zipContent = isSettingEnabled(argMap, Config.BULK_API_ZIP_CONTENT);
        if (bulkApi && !zipContent) {
            final String failureMessage = "Data Loader cannot map \"Body\" field using Bulk API and CSV content type.  Please enable the ZIP_CSV content type for Bulk API.";
            runProcessNegative(argMap, failureMessage);
        } else {
            runProcess(argMap, 1);
        }
    }


    /**
     * Verify that multiple binary files can be correctly zipped up and inserted into a record.
     *
     * @expectedResults Assert that the binaries of input and queried files are equal.
     */
    public void testCreateAttachmentMultipleFiles() throws ProcessInitializationException, DataAccessObjectException,
    ConnectionException {

        AttachmentTemplateListener myAttachmentTemplateListener = new AttachmentTemplateListener();

        final String fileName = convertTemplateToInput(this.baseName + "Template.csv", this.baseName + ".csv",
                myAttachmentTemplateListener);

        final Map<String, String> argMap = getTestConfig(OperationInfo.insert, fileName, false);
        argMap.put(Config.ENTITY, "Attachment");

        // this feature does not work when bulk api is enabled but the zip content type is not
        final boolean bulkApi = isBulkAPIEnabled(argMap);
        final boolean zipContent = isSettingEnabled(argMap, Config.BULK_API_ZIP_CONTENT);
        if (bulkApi && !zipContent) {
            final String failureMessage = "Data Loader cannot map \"Body\" field using Bulk API and CSV content type.  Please enable the ZIP_CSV content type for Bulk API.";
            runProcessNegative(argMap, failureMessage);
        } else {
            runProcessWithAttachmentListener(argMap, 3, myAttachmentTemplateListener,"Bay-Bridge.jpg", "BayBridgeBW.jpg", "BayBridgeFromTreasureIsland.jpg");
        }
    }
    
    public class AttachmentTemplateListener extends AccountIdTemplateListener {
        public AttachmentTemplateListener() {
            super(1);
        }

        @Override
        public void updateRow(int idx, Map<String, Object> row) {
            // set parent account id
            row.put("ParentId", getAccountIds()[0]);
            // make body pathname absolute
            String filePath = (String)row.get("Body");
            row.put("Body", getTestDataDir() + File.separator + filePath);
        }
    }

    private Controller runProcessWithAttachmentListener(Map<String, String> argMap, boolean expectProcessSuccess, String failMessage,
            int numInserts, int numUpdates, int numFailures, AttachmentTemplateListener myAttachmentTemplateListener, String... files) throws ProcessInitializationException,
            DataAccessObjectException, ConnectionException {

        if (argMap == null) argMap = getTestConfig();

        final ProcessRunner runner = ProcessRunner.getInstance(argMap);
        runner.setName(this.baseName);

        final TestProgressMontitor monitor = new TestProgressMontitor();
        runner.run(monitor);
        Controller controller = runner.getController();

        // verify process completed as expected
        if (expectProcessSuccess) {

            verifyInsertCorrectByContent(controller, createAttachmentFileMap(files), myAttachmentTemplateListener);
            //this should also still work
            assertTrue("Process failed: " + monitor.getMessage(), monitor.isSuccess());
            verifyFailureFile(controller, numFailures);        //A.S.: To be removed and replaced
            verifySuccessFile(controller, numInserts, numUpdates, false);

        } else {
            assertFalse("Expected process to fail but got success: " + monitor.getMessage(), monitor.isSuccess());
        }
        // TODO: validate all messages, including nulls if those exist
        if (failMessage != null) {
            assertEquals("wrong message: ", failMessage, monitor.getMessage());
        }

        // return the controller used by the process so that the tests can validate success/error output files, etc
        return controller;
    }
    
    protected Controller runProcessWithAttachmentListener(Map<String, String> argMap, int numRows, AttachmentTemplateListener myAttachmentTemplateListener, String... files) throws ProcessInitializationException,
    DataAccessObjectException, ConnectionException {
        return runProcessWithErrorsWithAttachmentListener(argMap, numRows, 0, myAttachmentTemplateListener,files);
    }
    
    protected Controller runProcessWithErrorsWithAttachmentListener(Map<String, String> argMap, int numSuccesses, int numFailures, AttachmentTemplateListener myAttachmentTemplateListener, String... files)
            throws ProcessInitializationException, DataAccessObjectException, ConnectionException {
        int numInserts = 0;
        int numUpdates = 0;

        OperationInfo op = OperationInfo.valueOf(argMap.get(Config.OPERATION));
        if (op == OperationInfo.insert)
            numInserts = numSuccesses;
        else if (op != null && op != OperationInfo.upsert)
            numUpdates = numSuccesses;
        else
            throw new UnsupportedOperationException(op + " not supported");
        return runProcessWithAttachmentListener(argMap, true, null, numInserts, numUpdates, numFailures, myAttachmentTemplateListener, files);
    }
    
}
