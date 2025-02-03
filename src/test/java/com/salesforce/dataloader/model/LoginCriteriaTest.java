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

import org.junit.Test;
import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.config.AppConfig;

import static org.junit.jupiter.api.Assertions.*;

public class LoginCriteriaTest extends ConfigTestBase {

    @Test
    public void testGetAndSetEnvironment() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
        criteria.setEnvironment("Production");
        assertEquals("Production", criteria.getEnvironment());
    }

    @Test
    public void testGetAndSetInstanceUrl() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
        criteria.setInstanceUrl("https://login.salesforce.com");
        assertEquals("https://login.salesforce.com", criteria.getInstanceUrl());
    }

    @Test
    public void testGetAndSetUserName() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
        criteria.setUserName("testuser");
        assertEquals("testuser", criteria.getUserName());
    }

    @Test
    public void testGetAndSetPassword() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
        criteria.setPassword("password123");
        assertEquals("password123", criteria.getPassword());
    }

    @Test
    public void testGetAndSetSessionId() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.SessionIdLogin);
        criteria.setSessionId("session123");
        assertEquals("session123", criteria.getSessionId());
    }

    @Test
    public void testUpdateConfigForUsernamePasswordLogin() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
        criteria.setUserName("testuser");
        criteria.setPassword("password123");
        criteria.setEnvironment("Production");

        AppConfig appConfig = super.getController().getAppConfig();
        criteria.updateConfig(appConfig);

        assertFalse(appConfig.getBoolean(AppConfig.PROP_SFDC_INTERNAL_IS_SESSION_ID_LOGIN));
        assertEquals("testuser", appConfig.getString(AppConfig.PROP_USERNAME));
        assertEquals("password123", appConfig.getString(AppConfig.PROP_PASSWORD));
        assertEquals("Production", appConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT));
    }

    @Test
    public void testUpdateConfigForSessionIdLogin() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.SessionIdLogin);
        criteria.setUserName("testuser");
        criteria.setSessionId("session123");
        criteria.setEnvironment("Production");

        AppConfig appConfig = super.getController().getAppConfig();
        criteria.updateConfig(appConfig);

        assertTrue(appConfig.getBoolean(AppConfig.PROP_SFDC_INTERNAL_IS_SESSION_ID_LOGIN));
        assertEquals("testuser", appConfig.getString(AppConfig.PROP_USERNAME));
        assertEquals("session123", appConfig.getString(AppConfig.PROP_SFDC_INTERNAL_SESSION_ID));
        assertEquals("Production", appConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT));
    }

    @Test
    public void testUpdateConfigForOAuthLogin() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.OAuthLogin);
        criteria.setEnvironment("Production");

        AppConfig appConfig = super.getController().getAppConfig();
        criteria.updateConfig(appConfig);

        assertEquals("Production", appConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT));
    }

    @Test
    public void testEmptyUserName() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
        criteria.setUserName("");
        assertEquals("", criteria.getUserName());
    }

    @Test
    public void testNullPassword() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
        criteria.setPassword(null);
        assertNull(criteria.getPassword());
    }
}