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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;

/**
* This is the base class for the LoadWizard and ExtractionWizard ui pages. Allows navigation to be done dynamically by forcing setupPage to
* be implemented by each wizard page
* 
*/
public abstract class OperationPage extends WizardPage {

   /**
    * @param pageName
    * @param title
    * @param titleImage
    * 
    */
   protected final Controller controller;
   protected final Logger logger;
   public static final int SHELL_X_OFFSET = 50;
   public static final int SHELL_Y_OFFSET = 0;

   public OperationPage(String name, Controller controller) {
       super(name);
       this.setTitle(Labels.getString(getClass().getSimpleName() + ".title"));
       this.setImageDescriptor(UIUtils.getImageRegistry().getDescriptor("logo"));
       this.controller = controller;
       this.logger = LogManager.getLogger(this.getClass());
       this.setPageComplete(false);
   }

   public boolean setupPage() {
       // Set the description
       String description = Labels.getString(this.getClass().getSimpleName() + ".description");
       this.setDescription(description);
       Point shellLocation = new Point(SHELL_X_OFFSET, SHELL_Y_OFFSET);
       this.getShell().setLocation(shellLocation);
       initializeCachedShellSize();

       boolean success = true;
       if (this.controller.isLoggedIn()) {
           success = setupPagePostLogin();
           if (success) {
               String message = this.getConfigInfo() + "\n" + this.controller.getAPIInfo();
               this.setMessage(message);
               Control[] controls = this.getShell().getChildren();
               for (Control control : controls) {
                   if (control instanceof Composite) {
                       GridData data = new GridData();
                       data.verticalSpan = GridData.FILL_VERTICAL;
                       data.grabExcessVerticalSpace = true;
                       control.setLayoutData(data);

                       controls = ((Composite)control).getChildren();
                       // get the first Composite among children
                       break;
                   }
               }

               for (Control ctl : controls) {
                   // Tracer to see if extra vertical gap is removed
                   // ctl.setBackground(new Color(200, 0, 0));
                   if (ctl instanceof Composite) {
                       Composite comp = (Composite)ctl;
                       Control[] children = comp.getChildren();
                       for (Control child : children) {
                           GridData data = (GridData) child.getLayoutData();
                           if (data == null) {
                               data = new GridData();
                           }
                           data.verticalSpan = GridData.FILL_VERTICAL;
                           data.grabExcessVerticalSpace = true;
                           child.setLayoutData(data);
                       }
                       break;
                   }
               }
           }
       }

       return success;
   }
   
   public abstract void setPageComplete();
   protected abstract boolean setupPagePostLogin();
   protected abstract String getConfigInfo();

   /**
    * Need to subclass this function to prevent the getNextPage() function being called before the button is clicked.
    */
   @Override
   public boolean canFlipToNextPage() {
       return isPageComplete();
   }
   
   // concrete subclasses must override this method if they allow Finish operation
   public boolean finishAllowed() {
       return false;
   }
   
   public IWizardPage getNextPage() {
       OperationPage nextPage = (OperationPage)super.getNextPage();
       if (nextPage != null) {
           nextPage = nextPage.getNextPageOverride();
       }
       return nextPage;
   }
   
   protected OperationPage getNextPageOverride() {
       return this;
   }
   
   public IWizardPage getPreviousPage() {
       OperationPage prevPage = (OperationPage)super.getPreviousPage();
       if (prevPage != null) {
           prevPage = prevPage.getPreviousPageOverride();
       }
       return prevPage;
   }
   
   protected OperationPage getPreviousPageOverride() {
       return this;
   }
   
   private static Point cachedShellSize = null;
   
   private synchronized void initializeCachedShellSize() {
       this.getShell().addListener(SWT.Resize, this::shellResized);
       if (cachedShellSize != null) {
           return;
       }
       int width = Config.DEFAULT_WIZARD_WIDTH;
       int height = Config.DEFAULT_WIZARD_HEIGHT;
       try {
           width = controller.getConfig().getInt(Config.WIZARD_WIDTH);
           height = controller.getConfig().getInt(Config.WIZARD_HEIGHT);
       } catch (Exception ex) {
           // no op
       }
       cachedShellSize = new Point(width, height);
   }

   protected static synchronized Point getCachedShellSize() {
       if (cachedShellSize == null) {
           return null;
       }
       return new Point(cachedShellSize.x, cachedShellSize.y);
   }
   
    private void shellResized(Event event) {
        switch (event.type) {
            case SWT.Resize:
                Point shellSize = this.getShell().getSize();
                cachedShellSize = new Point(shellSize.x, shellSize.y);
                break;
        }
    }
}