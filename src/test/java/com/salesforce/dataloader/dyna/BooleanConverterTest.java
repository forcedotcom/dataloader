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

import org.apache.commons.beanutils.ConversionException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BooleanConverterTest {

    @Test
    public void testBooleanConverter() {
        BooleanConverter converter = new BooleanConverter();
        Boolean result;

        // test null and empty string, should return null
        result = (Boolean)converter.convert(null, null);
        assertNull(result);
        result = (Boolean)converter.convert(null, "");
        assertNull(result);

        // if we pass in a boolean, we should get the same one back
        result = (Boolean)converter.convert(null, Boolean.TRUE);
        assertEquals(Boolean.TRUE, result);

        result = (Boolean)converter.convert(null, Boolean.FALSE);
        assertEquals(Boolean.FALSE, result);

        // //////////////////////////
        // test the valid true values
        // //////////////////////////.
        result = (Boolean)converter.convert(null, "yes");
        assertEquals(Boolean.TRUE, result);

        result = (Boolean)converter.convert(null, "y");
        assertEquals(Boolean.TRUE, result);

        result = (Boolean)converter.convert(null, "true");
        assertEquals(Boolean.TRUE, result);

        result = (Boolean)converter.convert(null, "on");
        assertEquals(Boolean.TRUE, result);

        result = (Boolean)converter.convert(null, "1");
        assertEquals(Boolean.TRUE, result);

        // ///////////////////////////
        // Test the valid false values
        // ///////////////////////////
        result = (Boolean)converter.convert(null, "no");
        assertEquals(Boolean.FALSE, result);

        result = (Boolean)converter.convert(null, "n");
        assertEquals(Boolean.FALSE, result);

        result = (Boolean)converter.convert(null, "false");
        assertEquals(Boolean.FALSE, result);

        result = (Boolean)converter.convert(null, "off");
        assertEquals(Boolean.FALSE, result);

        result = (Boolean)converter.convert(null, "0");
        assertEquals(Boolean.FALSE, result);

        // For garbage, we throw a conversion Exception

        try {
            result = (Boolean)converter.convert(null, "qweorijo");
            Assert.fail();
        } catch (ConversionException e) {

        }
    }
}
