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
package com.salesforce.dataloader;

import java.util.*;

import com.salesforce.dataloader.action.progress.ILoaderProgress;

/**
 * Progress monitor for use by tests.
 * 
 * @author Colin Jarvis
 * @since 20.0
 */
public class TestProgressMontitor implements ILoaderProgress {

    private String taskName;
    private int workDone;
    private int totalWork;
    private boolean success;
    private String message;
    private final List<String> subTasksInOrder = new ArrayList<String>();

    @Override
    public void beginTask(String name, int totalWork) {
        this.taskName = name;
        this.totalWork = totalWork;
    }

    @Override
    public void doneError(String message) {
        this.success = false;
        this.message = message;
    }

    @Override
    public void doneSuccess(String message) {
        this.success = true;
        this.message = message;
    }

    @Override
    public void worked(int worked) {
        this.workDone += worked;
    }

    @Override
    public void setSubTask(String name) {
        this.subTasksInOrder.add(name);
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    // /////////// getters for test verification //////////////////

    public String getTaskName() {
        return this.taskName;
    }

    public int getTotalWork() {
        return this.totalWork;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public int getNumWorked() {
        return this.workDone;
    }

    public List<String> getSubTasks() {
        return Collections.unmodifiableList(this.subTasksInOrder);
    }

}
