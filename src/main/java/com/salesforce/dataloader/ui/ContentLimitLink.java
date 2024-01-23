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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.util.AppUtil;

/**
 * 
 */
public class ContentLimitLink extends Link {

    /**
     * @param parent
     * @param style
     */
    private Controller controller;
    
    public ContentLimitLink(Composite parent, int style, Controller controller) {
        super(parent, style);
        this.controller = controller;
        this.setText(Labels.getString("UI.contentUploadLimit"));
        this.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                UIUtils.openURL(e.text);
            }
          });
        
        // based on https://www.informit.com/articles/article.aspx?p=354574&seqNum=3
        this.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                setVisible();
            }
          });
    }
    
    public void setVisible() {
        Config config = controller.getConfig();
        String operation = config.getString(Config.OPERATION);
        boolean isVisible = AppUtil.isContentSObject(
                config.getString(Config.ENTITY))
                && operation != null
                && ("insert".equalsIgnoreCase(operation)
                    || "update".equalsIgnoreCase(operation)
                    || "upsert".equalsIgnoreCase(operation));
        setVisible(isVisible);
    }
    
    // Based on answer at https://stackoverflow.com/questions/4264983/why-is-subclassing-not-allowed-for-many-of-the-swt-controls
    @Override
    protected void checkSubclass() {
        //  allow subclass
    }

}
