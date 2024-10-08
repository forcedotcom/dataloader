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
import com.salesforce.dataloader.util.OAuthBrowserLoginRunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.io.UnsupportedEncodingException;

/**
 * the oauth token flow. this is normally used for client to server where the client is not a secured environment
 * as it does not involve a secret. We use this as our standard login for SF oauth. The disadvantage to this flow is
 * it prompts for authentication and authorization everytime.
 */
public class OAuthLoginFromBrowserFlow extends Dialog {
    protected static Logger logger = LogManager.getLogger(OAuthLoginFromBrowserFlow.class);
    protected final AppConfig appConfig;

    public OAuthLoginFromBrowserFlow(Shell parent, AppConfig appConfig) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.FILL);
        this.appConfig = appConfig;
    }
    
    public boolean open() throws UnsupportedEncodingException {    	
    	final String verificationURLStr;
    	final OAuthBrowserLoginRunner loginRunner;
    	try {
    	    loginRunner = new OAuthBrowserLoginRunner(appConfig, true);
    	    verificationURLStr = loginRunner.getVerificationURLStr();
    	} catch (Exception ex) {
    	    logger.error(ex.getMessage());
    	    return false;
    	}
	    // Create the dialog window
        Display display = getParent().getDisplay();
        int style = getParent().getStyle();
        Shell shell = new Shell(getParent(), style | SWT.APPLICATION_MODAL);
        Font f = shell.getFont();
        FontData[] farr = f.getFontData();
        FontData fd = farr[0];
        fd.setStyle(SWT.BOLD);
        Font boldFont = new Font(Display.getCurrent(), fd);
        shell.setText(Labels.getString("OAuthInBrowser.title"));

        Composite container = new Composite(shell, SWT.NONE);
        GridLayout containerLayout = new GridLayout(1, false);
        container.setLayout(containerLayout);
        shell.setLayout(new FillLayout());

        Composite infoComp = new Composite(container, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_CENTER);
        data.heightHint = 150;
        data.widthHint = 600;
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        infoComp.setLayout(layout);
        infoComp.setLayoutData(data);
        infoComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        
        Label label = new Label(infoComp, SWT.RIGHT);
        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        label.setText(Labels.getString("OAuthInBrowser.authStep1Title"));
        label.setFont(boldFont);

        label = new Label(infoComp, SWT.WRAP);
        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText(Labels.getString("OAuthInBrowser.authStep1Content"));

        label = new Label(infoComp, SWT.WRAP);
        label = new Label(infoComp, SWT.WRAP);

        label = new Label(infoComp, SWT.RIGHT);
        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        label.setText(Labels.getString("OAuthInBrowser.authStep2Title"));
        label.setFont(boldFont);

        label = new Label(infoComp, SWT.WRAP);
        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText(Labels.getFormattedString("OAuthInBrowser.authStep2Content", loginRunner.getUserCode()));

        label = new Label(infoComp, SWT.WRAP);
        label = new Label(infoComp, SWT.WRAP);

        label = new Label(infoComp, SWT.RIGHT);
        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        label.setText(Labels.getString("OAuthInBrowser.authStep3Title"));
        label.setFont(boldFont);

        label = new Label(infoComp, SWT.WRAP);
        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText(Labels.getString("OAuthInBrowser.authStep3Content"));

        
        Composite contentComp = new Composite(container, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        contentComp.setLayoutData(data);
        GridLayout contentLayout = new GridLayout(3, false);
        contentLayout.verticalSpacing = 10;
        contentComp.setLayout(contentLayout);

        label = new Label(contentComp, SWT.RIGHT);
        label.setFont(new Font(Display.getCurrent(), fd));
        label.setText(Labels.getString("OAuthInBrowser.verificationURL"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        label.setLayoutData(data);
        Link link = new Link(contentComp, SWT.END);
        link.setText("<a href=\""+verificationURLStr+"\">"+verificationURLStr+"</a>");
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                URLUtil.openURL(e.text);
            }
          });
        final Clipboard clipboard = new Clipboard(display);
        Button copy = new Button(contentComp, SWT.PUSH);
        copy.setText(Labels.getString("OAuthInBrowser.copyToClipboardButton"));
        copy.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event e) {
            String textData = verificationURLStr;
            TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents(new Object[] { textData },
                new Transfer[] { textTransfer });
          }
        });
        

        shell.pack();
        shell.open();
        

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
            if (loginRunner.isLoginProcessCompleted()) {
            	shell.close();
            	break;
            }
        }
        return loginRunner.getLoginStatus() == OAuthBrowserLoginRunner.LoginStatus.SUCCESS;
    }
}
