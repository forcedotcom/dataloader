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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;

public class UIUtils {
    private static ImageRegistry image_registry;

    public static URL newURL(String url_name) {
        try {
            return new URL(url_name);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL " + url_name, e);
        }
    }

    public static synchronized ImageRegistry getImageRegistry() {
        if (image_registry == null) {

            image_registry = new ImageRegistry();

            Class<?> baseClass = LoaderWindow.class;

            image_registry.put("sfdc_icon", ImageDescriptor.createFromFile(baseClass,
                    "img/icons/icon_sforceDL16x16.bmp"));

            image_registry.put("logo", ImageDescriptor.createFromFile(baseClass, "img/appExch_dataload_logo.gif"));
            image_registry.put("splashscreens", ImageDescriptor.createFromFile(baseClass, "img/splashscreens.gif"));

            for (OperationInfo info : OperationInfo.values()) {
                if (image_registry.get(info.getIconName()) == null)
                    image_registry.put(info.getIconName(), ImageDescriptor.createFromFile(baseClass, info
                            .getIconLocation()));
            }

            image_registry.put("downArrow", ImageDescriptor.createFromFile(baseClass, "img/downArrow.gif"));
        }
        return image_registry;
    }

    public static Color getDefaultTitleBackGround() {
        return Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
    }

    public static int warningConfMessageBox(Shell shell, String message) {
        return messageBox(shell,
                Labels.getString("UI.warning"), SWT.ICON_WARNING | SWT.YES | SWT.NO, String.valueOf(message)); //$NON-NLS-1$
    }

    public static int errorMessageBox(Shell shell, Throwable e) {
        return errorMessageBox(shell, e.getMessage());
    }

    public static int errorMessageBox(Shell shell, String message) {
        return messageBox(shell, Labels.getString("UI.error"), SWT.OK | SWT.ICON_ERROR, String.valueOf(message)); //$NON-NLS-1$
    }

    private static int messageBox(Shell shell, String title, int uiProps, String message) {
        MessageBox mb = new MessageBox (shell, uiProps);
        if(title != null && title.length() > 0) {
            mb.setText(title);
        }
        mb.setMessage(String.valueOf(message));
        return mb.open();
    }

    /**
     * @param combo
     * @param itemList
     * @return Array of combo items
     */
    public static String[] setComboItems(Combo combo, List<String> itemList, String defaultItemText) {
        String[] itemArray = itemList.toArray(new String[itemList.size()]);
        Arrays.sort(itemArray);
        combo.setItems(itemArray);
        if(defaultItemText != null && defaultItemText.length() > 0) {
            combo.setText(defaultItemText);
        }
        return itemArray;
    }
}
