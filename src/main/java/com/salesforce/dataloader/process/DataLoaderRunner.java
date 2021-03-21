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
package com.salesforce.dataloader.process;

import com.salesforce.dataloader.client.HttpClientTransport;

/**
 * @author Lexi Viripaeff
 * @input DataLoaderRunner -------------- @ * ----------------
 */

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.ui.UIUtils;

import java.io.IOException;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DataLoaderRunner extends Thread {

    private static final String UI = "ui";
    private static final String RUN_MODE = "run.mode";
    private static final String RUN_MODE_BATCH = "batch";

    private static boolean isBatchMode(String[] args) {
        Map<String, String> argNameValuePair = Controller.getArgMapFromArgArray(args);
        
        return argNameValuePair.containsKey(RUN_MODE) ?
                RUN_MODE_BATCH.equalsIgnoreCase(argNameValuePair.get(RUN_MODE)) : false;
    }
    
    public void run() {
        // called just before the program closes
        try {
            HttpClientTransport.cleanup();
        } catch (IOException ex) {
            // ignore
        }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new DataLoaderRunner());
        Controller.setConfigDir(args);
        if (isBatchMode(args)) {
            ProcessRunner.runBatchMode(args);
        } else {
            /* Run in the UI mode, get the controller instance with batchMode == false */
            try {
                Controller controller = Controller.getInstance(UI, false, args);
                controller.createAndShowGUI();
            } catch (ControllerInitializationException e) {
                UIUtils.errorMessageBox(new Shell(new Display()), e);
            }
        }
    }
}
