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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.MappingDialog;

/**
 * Describe your class here.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class MappingDropAdapter extends ViewerDropAdapter {
    public enum MAPPING_CHOICE {
        ADD,
        REPLACE,
        CANCEL;
    }
    private static final String MAPPING_DELIMITER = ", ";
    private final MappingDialog mappingDialog;
    private String currentSforceMappings;
    private String sforceFieldToAddOrReplace;
    private Map.Entry<String, String> dropEntry;
    private Controller controller;

    public MappingDropAdapter(TableViewer arg0, MappingDialog dlg, Controller controller) {
        super(arg0);
        this.mappingDialog = dlg;
        this.controller = controller;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean performDrop(Object arg0) {        
        this.dropEntry = (Entry<String, String>)getCurrentTarget();
        this.currentSforceMappings = this.dropEntry.getValue();
        this.sforceFieldToAddOrReplace = (String)arg0;
        
        if (this.currentSforceMappings == null || this.currentSforceMappings.isBlank()) {
            // if no existing mapping, perform add action
            performDropAction(MAPPING_CHOICE.ADD);
        } else {
            // ask user to add, replace, or cancel action if a mapping exists
            MappingDropActionDialog selectDropActionDlg = new MappingDropActionDialog(
                    this, controller,
                    this.dropEntry.getKey(), 
                    this.currentSforceMappings,
                    this.sforceFieldToAddOrReplace);
            selectDropActionDlg.open();
        }
        return true;
    }
    
    public void performDropAction(MAPPING_CHOICE choice) {
        String newSforceMappings = this.sforceFieldToAddOrReplace;
        if (choice == MAPPING_CHOICE.CANCEL) {
            return;
        }
        if (this.currentSforceMappings != null && !this.currentSforceMappings.isBlank()) {
            if (choice == MAPPING_CHOICE.ADD) {
                // add to existing mappings
                newSforceMappings = this.currentSforceMappings + MAPPING_DELIMITER + this.sforceFieldToAddOrReplace;
            } else { // choice == MAPPING_CHOICE.REPLACE
                //add replaced Salesforce fields in sforceFieldsViewer so that they are available for mapping to other fields
                mappingDialog.replenishMappedSforceFields(this.currentSforceMappings);
            }
        }
        this.dropEntry.setValue(newSforceMappings);
        mappingDialog.getMapper().putMapping(this.dropEntry.getKey(), this.dropEntry.getValue());
        mappingDialog.packMappingColumns();
    }

    @Override
    public boolean validateDrop(Object arg0, int arg1, TransferData arg2) {

        return TextTransfer.getInstance().isSupportedType(arg2);
    }
    
    public MappingDialog getMappingDialog() {
        return this.mappingDialog;
    }

}