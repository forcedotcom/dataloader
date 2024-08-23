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

import java.util.*;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.*;

import com.salesforce.dataloader.ui.MappingDialog;

/**
 * Describe your class here.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class SforceDragListener extends DragSourceAdapter {
    private final StructuredViewer viewer;
    private final MappingDialog mappingDialog;

    public SforceDragListener(StructuredViewer viewer, MappingDialog dialog) {
        this.viewer = viewer;
        this.mappingDialog = dialog;
    }

    /**
     * Method declared on DragSourceListener
     */
    @Override
    public void dragFinished(DragSourceEvent event) {
        if (!event.doit) return;
        //if the gadget was moved, remove it from the source viewer

        try {
            if (event.detail == DND.DROP_MOVE && !mappingDialog.isDragActionCancelled()) {
                IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                for (Iterator<?> it = selection.iterator(); it.hasNext();) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, String> eventElem = (Entry<String, String>)it.next();
                    eventElem.setValue("");
                    mappingDialog.getMapper().removeMapping(eventElem.getKey());
                }
                mappingDialog.packMappingColumns();
                viewer.refresh();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method declared on DragSourceListener
     */
    @Override
    public void dragSetData(DragSourceEvent event) {
        try {
            IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
            @SuppressWarnings("unchecked")
            Map.Entry<String, String> elem = (Entry<String, String>)selection.getFirstElement();

            if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
                event.data = elem.getValue();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method declared on DragSourceListener
     */
    @Override
    public void dragStart(DragSourceEvent event) {
        event.doit = !viewer.getSelection().isEmpty();
    }

}