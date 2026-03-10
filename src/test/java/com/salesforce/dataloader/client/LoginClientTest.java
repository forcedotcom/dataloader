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
package com.salesforce.dataloader.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for LoginClient API version logic.
 * SOAP login is not supported in API versions >= 65; login falls back to 64.0.
 */
public class LoginClientTest {

    @Test
    public void getApiVersionForLogin_when65_returns64() {
        assertEquals("64.0", LoginClient.getApiVersionForLogin("65.0"));
    }

    @Test
    public void getApiVersionForLogin_when66_returns64() {
        assertEquals("64.0", LoginClient.getApiVersionForLogin("66.0"));
    }

    @Test
    public void getApiVersionForLogin_when70_returns64() {
        assertEquals("64.0", LoginClient.getApiVersionForLogin("70.0"));
    }

    @Test
    public void getApiVersionForLogin_when64_returns64() {
        assertEquals("64.0", LoginClient.getApiVersionForLogin("64.0"));
    }

    @Test
    public void getApiVersionForLogin_when63_returns63() {
        assertEquals("63.0", LoginClient.getApiVersionForLogin("63.0"));
    }

    @Test
    public void getApiVersionForLogin_when50_returns50() {
        assertEquals("50.0", LoginClient.getApiVersionForLogin("50.0"));
    }

    @Test
    public void getApiVersionForLogin_whenNull_returnsNull() {
        assertNull(LoginClient.getApiVersionForLogin(null));
    }

    @Test
    public void getApiVersionForLogin_whenEmpty_returnsEmpty() {
        assertEquals("", LoginClient.getApiVersionForLogin(""));
    }

    @Test
    public void getServicePath_usesProvidedVersion() {
        assertEquals("/services/Soap/u/64.0/", LoginClient.getServicePath("64.0"));
        assertEquals("/services/Soap/u/65.0/", LoginClient.getServicePath("65.0"));
    }

    @Test
    public void getServicePath_withLoginVersion_uses64ForV65Session() {
        String loginVersion = LoginClient.getApiVersionForLogin("65.0");
        assertEquals("/services/Soap/u/64.0/", LoginClient.getServicePath(loginVersion));
    }

    @Test
    public void getServicePath_withLoginVersion_passesThrough64() {
        String loginVersion = LoginClient.getApiVersionForLogin("64.0");
        assertEquals("/services/Soap/u/64.0/", LoginClient.getServicePath(loginVersion));
    }

    @Test
    public void getServicePath_withLoginVersion_passesThrough63() {
        String loginVersion = LoginClient.getApiVersionForLogin("63.0");
        assertEquals("/services/Soap/u/63.0/", LoginClient.getServicePath(loginVersion));
    }

}
