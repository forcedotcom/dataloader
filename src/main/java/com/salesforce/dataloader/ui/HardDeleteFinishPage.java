/*
 * Copyright (c) 2011, salesforce.com, inc.
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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.controller.Controller;

public class HardDeleteFinishPage extends FinishPage {

    public HardDeleteFinishPage(Controller controller) {
        super(controller);
    }

    private final AtomicBoolean canFinish = new AtomicBoolean(false);

    private void setCanFinish(boolean selection) {
        this.canFinish.set(selection);
        this.getContainer().updateButtons();
    }

    @Override
    protected void hook_createControl(Composite comp) {
        super.hook_createControl(comp);
        if (getController().getConfig().getOperationInfo() == OperationInfo.hard_delete) {
            Composite terms = new Composite(comp, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            terms.setLayout(layout);
            GridData gd = new GridData();
            gd.horizontalAlignment = GridData.END;
            gd.grabExcessVerticalSpace = true;
            gd.grabExcessHorizontalSpace = true;
            gd.verticalAlignment = GridData.END;
            terms.setLayoutData(gd);

            final BaseWizard wiz = (BaseWizard)getWizard();

            final Label label = new Label(terms, SWT.RIGHT);
            label.setForeground(new Color(label.getDisplay(), 0xff, 0, 0));
            label.setText(wiz.getLabel("finishMessage"));

            final Button b = new Button(terms, SWT.CHECK);
            b.setText(wiz.getLabel("finishMessageConfirm"));
            b.setSelection(this.canFinish.get());
            b.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent se) {
                    setCanFinish(((Button)se.widget).getSelection());
                }
            });
        }
    }

    @Override
    public boolean finishAllowed() {
        return super.finishAllowed() && this.canFinish.get();
    }
}
