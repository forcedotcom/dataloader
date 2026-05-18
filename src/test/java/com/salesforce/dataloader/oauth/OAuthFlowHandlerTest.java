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
package com.salesforce.dataloader.oauth;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.OAuthServerFlow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Verifies that OAuthFlowHandler works correctly in batch mode
 * by skipping Display.getDefault() calls that would crash without SWT.
 */
public class OAuthFlowHandlerTest {

    private AppUtil.APP_RUN_MODE originalRunMode;

    @Before
    public void setUp() {
        originalRunMode = AppUtil.getAppRunMode();
    }

    @After
    public void tearDown() {
        AppUtil.setAppRunMode(originalRunMode.name().toLowerCase());
    }

    @Test
    public void testHandleOAuthLoginSucceedsInBatchMode() throws Exception {
        AppUtil.setAppRunMode("batch");

        Controller mockController = mock(Controller.class);
        when(mockController.login()).thenReturn(true);
        AppConfig mockAppConfig = mock(AppConfig.class);

        try (MockedConstruction<OAuthServerFlow> ignored = mockConstruction(
                OAuthServerFlow.class,
                (mock, context) -> when(mock.performOAuthFlow()).thenReturn(true))) {

            assertTrue("Run mode should be BATCH", AppUtil.isRunningInBatchMode());

            OAuthFlowHandler handler = new OAuthFlowHandler(
                    mockAppConfig, status -> {}, mockController, () -> {});

            boolean result = handler.handleOAuthLogin();
            assertTrue("OAuth login should succeed in batch mode with fix applied", result);
        }
    }
}
