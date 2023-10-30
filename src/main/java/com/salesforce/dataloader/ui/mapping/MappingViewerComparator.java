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

package com.salesforce.dataloader.ui.mapping;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.salesforce.dataloader.ui.MappingDialog;

/**
 * This class implements the sorting for the SforceTable
 */
public class MappingViewerComparator extends ViewerComparator {
    private static final int ASCENDING = 0;
    private static final int DESCENDING = 1;

    private int column = 0;
    private int direction = ASCENDING;

    /**
     * Does the sort. If it's a different column from the previous sort, do an ascending sort. If it's the same column
     * as the last sort, toggle the sort direction.
     *
     * @param column
     */
    public void doSort(int column) {
        if (column == this.column) {
            // Same column as last sort; toggle the direction
            direction = 1 - direction;
        } else {
            // New column; do an ascending sort
            this.column = column;
            direction = ASCENDING;
        }
    }

    private int safeCompare(String s1, String s2)
    {
        if(s1 == null && s2 == null)
            return 0;
        if (s1 == null)
            return -1;
        if (s2 == null)
            return 1;
        return s1.compareToIgnoreCase(s2);
    }

    /**
     * Compares the object for sorting
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int rc = 0;
        @SuppressWarnings("unchecked")
        Entry<String, String> m1 = (Entry<String, String>)e1;
        @SuppressWarnings("unchecked")
        Entry<String, String> m2 = (Entry<String, String>)e2;

        // Determine which column and do the appropriate sort
        switch (column) {
        case MappingDialog.MAPPING_DAO:
            rc =safeCompare(m1.getKey(), m2.getKey());
            break;
        case MappingDialog.MAPPING_SFORCE:
            rc = safeCompare(m1.getValue(), m2.getValue());
            break;
        }

        // If descending order, flip the direction
        if (direction == DESCENDING) rc = -rc;

        return rc;
    }
}