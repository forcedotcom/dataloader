/*
 * Copyright (c) 2011, salesforce.com, inc.
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
package com.salesforce.dataloader;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.salesforce.dataloader.action.ExtractTest;
import com.salesforce.dataloader.client.PartnerClientTest;
import com.salesforce.dataloader.dao.CsvTest;
import com.salesforce.dataloader.dao.database.DatabaseTest;
import com.salesforce.dataloader.dyna.ConverterTest;
import com.salesforce.dataloader.mapping.MappingTest;
import com.salesforce.dataloader.mapping.SoqlInfoTest;
import com.salesforce.dataloader.process.*;

/**
 * Class to run all the dataloader test cases
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class AllTests {

    public static Test suite() {
        final TestSuite suite = new TestSuite("dataloader tests");
        suite.addTestSuite(LicenseHeaderTest.class);
        suite.addTestSuite(CsvTest.class);
        suite.addTestSuite(SoqlInfoTest.class);
        suite.addTestSuite(DatabaseTest.class);
        suite.addTestSuite(ConverterTest.class);
        suite.addTestSuite(MappingTest.class);
        suite.addTestSuite(ExtractTest.class);
        suite.addTestSuite(PartnerClientTest.class);
        suite.addTest(DateProcessTest.suite());
        suite.addTest(CsvExtractProcessTest.suite());
        suite.addTest(CsvExtractAllProcessTest.suite());
        suite.addTest(CsvHardDeleteTest.suite());
        suite.addTest(CsvUpsertProcessTest.suite());
        // suite.addTest(CsvProcessWithOffsetTest.suite());
        suite.addTest(CsvProcessTest.suite());
        suite.addTest(DatabaseProcessTest.suite());

        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
