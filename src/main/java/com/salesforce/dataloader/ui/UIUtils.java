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

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.AppConfig;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import java.util.Arrays;
import java.util.List;

public class UIUtils {
    private static ImageRegistry image_registry;

    public static synchronized ImageRegistry getImageRegistry() {
        if (image_registry == null) {

            image_registry = new ImageRegistry();

            Class<?> baseClass = LoaderWindow.class;

            image_registry.put("sfdc_icon",
                    ImageDescriptor.createFromURL(baseClass.getClassLoader().getResource("img/icons/icon_32x32.png")));

            image_registry.put("logo",
                    ImageDescriptor.createFromURL(baseClass.getClassLoader().getResource("img/icons/icon_128x128.png")));
            image_registry.put("title_logo",
                    ImageDescriptor.createFromURL(baseClass.getClassLoader().getResource("img/dataloader-title-logo.png")));
            image_registry.put("splashscreens",
                    ImageDescriptor.createFromURL(baseClass.getClassLoader().getResource("img/icons/icon_256x256.png")));

            for (OperationInfo info : OperationInfo.values()) {
                if (image_registry.get(info.getIconName()) == null)
                    image_registry.put(info.getIconName(),
                            ImageDescriptor.createFromURL(baseClass.getClassLoader().getResource(info.getIconLocation())));
            }

            image_registry.put("downArrow",
                    ImageDescriptor.createFromURL(baseClass.getClassLoader().getResource("img/downArrow.gif")));

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

    public static int infoMessageBox(Shell shell, String message) {
        return messageBox(shell, Labels.getString("UI.message"), SWT.OK | SWT.ICON_INFORMATION, message);
    }

    private static int messageBox(Shell shell, String title, int uiProps, String message) {
        MessageBox mb = new MessageBox(shell, uiProps);
        if (title != null && title.length() > 0) {
            mb.setText(title);
        }
        mb.setMessage(String.valueOf(message));
        return mb.open();
    }

    /**
     * @return Array of combo items
     */
    public static String[] setComboItems(Combo combo, List<String> itemList, String defaultItemText) {
        String[] itemArray = itemList.toArray(new String[itemList.size()]);
        Arrays.sort(itemArray);
        combo.setItems(itemArray);
        if (defaultItemText != null && defaultItemText.length() > 0) {
            combo.setText(defaultItemText);
        }
        return itemArray;
    }
    
    
    public static int getControlWidth(Control control) {
        GC gc = new GC(control);
        gc.setFont(control.getFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        return org.eclipse.jface.dialogs.Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
    }
    
    public static Rectangle getPersistedWizardBounds(AppConfig appConfig) {
        int xOffset = AppConfig.DEFAULT_WIZARD_X_OFFSET;
        int yOffset = AppConfig.DEFAULT_WIZARD_Y_OFFSET;
        int width = AppConfig.DEFAULT_WIZARD_WIDTH;
        int height = AppConfig.DEFAULT_WIZARD_HEIGHT;
        if (appConfig != null) {
            try {
                xOffset = appConfig.getInt(AppConfig.WIZARD_X_OFFSET);
                yOffset = appConfig.getInt(AppConfig.WIZARD_Y_OFFSET);
                width = appConfig.getInt(AppConfig.WIZARD_WIDTH);
                height = appConfig.getInt(AppConfig.WIZARD_HEIGHT);
            } catch (Exception ex) {
                // no op
            }
        }
        return new Rectangle(xOffset, yOffset, width, height);
    }
    
    public static void setTableColWidth(Table table) {
        if (table == null) {
            return;
        }
        int numCols = table.getColumnCount();
        if (numCols == 0) {
            return;
        }
        Rectangle currentClientAreaBounds = table.getClientArea();
        int desiredColWidth = currentClientAreaBounds.width / numCols;
        if (desiredColWidth > 0) {
            int currentTotalColWidth = 0;
            for (int i=0; i < numCols; i++) {
                currentTotalColWidth += table.getColumn(i).getWidth();
            }
            if (currentTotalColWidth > currentClientAreaBounds.width) {
                return; // do not change column width if current dialog width is less than total column width
            }

            for (int i=0; i < numCols; i++) {
                if (table.getColumn(i).getWidth() < desiredColWidth) {
                    table.getColumn(i).setWidth(desiredColWidth);
                }
            }
        }
    }
}
