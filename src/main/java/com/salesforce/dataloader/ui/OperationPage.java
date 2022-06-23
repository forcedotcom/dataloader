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
import org.eclipse.jface.wizard.WizardPage;

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

   public OperationPage(String name, Controller controller) {
       super(name);
       this.setTitle(Labels.getString(getClass().getSimpleName() + ".title"));
       this.setImageDescriptor(UIUtils.getImageRegistry().getDescriptor("splashscreens"));
       this.controller = controller;
       this.logger = LogManager.getLogger(this.getClass());
       this.setPageComplete(false);
   }
   
   public boolean setupPage() {
       // Set the description
       String description = Labels.getString(this.getClass().getSimpleName() + ".description")
                                           + getConfigInfo();
       this.setDescription(description);
       boolean success = true;
       if (this.controller.isLoggedIn()) {
           success = setupPagePostLogin();            
           this.setDescription(this.getDescription() 
                   + "\n" + this.controller.getAPIInfo());
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
}