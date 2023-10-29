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

package com.salesforce.dataloader.ui.uiActions;

import org.eclipse.jface.action.Action;

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.*;
import com.salesforce.dataloader.util.AppUtil;

/**
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class HelpUIAction extends Action {
    private Controller controller;

    public HelpUIAction(Controller controller) {
        super(Labels.getString("HelpUIAction.menuText"), null);  //$NON-NLS-1$
        setToolTipText(Labels.getString("HelpUIAction.tooltip"));  //$NON-NLS-1$

        this.controller = controller;

    }

    @Override
    public void run() {
        HyperlinkDialog dlg = new HyperlinkDialog(LoaderWindow.getApp().getShell(), controller);
        dlg.setText(Labels.getString("HelpUIAction.dlgTitle")); //$NON-NLS-1$
        dlg.setMessage(
                getLoaderUpgradeMessage()
                + System.getProperty("line.separator")
                + Labels.getString("HelpUIAction.dlgMsg")
                + System.getProperty("line.separator")
                + Labels.getString("HelpUIAction.dlgLinkText")
            ); //$NON-NLS-1$
        dlg.setBoldMessage(Labels.getString("HelpUIAction.msgHeader")); //$NON-NLS-1$
        dlg.open();
    }
    
    private String getLoaderUpgradeMessage() {
        String upgradeMsg = Labels.getString("HelpUIAction.latestVersion");
        if (!AppUtil.DATALOADER_VERSION.equals(AppUtil.getLatestDownloadableDataLoaderVersion())) {
            upgradeMsg = 
                    Labels.getFormattedString("LoaderDownloadDialog.messageLineOne", 
                                            new String[] {AppUtil.getLatestDownloadableDataLoaderVersion(), 
                                                          AppUtil.DATALOADER_DOWNLOAD_URL});
        }
        upgradeMsg += System.getProperty("line.separator") + System.getProperty("line.separator");
        return upgradeMsg;
    }

}