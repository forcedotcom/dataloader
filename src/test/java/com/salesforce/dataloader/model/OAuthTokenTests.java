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
package com.salesforce.dataloader.model;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by rmazzeo on 12/9/15.
 */
public class OAuthTokenTests {
    @Test
    public void testAccessToken(){
        OAuthToken target = new OAuthToken();
        String expected = "expected";

        target.setAccessToken(expected);
        String actual = target.getAccessToken();

        Assert.assertEquals(actual, expected, "Access tokens differed");
    }

    @Test
    public void testTokenType(){
        OAuthToken target = new OAuthToken();
        String expected = "expected";

        target.setTokenType(expected);
        String actual = target.getTokenType();

        Assert.assertEquals(actual, expected, "Token type differed");
    }

    @Test
    public void testSignature(){
        OAuthToken target = new OAuthToken();
        String expected = "expected";

        target.setSignature(expected);
        String actual = target.getSignature();

        Assert.assertEquals(actual, expected, "Signature differed");
    }

    @Test
    public void testInstanceUrl(){
        OAuthToken target = new OAuthToken();
        String expected = "expected";

        target.setInstanceUrl(expected);
        String actual = target.getInstanceUrl();

        Assert.assertEquals(actual, expected, "Instance Url differed");
    }

    @Test
    public void testScope(){
        OAuthToken target = new OAuthToken();
        String expected = "expected";

        target.setScope(expected);
        String actual = target.getScope();

        Assert.assertEquals(actual, expected, "Scope differed");
    }

    @Test
    public void testId(){
        OAuthToken target = new OAuthToken();
        String expected = "expected";

        target.setId(expected);
        String actual = target.getId();

        Assert.assertEquals(actual, expected, "Id differed");
    }

    @Test
    public void testIssuedAt(){
        OAuthToken target = new OAuthToken();
        Long expected = 19999999999L;

        target.setIssuedAt(expected);
        Long actual = target.getIssuedAt();

        Assert.assertEquals(actual, expected, "IssuedAt differed");
    }

    @Test
    public void testRefreshToken(){
        OAuthToken target = new OAuthToken();
        String expected = "expected";

        target.setRefreshToken(expected);
        String actual = target.getRefreshToken();

        Assert.assertEquals(actual, expected, "RefreshToken differed");
    }
}
