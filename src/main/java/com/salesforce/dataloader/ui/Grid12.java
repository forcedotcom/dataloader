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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;

/**
 * Grid12 simplifies the creation of grid based layout
 */
public class Grid12 {
    private Composite composite;
    private final int columnWidth;
    private final GridLayout root;
    private boolean grabExcessHorizontalSpace;
    private boolean grabExcessVerticalSpace;

    public Grid12(Composite composite, int columnWidth, boolean grabExcessVerticalSpace, boolean grabExcessHorizontalSpace) {
        this.composite = composite;
        this.columnWidth = columnWidth;
        this.root = new GridLayout(12, true);
        this.composite.setLayout(root);
        this.grabExcessVerticalSpace = grabExcessVerticalSpace;
        this.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
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
        data.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        data.grabExcessVerticalSpace = grabExcessVerticalSpace;

        return data;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public Label createLabel(int columns, String message) {
        return createLabel(columns, message, SWT.RIGHT | SWT.WRAP);
    }

    public Label createLeftLabel(int columns, String message) {
        return createLabel(columns, message, SWT.LEFT | SWT.WRAP);
    }
    
    public Label createLabel(int columns, String message, int style) {
        Label label = new Label(composite, style);
        label.setText(message);
        label.setLayoutData(createCell(columns));
        applyFormatting(label);
        return label;
    }

    public Label createImage(int columns, Image image) {
        return createImage(columns, image, SWT.RIGHT, SWT.RIGHT);
    }

    public Label createImage(int columns, Image image, int style, int alignment) {
        Label label = new Label(composite, style);
        label.setImage(image);
        label.setLayoutData(createCell(columns));
        label.setAlignment(alignment);
        applyFormatting(label);

        return label;
    }

    public Text createText(int columns) {
        return createText(columns, SWT.BORDER, "");
    }

    public Text createText(int columns, int style, String content) {
        Text text = new Text(composite, style);
        text.setLayoutData(createCell(columns, SWT.FILL));
        text.setText(content);
        applyFormatting(text);
        return text;
    }

    public void createPadding(int columns) {
        Composite composite = new Composite(this.composite, SWT.NONE);
        applyFormatting(composite);
        composite.setLayoutData(createCell(columns));
    }

    public Button createButton(int columns, int style, String content) {
        Button button = new Button(composite, style);
        button.setText(content);
        button.setLayoutData(createCell(columns, SWT.FILL));
        applyFormatting(button);

        return button;
    }

    public Combo createCombo(int columns, int style, ArrayList<String> labels) {
        Combo combo = new Combo(composite, style);
        combo.setLayoutData(createCell(columns));
        for (String label: labels) {
            combo.add(label);
        }
        applyFormatting(combo);

        return combo;
    }

    public void hide(Control control) {
        control.setVisible(false);
        ((GridData) control.getLayoutData()).exclude = true;
    }

    public void show(Control control) {
        control.setVisible(true);
        ((GridData) control.getLayoutData()).exclude = false;
    }

    public void pack() {
        composite.pack();
    }

    private void applyFormatting(Control control) {
        //use this to apply standard formatting

        //the below call is useful for debugging grid layout issues
        //control.setBackground(new Color(composite.getDisplay(),new Random().nextInt(255),new Random().nextInt(255),new Random().nextInt(255)));
    }
}
