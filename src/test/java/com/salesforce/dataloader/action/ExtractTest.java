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
package com.salesforce.dataloader.action;

import com.salesforce.dataloader.exception.ExtractException;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for extract action
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class ExtractTest {

    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(ExtractTest.class);

    @Test
    public void testQueryParsePositive() throws Exception {
        // test simple soql
        positiveQueryParse("select field1, field2, field3 from account",
                new String[] {"field1", "field2", "field3"});

        // test for bug#89726 -- use keywords in the soql
        positiveQueryParse("select fromField1, fromField2, fromField3 from account",
                new String[] {"fromField1", "fromField2", "fromField3"});
        positiveQueryParse("select selectField1, selectField2, selectField3 from account",
                new String[] {"selectField1", "selectField2", "selectField3"});
        positiveQueryParse("select    from  ,  select   from    select",
                new String[] {"from", "select"});
        positiveQueryParse("select from, select from select where select like '0' and from like '1'",
                new String[] {"from", "select"});
        positiveQueryParse("select max(name) mname from Account group by Industry",
                new String[] {"max(name) mname"});
        positiveQueryParse("select max(name) from Account group by Industry",
                new String[] {"max(name)"});
        positiveQueryParse("select max(name), industry, max(name) mname, industry from Account group by Industry",
                new String[] {"max(name)", "industry", "max(name) mname", "industry"});
    }

    @Test
    public void testQueryParseNegative() {
        // empty
        negativeQueryParse(null);
        negativeQueryParse("");

        // malformed queries
        negativeQueryParse(" from Account");
        negativeQueryParse("select field1, field2, field3 from ");
        negativeQueryParse("account field1, field2, field3 from select ");
    }

    private void positiveQueryParse(String soqlString, String[] expectedFieldArray) throws ExtractException {
        List<String> expectedFields = Arrays.asList(expectedFieldArray);
        List<String> fields = AbstractExtractAction.getColumnsFromSoql(soqlString, logger);
        assertEquals("Expected list of fields: " + expectedFields.toString(), fields, expectedFields);
    }

    private void negativeQueryParse(String soqlString) {
        try {
            List<String> fields = AbstractExtractAction.getColumnsFromSoql(soqlString, logger);
            Assert.fail("The parse should have failed with an error, instead of getting fields: " + fields.toString());
        } catch (ExtractException e) {
            assertNotNull("The parse error message should not be null", e.getMessage());
            assertTrue("The parse error message should not be empty", !e.getMessage().isEmpty());
        }
    }

}
