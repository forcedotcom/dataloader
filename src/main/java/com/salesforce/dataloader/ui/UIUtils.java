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
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.ui.extraction.ExtractionDataSelectionPage;
import com.sforce.soap.partner.LimitInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class UIUtils {
    private static ImageRegistry image_registry;
    private static Logger logger = LogManager.getLogger(UIUtils.class);


    public static boolean isValidHttpsUrl(String url) {
        try {
            // check if it is a valid url
            URI uri = new URL(url).toURI();
            // check if it is https protocol
            return "https".equalsIgnoreCase(uri.getScheme());
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void validateHttpsUrlAndThrow(String url) {
        if (!isValidHttpsUrl(url)) {
            throw new RuntimeException("Dataloader only supports server URL that uses https protocol:" + url);
        }
    }

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
    
    public static void updateWizardPageDescription(WizardPage page, PartnerClient partnerClient) {
        if (partnerClient == null) {
            return;
        }
        String currentMessage = page.getDescription();
        LimitInfo apiLimitInfo = partnerClient.getAPILimitInfo();
        String apiInfoStr = "\n    "
                + Labels.getFormattedString("Operation.apiVersion", partnerClient.getAPIVersion());
        if (apiLimitInfo != null) {
            apiInfoStr = "\n    "
                    + Labels.getFormattedString("Operation.currentAPIUsage", apiLimitInfo.getCurrent())
                    + "\n    "
                    + Labels.getFormattedString("Operation.apiLimit", apiLimitInfo.getLimit())
                    + apiInfoStr;
        }
        logger.debug(apiInfoStr);
        page.setDescription(currentMessage + "\n" + apiInfoStr);
    }
}
