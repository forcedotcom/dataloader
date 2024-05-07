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

import java.util.*;
import java.util.Map.Entry;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Config.ConfigListener;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.uiActions.*;
import com.salesforce.dataloader.util.AppUtil;

/**
 * The main class for the Loader UI.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class LoaderWindow extends ApplicationWindow {

    //UI actions
    private final TreeMap<Integer, OperationUIAction> operationActionsByIndex;
    private final EnumMap<OperationInfo, Button> operationButtonsByIndex = new EnumMap<OperationInfo, Button>(
            OperationInfo.class);

    private TreeMap<Integer, OperationUIAction> createActionMap(Controller controller) {
        TreeMap<Integer, OperationUIAction> map = new TreeMap<Integer, OperationUIAction>();
        for (OperationInfo info : OperationInfo.values())
            map.put(info.getDialogIdx(), info.createUIAction(controller));

                return map;
    }

    private final ExitUIAction uiActionExit;
    private final ViewCSVFileAction uiActionViewSuccess;
    private final HelpUIAction uiActionHelp;
    private final AdvancedSettingsUIAction uiActionSettings;
    private final LogoutUIAction uiActionLogout;
    private final Controller controller;


    private static LoaderWindow app;
    private Shell shell;

    public LoaderWindow(Controller controller) {
        super(null);

        //need to initialize the Display
        Display.getDefault();

        //Create UI actions
        this.operationActionsByIndex = createActionMap(controller);

        this.uiActionExit = new ExitUIAction();
        this.uiActionViewSuccess = new ViewCSVFileAction(controller);
        this.uiActionSettings = new AdvancedSettingsUIAction(controller);
        this.uiActionHelp = new HelpUIAction(controller);
        this.uiActionLogout = new LogoutUIAction(controller);

        app = this;

        addMenuBar();
        addStatusLine();
        this.controller = controller;

        final ConfigListener listener = new ConfigListener() {
            @Override
            public void configValueChanged(String key, String oldValue, String newValue) {
                if (Config.BULK_API_ENABLED.equals(key)) {
                    boolean isBulkApiEnabled = false;
                    if (newValue != null) isBulkApiEnabled = Boolean.valueOf(newValue);
                    LoaderWindow.this.operationButtonsByIndex.get(OperationInfo.hard_delete).setEnabled(isBulkApiEnabled);
                    LoaderWindow.this.operationActionsByIndex.get(OperationInfo.hard_delete.getDialogIdx()).setEnabled(
                            isBulkApiEnabled);
                    
                    // disable Undelete button and action if bulk API is enabled
                    LoaderWindow.this.operationButtonsByIndex.get(OperationInfo.undelete).setEnabled(!isBulkApiEnabled);
                    LoaderWindow.this.operationActionsByIndex.get(OperationInfo.undelete.getDialogIdx()).setEnabled(
                            !isBulkApiEnabled);
                    getShell().redraw();
                }
            }
        };

        this.controller.getConfig().addListener(listener);

    }

    public void dispose() {
        // make sure configuration gets written
        if(this.controller != null) {
            controller.saveConfig();
        }
    }

    public static LoaderWindow getApp() {
        return app;

    }

    public Controller getController() {
        return controller;
    }

    /**
     * This runs the Loader Window
     */
    public void run() {
        Config config = controller.getConfig();
        if (!config.getBoolean(Config.HIDE_WELCOME_SCREEN)) {
            displayTitleDialog(Display.getDefault(), this.operationActionsByIndex, this.controller.getConfig());
        }
        if (config.getBoolean(Config.SHOW_LOADER_UPGRADE_SCREEN)) {
            displayUpgradeDialog(Display.getDefault());
        }
        setBlockOnOpen(true);
        open();

        Display currentDisplay = Display.getCurrent();
        if (currentDisplay != null) {
            currentDisplay.dispose();
        }
    }
    
    public void updateTitle(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            suffix = "";
            this.uiActionLogout.setEnabled(false);
        } else {
            suffix = " - " + suffix;
            this.uiActionLogout.setEnabled(true);
        }
        this.shell.setText(Labels.getString("LoaderWindow.title") + suffix);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);

        this.shell = shell;
        // Set the title bar text
        updateTitle(null);
        shell.setSize(600, 400);
        Point shellLocation = shell.getLocation();
        if (shellLocation.x < Config.DEFAULT_WIZARD_X_OFFSET) {
            shellLocation.x = Config.DEFAULT_WIZARD_X_OFFSET;
            shell.setLocation(shellLocation);
        }

        shell.setImage(UIUtils.getImageRegistry().get("sfdc_icon"));
    }

    private OperationUIAction getOperationAction(int i) {
        return this.operationActionsByIndex.get(i);
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite comp = createContainer(parent);

        createButtons(comp);

        getStatusLineManager().setMessage(Labels.getString("LoaderWindow.chooseAction"));        
        comp.pack();
        parent.pack();
        return parent;
    }

    private Composite createContainer(Composite parent) {
        Composite comp = new Composite(parent, SWT.BORDER);
        comp.setLayout(new FillLayout(SWT.VERTICAL));
        
        Composite logoComp = new Composite(comp, SWT.NONE);
        logoComp.setLayout(new FillLayout(SWT.HORIZONTAL));
        CLabel label = new CLabel(logoComp, SWT.RIGHT);
        label.setImage(UIUtils.getImageRegistry().get("title_logo"));
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.BEGINNING;
        gridData.grabExcessHorizontalSpace = false;
        gridData.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
        gridData.grabExcessVerticalSpace = true;
        label.setLayoutData(gridData);
        
        label = new CLabel(logoComp, SWT.NONE);
        FontData fd = new FontData("Verdana", 28, SWT.NORMAL);
        label.setFont(new Font(Display.getCurrent(), fd));
        label.setText("data loader");
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.BEGINNING;
        gridData.grabExcessHorizontalSpace = false;
        gridData.verticalAlignment = GridData.VERTICAL_ALIGN_FILL;
        gridData.grabExcessVerticalSpace = true;
        label.setLayoutData(gridData);

        comp.pack();
        return comp;
    }

    private void createButtons(Composite parent) {
        Composite buttons = new Composite(parent, SWT.NONE);
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.wrap = true;
        rowLayout.pack = false;
        rowLayout.justify = false;
        rowLayout.marginLeft = 10;
        rowLayout.marginRight = 10;
        rowLayout.marginTop = 15;
        rowLayout.marginBottom = 15;
        rowLayout.spacing = 5;
        buttons.setLayout(rowLayout);
        // create all the buttons, in order
        for (final OperationInfo info : OperationInfo.ALL_OPERATIONS_IN_ORDER) {
            createOperationButton(buttons, info);
        }
    }

    private void createOperationButton(Composite parent, final OperationInfo info) {

        final Button butt = new Button(parent, SWT.PUSH | SWT.FLAT);
        butt.setText(info.getLabel());
        butt.setEnabled(info.isOperationAllowed(this.controller.getConfig()));
        butt.setImage(info.getUIHelper().getIconImage());
        butt.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                info.createUIAction(getController()).run();
            }
        });
        this.operationButtonsByIndex.put(info, butt);
    }

    private void displayTitleDialog(final Display display, final TreeMap<Integer, OperationUIAction> map,
            final Config cfg) {

        display.asyncExec(new Thread() {
            @Override
            public void run() {
                WelcomeScreenDialog dlg = new WelcomeScreenDialog(display.getActiveShell(), cfg);
                int result = dlg.open();

                for (Entry<Integer, OperationUIAction> ent : map.entrySet()) {
                    if (result == ent.getKey()) ent.getValue().run();
                }
                addMenuBar();
            }
        });
    }
    
    private void displayUpgradeDialog(final Display display) {
        if (AppUtil.DATALOADER_VERSION.equals(AppUtil.getLatestDownloadableDataLoaderVersion())) {
            return; // running app's version matches with downloadable version.
        }
        display.asyncExec(new Thread() {
            @Override
            public void run() {
                LoaderUpgradeDialog dlg = new LoaderUpgradeDialog(display.getActiveShell());
                dlg.open();
                addMenuBar();
            }
        });
    }

    /**
     * Creates the menu for the application
     * 
     * @return MenuManager
     */
    @Override
    protected MenuManager createMenuManager() {
        // Create the main menu
        MenuManager mm = new MenuManager();

        // Create the File menu
        MenuManager fileMenu = new MenuManager(Labels.getString("LoaderWindow.file")); //$NON-NLS-1$
        mm.add(fileMenu);

        // Add the actions to the File menu, in the correct order
        for (OperationInfo info : OperationInfo.ALL_OPERATIONS_IN_ORDER)
            fileMenu.add(getOperationAction(info.getDialogIdx()));

                fileMenu.add(uiActionLogout);
                fileMenu.add(new Separator());
                fileMenu.add(uiActionExit);


                //Create the settings menu
                MenuManager settingsMenu = new MenuManager(Labels.getString("LoaderWindow.settings")); //$NON-NLS-1$
                settingsMenu.add(uiActionSettings);
                mm.add(settingsMenu);

                // Create the View menu
                MenuManager viewMenu = new MenuManager(Labels.getString("LoaderWindow.view")); //$NON-NLS-1$
                mm.add(viewMenu);

                // Add the actions to the View menu
                viewMenu.add(uiActionViewSuccess);

                // Create the Help menu
                MenuManager helpMenu = new MenuManager(Labels.getString("LoaderWindow.help")); //$NON-NLS-1$
                helpMenu.add(uiActionHelp);
                mm.add(helpMenu);

                // Add the actions to the Help menu

                return mm;
    }

}
