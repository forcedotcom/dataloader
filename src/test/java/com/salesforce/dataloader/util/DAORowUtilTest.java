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

package com.salesforce.dataloader.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DAORowUtilTest {
    @Test
    public void testNullPhoneFieldValue() {
        String result = DAORowUtil.getPhoneFieldValue(null, null);
        assertEquals("incorrect conversion: ", 
                null,
                result);
    }
    @Test
    public void testTenDigitUSPhoneFieldValue() {
        String result = DAORowUtil.getPhoneFieldValue("1234567890", "en_US");
        assertEquals("incorrect conversion: ", 
                "(123) 456-7890",
                result);
    }
    @Test
    public void testTenDigitCAPhoneFieldValue() {
        String result = DAORowUtil.getPhoneFieldValue("1234567890", "en_CA");
        assertEquals("incorrect conversion: ", 
                "(123) 456-7890",
                result);
    }
    @Test
    public void testElevenDigitUSPhoneFieldValue() {
        String result = DAORowUtil.getPhoneFieldValue("11234567890", "en_US");
        assertEquals("incorrect conversion: ", 
                "(123) 456-7890",
                result);
    }
    @Test
    public void testElevenDigitCAPhoneFieldValue() {
        String result = DAORowUtil.getPhoneFieldValue("11234567890", "en_CA");
        assertEquals("incorrect conversion: ", 
                "(123) 456-7890",
                result);
    }
    @Test
    public void testTwelveDigitPhoneFieldValue() {
        String result = DAORowUtil.getPhoneFieldValue("112345678901", "en_US");
        assertEquals("incorrect conversion: ", 
                "112345678901",
                result);
    }
    @Test
    public void testNineDigitPhoneFieldValue() {
        String result = DAORowUtil.getPhoneFieldValue("123456789", "en_US");
        assertEquals("incorrect conversion: ", 
                "123456789",
                result);
    }
    @Test
    public void testElevenDigitPhoneFieldValueNotStartingWithOne() {
        String result = DAORowUtil.getPhoneFieldValue("21234567890", "en_US");
        assertEquals("incorrect conversion: ", 
                "21234567890",
                result);
    }
    @Test
    public void testTenDigitPhoneFieldValueStartingWithPlus() {
        String result = DAORowUtil.getPhoneFieldValue("+1234567890", "en_US");
        assertEquals("incorrect conversion: ", 
                "+1234567890",
                result);
    }
    @Test
    public void testTenDigitPhoneFieldValueStartingWithMinus() {
        String result = DAORowUtil.getPhoneFieldValue("-1234567890", "en_US");
        assertEquals("incorrect conversion: ", 
                "-1234567890",
                result);
    }
    @Test
    public void testTenDigitPhoneFieldValueNonUSLocale() {
        String result = DAORowUtil.getPhoneFieldValue("1234567890", "en_UK");
        assertEquals("incorrect conversion: ", 
                "1234567890",
                result);
    }
}
