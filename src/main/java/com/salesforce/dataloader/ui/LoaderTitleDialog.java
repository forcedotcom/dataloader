/*
 * Copyright (c) 2012, salesforce.com, inc.
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;

/**
 * Splash screen for the loader.
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class LoaderTitleDialog extends LoaderTitleAreaDialog {

    private final Config config;

    /**
     * MyTitleAreaDialog constructor
     * 
     * @param shell
     *            the parent shell
     */
    public LoaderTitleDialog(Shell activeShell, Config cfg) {
        super(activeShell);
        this.config = cfg;
    }

    /**
     * Creates the dialog's contents
     * 
     * @param parent
     *            the parent composite
     * @return Control
     */
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        // Set the title
        setTitle(Labels.getString("TitleDialog.title")); //$NON-NLS-1$

        // Set the message
        setMessage(Labels.getString("TitleDialog.messageLineOne") //$NON-NLS-1$
                + System.getProperty("line.separator") + Labels.getString("TitleDialog.messageLineTwo")); //$NON-NLS-1$ //$NON-NLS-2$

        setBannerImage(UIUtils.getImageRegistry().get("logo"));

        // Set the image
        setTitleImage(UIUtils.getImageRegistry().get("splashscreens")); //$NON-NLS-1$

        return contents;
    }

    /**
     * Creates the gray area
     * 
     * @param parent
     *            the parent composite
     * @return Control
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);

        return composite;
    }

    /**
     * Creates the buttons for the button bar
     * 
     * @param parent
     *            the parent composite
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create all the buttons, in order
        for (OperationInfo info : OperationInfo.ALL_OPERATIONS_IN_ORDER) {
            final Button butt = createButton(parent, info.getDialogIdx(), info.getLabel(), false);
            butt.setEnabled(info.isOperationAllowed(this.config));
            Image img = info.getIconImage();
            butt.setImage(img);
            GridData gd = (GridData)butt.getLayoutData();
            gd.grabExcessHorizontalSpace = true;
            gd.widthHint += img.getImageData().width;
            butt.setLayoutData(gd);
        }
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
    }

    /**
     * Sets the button behavior
     */
    @Override
    protected void buttonPressed(int buttonID) {
        setReturnCode(buttonID);
        close();
    }
}
