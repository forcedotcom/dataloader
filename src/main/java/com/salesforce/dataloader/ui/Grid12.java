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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Grid12 simplifies the creation of grid based layout
 */
public class Grid12 {
    private Composite composite;
    private final int columnWidth;
    private final int cellHeight;
    private final GridLayout root;

    public Grid12(Composite composite, int columnWidth){
        this(composite, columnWidth, -1);
    }
    public Grid12(Composite composite, int columnWidth, int cellHeight){
        this.composite = composite;
        this.columnWidth = columnWidth;
        this.cellHeight = cellHeight;
        this.root = new GridLayout(12, true);
        this.composite.setLayout(root);
    }

    public GridLayout getRoot() {
        return root;
    }

    public GridData createCell(int columnSpan){
        return createCell(columnSpan, SWT.FILL);
    }

    public GridData createCell(int columnSpan, int horizontalAlignment) {
        GridData data = new GridData();
        data.horizontalSpan = columnSpan;
        data.widthHint = columnSpan * this.columnWidth;
        data.horizontalAlignment = horizontalAlignment;
        data.heightHint = cellHeight;

        return data;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public Label createLabel(int columns, String message) {
        Label label = new Label(composite, SWT.RIGHT);
        label.setText(message);
        label.setLayoutData(createCell(columns));

        return label;
    }

    public Text createText(int columns) {
        return createText(columns, SWT.BORDER, "");
    }

    public Text createText(int columns, int style, String content) {
        Text text = new Text(composite, style);
        text.setLayoutData(createCell(columns, SWT.FILL));
        text.setText(content);

        return text;
    }

    public void createPadding(int columns) {
        new Composite(composite, SWT.NONE).setLayoutData(createCell(columns));
    }

    public Button createButton(int columns, int style, String content) {
        Button button = new Button(composite, style);
        button.setText(content);
        button.setLayoutData(createCell(columns, SWT.FILL));

        return button;
    }

    public CCombo createCombo(int columns, int style, String... labels) {
        CCombo combo = new CCombo(composite, style);
        combo.setEditable(false);
        combo.setLayoutData(createCell(columns));
        for (String label: labels) {
            combo.add(label);
        }

        return combo;
    }
}
