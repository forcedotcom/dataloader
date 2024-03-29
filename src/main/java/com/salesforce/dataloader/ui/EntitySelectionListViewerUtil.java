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

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.ui.entitySelection.EntityContentProvider;
import com.salesforce.dataloader.ui.entitySelection.EntityFilter;
import com.salesforce.dataloader.ui.entitySelection.EntityLabelProvider;
import com.salesforce.dataloader.ui.entitySelection.EntityViewerComparator;

public class EntitySelectionListViewerUtil {
    private static final String PROPERTIES_PREFIX_STR = "DataSelectionPage";
    
    public static ListViewer getEntitySelectionListViewer(Composite comp, Config config) {
        String propertiesPrefixStr = PROPERTIES_PREFIX_STR;
        Label label = new Label(comp, SWT.RIGHT);
        label.setText(Labels.getString(propertiesPrefixStr + ".selectObject")); //$NON-NLS-1$
        GridData data = new GridData();
        label.setLayoutData(data);

        // Add a checkbox to toggle filter
        Button filterAll = new Button(comp, SWT.CHECK);
        filterAll.setText(Labels.getString(propertiesPrefixStr + ".showAll")); //$NON-NLS-1$
        filterAll.setToolTipText(Labels.getString(propertiesPrefixStr + ".showAllToolTip"));

        Text search = new Text(comp, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        search.setLayoutData(data);
        
        EntityFilter filter = new EntityFilter(search, filterAll);
        final ListViewer listViewer = new ListViewer(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        listViewer.setContentProvider(new EntityContentProvider());
        listViewer.setLabelProvider(new EntityLabelProvider());
        listViewer.setInput(null);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = OperationPage.getShellSizeAtLogin().x / 3;
        listViewer.getControl().setLayoutData(data);
        listViewer.addFilter(filter);
        listViewer.setComparator(new EntityViewerComparator());
        
        filterAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                listViewer.refresh();
            }
        });

        search.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                listViewer.refresh();
            }
        });
        
        search.addListener(SWT.KeyUp, new Listener() {
            public void handleEvent(Event e) {
                listViewer.refresh();
            }
        });
        
        return listViewer;
    }
}
