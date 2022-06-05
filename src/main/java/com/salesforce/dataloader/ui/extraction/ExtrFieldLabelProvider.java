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

package com.salesforce.dataloader.ui.extraction;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Control;

import com.sforce.soap.partner.Field;

/**
 * 
 */
public class ExtrFieldLabelProvider extends CellLabelProvider {

    public ExtrFieldLabelProvider() {
    }

   
    /**
     * Adds a listener
     * 
     * @param arg0 the listener
     */
    @Override
    public void addListener(ILabelProviderListener arg0) {
        // Throw it away
        ILabelProviderListener listener = arg0;
    }

    /**
     * Dispose any created resources
     */
    @Override
    public void dispose() {
        // Dispose the image
    }

    /**
     * Returns whether the specified property, if changed, would affect the label
     * 
     * @return boolean
     */
    @Override
    public boolean isLabelProperty(Object arg0, String arg1) {
        return false;
    }

    /**
     * Removes the specified listener
     * 
     * @param arg0 the listener
     */
    @Override
    public void removeListener(ILabelProviderListener arg0) {
        // Do nothing
    }

    @Override
    public void update(ViewerCell cell) {
        Field field = (Field)cell.getElement();
        String fieldTypeStr = getFieldTypeStr(field);
        cell.setText(field.getName() + " (" + fieldTypeStr + ")");
        String tooltipStr = field.getInlineHelpText();
        if (tooltipStr == null) {
            tooltipStr = fieldTypeStr;
        } else {
            tooltipStr = fieldTypeStr + "\n" + tooltipStr;
        }
        Control control = cell.getControl();
        control.getParent().setToolTipText(tooltipStr);
        for (Control child : control.getParent().getChildren()) {
            child.setToolTipText(tooltipStr);
        }
    }
    
    private String getFieldTypeStr(Field field) {
        String fieldTypeStr = field.getType().toString();
        if (fieldTypeStr.startsWith("_")) {
            fieldTypeStr = fieldTypeStr.substring(1);
        }
        if ("string".equalsIgnoreCase(fieldTypeStr) || "textarea".equalsIgnoreCase(fieldTypeStr)) {
            fieldTypeStr = fieldTypeStr
                    + ", "
                    + field.getLength();
        }
        return fieldTypeStr;
    }
}