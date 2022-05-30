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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Text;

import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.Field;

public class FieldFilter  extends ViewerFilter {
    private Text searchText;

    public FieldFilter(Text search) {
        super();
        this.searchText = search;
    }
    /**
     * Returns whether the specified element passes this filter
     *
     * @param arg0
     *            the viewer
     * @param arg1
     *            the parent element
     * @param arg2
     *            the element
     * @return boolean
     */
    @Override
    public boolean select(Viewer arg0, Object arg1, Object arg2) {

        Field field = (Field)arg2;
        String fieldName = field.getName();
        String fieldLabel = field.getLabel();
        return filterBySearchText(fieldName, fieldLabel);
    }
    
    private boolean filterBySearchText(String fieldName, String fieldLabel) {
        String searchText = this.searchText.getText();
        if (searchText != null && !searchText.isEmpty()) {
            searchText = searchText.toLowerCase();
            if ((fieldName != null && fieldName.toLowerCase().contains(searchText)) 
               || (fieldLabel != null && fieldLabel.toLowerCase().contains(searchText))) {
                return true;
            } else {
                return false;
            }
        } else { // no search text specified
            return true;
        }
    }

}
