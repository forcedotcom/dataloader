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

package com.salesforce.dataloader.action.progress;

/**
 * This class implements the ILoaderProgress but does nothing with
 * the callbacks.
 * 
 * We're nihilists Lebowski, we believe in nothing.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.util.DLLogManager;

public class NihilistProgressAdapter implements ILoaderProgress {
    private String message;
    private boolean success = false;
    private int numRowsWithError = 0;
    private int numberBatchesTotal = 0;
    private final Logger logger = DLLogManager.getLogger(getClass());
    private int workDone;
    private int totalWork;
    private final List<String> subTasksInOrder = new ArrayList<String>();


    public NihilistProgressAdapter() {
        // no op
    }

    public void beginTask(String name, int totalWork) {
        this.totalWork = totalWork;
    }
    
    public void doneError(String msg) {
        success = false;
        message = msg;
        logger.error(msg);
    }

    public void doneSuccess(String msg) {
        success = true;
        message = msg;
        logger.info(msg);
    }

    public void worked(int worked) {
        this.workDone += worked;
    }

    public void setSubTask(String name) {
        this.subTasksInOrder.add(name);
        logger.info(name);
    }

    public int getTotalWork() {
        return this.totalWork;
    }
    
    public int getNumWorked() {
        return this.workDone;
    }

    public List<String> getSubTasks() {
        return Collections.unmodifiableList(this.subTasksInOrder);
    }

    public boolean isCanceled() {
        return false;
    }

    public void setNumberBatchesTotal(int numberBatchesTotal) {
        this.numberBatchesTotal = numberBatchesTotal;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public int getNumberBatchesTotal() {
        return this.numberBatchesTotal;
    }

    public void setNumberRowsWithError(int rowsWithError) {
        this.numRowsWithError = rowsWithError;
        
    }

    public int getNumberRowsWithError() {
        return this.numRowsWithError;
    }
}