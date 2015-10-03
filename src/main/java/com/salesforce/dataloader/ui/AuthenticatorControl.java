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

package com.salesforce.dataloader.ui;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.model.LoginCriteria;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.ws.ConnectionException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import java.util.function.Consumer;

/**
 * AuthenticatorControl is the UI orchestration of logging in.
 */
public class AuthenticatorControl {
    private final Config config;
    private final Controller controller;
    private final String nestedException = "nested exception is:";

    public AuthenticatorControl(Config config, Controller controller) {
        this.config = config;
        this.controller = controller;
    }

    public Config getConfig() {
        return config;
    }

    public void login(LoginCriteria criteria, Consumer<String> messager) {
        config.setValue(Config.USERNAME, criteria.getUserName().trim());
        config.setValue(Config.PASSWORD, criteria.getPassword().trim());
        config.setValue(Config.ENDPOINT, criteria.getInstanceUrl().trim());


        BusyIndicator.showWhile(Display.getDefault(), new Thread() {
            @Override
            public void run() {
                try {
                    messager.accept(Labels.getString("SettingsPage.verifyingLogin"));
                    if (controller.login() && controller.setEntityDescribes()) {
                        messager.accept(Labels.getString("SettingsPage.loginSuccessful"));
                        controller.saveConfig();
                        //loadDataSelectionPage(controller);
                    } else {
                        messager.accept(Labels.getString("SettingsPage.invalidLogin"));
                        //setPageComplete(false);
                    }
                } catch (LoginFault lf ) {
                    messager.accept(Labels.getString("SettingsPage.invalidLogin"));
                    //setPageComplete(false);
                } catch (ApiFault e) {
                    String msg = e.getExceptionMessage();
                    processException(msg);
                    //logger.error(msg);
                } catch (Throwable e) {
                    String msg = e.getMessage();
                    processException(msg);
                    //logger.error(msg);
                    //logger.error("\n" + ExceptionUtil.getStackTraceString(e));
                }
            }

            /**
             * @param msg
             */
            private void processException(String msg) {
                if (msg == null || msg.length() < 1) {
                    messager.accept(Labels.getString("SettingsPage.invalidLogin"));
                } else {
                    int x = msg.indexOf(nestedException);
                    if (x >= 0) {
                        x += nestedException.length();
                        msg = msg.substring(x);
                    }
                    messager.accept(msg.replace('\n', ' ').trim());
                }
//              setPageComplete(false);
            }

        });
    }
}
