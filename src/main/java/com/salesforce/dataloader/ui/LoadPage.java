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

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;

/**
 * This is the base class for the LoadWizard ui pages. Allows navigation to be done dynamically by forcing setupPage to
 * be implemented by each wizard page
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public abstract class LoadPage extends OperationPage {

    /**
     * @param pageName
     * @param title
     * @param titleImage
     * 
     */


    public LoadPage(String name, Controller controller) {
        super(name, controller);
    }
    
    @Override
    protected String getConfigInfo() {
        return Labels.getString("LoadPage.importBatchSize")
        + " "
        + controller.getAppConfig().getImportBatchSize()
        + "    "
        + Labels.getString("AdvancedSettingsDialog.uiLabel." + AppConfig.PROP_LOAD_ROW_TO_START_AT)
        + " "
        + controller.getAppConfig().getString(AppConfig.PROP_LOAD_ROW_TO_START_AT); //$NON-NLS-1$
    }

    /*
     * Common code for getting the next page
     */
    @Override
    public LoadPage getNextPage() {
        LoadPage nextPage = (LoadPage)super.getNextPage();
        if( nextPage != null && nextPage.setupPage()) {
            return nextPage;
        } else {
            return this;
        }
    }
}
