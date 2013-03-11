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
package com.salesforce.dataloader.dyna;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.TestBase;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SObjectReferenceConverterTest extends ConfigTestBase {

    @Test
    public void testSObjectReferenceConverter() throws ConnectionException {
        SObjectReferenceConverter refConverter = new SObjectReferenceConverter();
        SObjectReference ref;

        getController().login();
        getController().setReferenceDescribes();

        // null test
        ref = (SObjectReference)refConverter.convert(null, null);
        assertTrue(ref.isNull());

        // empty test
        ref = (SObjectReference)refConverter.convert(null, "");
        assertTrue(ref.isNull());

        // test getting SObjectReference back - string
        SObjectReference testRefStr = new SObjectReference("12345");
        ref = (SObjectReference)refConverter.convert(null, testRefStr.getReferenceExtIdValue());
        assertEquals(testRefStr, ref);

        // test getting SObjectReference back - number
        SObjectReference testRefNbr = new SObjectReference(12345);
        ref = (SObjectReference)refConverter.convert(null, testRefNbr.getReferenceExtIdValue());
        assertEquals(testRefNbr, ref);

        // test getting SObjectReference back - date
        SObjectReference testRefDate = new SObjectReference(Calendar.getInstance().getTime());
        ref = (SObjectReference)refConverter.convert(null, testRefDate.getReferenceExtIdValue());
        assertEquals(testRefDate, ref);

        // test validity of creating XML structure for foreign key ref
        testValidSObjectReference("12345", "Parent", true);
        testValidSObjectReference("12345", "Bogus", false);
    }

    private void testValidSObjectReference(String refValue, String relationshipName, boolean expectSuccess) {
        SObjectReference ref = new SObjectReference(refValue);
        SObject sObj = new SObject();
        String fkFieldName = TestBase.DEFAULT_ACCOUNT_EXT_ID_FIELD;

        try {
            ref.addReferenceToSObject(getController(), sObj, ObjectField.formatAsString("Parent",
                    TestBase.DEFAULT_ACCOUNT_EXT_ID_FIELD));

            SObject child = (SObject)sObj.getChild(relationshipName);
            boolean succeeded = child != null && child.getField(fkFieldName) != null && child.getField(fkFieldName)
                    .equals(refValue);
            if (expectSuccess && !succeeded || !expectSuccess && succeeded) {
                Assert.fail();
            }
        } catch (Exception e) {
            if (expectSuccess) {
                Assert.fail();
            }
        }
    }
}
