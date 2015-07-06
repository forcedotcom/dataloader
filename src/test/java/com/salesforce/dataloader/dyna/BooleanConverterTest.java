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
package com.salesforce.dataloader.dyna;

import org.apache.commons.beanutils.ConversionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BooleanConverterTest {

    private static final String[] VALID_TRUE_VALUES = {"yes", "y", "true", "on", "1"};
    private static final String[] VALID_FALSE_VALUES = {"no", "n", "false", "off", "0"};
    private final BooleanConverter converter = new BooleanConverter();

    @Test(expected = ConversionException.class)
    public void testConvertInvalidString() {
        converter.convert(null, "qweorijo");
    }

    @Test
    public void testConvertEmptyString() {
        assertNull(converter.convert(null, ""));
    }

    @Test
    public void testConvertNull() {
        assertNull(converter.convert(null, null));
    }

    @Test
    public void testConvertValidFalseValues() {
        for(String validFalseValue : VALID_FALSE_VALUES) {
            assertFalse((Boolean) converter.convert(null, validFalseValue));
        }
    }

    @Test
    public void testConvertValidTrueValues() {
        for(String validTrueValue : VALID_TRUE_VALUES) {
            assertTrue((Boolean) converter.convert(null, validTrueValue));
        }
    }

    @Test
    public void testConvertBooleanFalse() {
        assertEquals(Boolean.FALSE, converter.convert(null, Boolean.FALSE));
    }

    @Test
    public void testConvertBooleanTrue() {
        assertEquals(Boolean.TRUE, converter.convert(null, Boolean.TRUE));
    }
}
