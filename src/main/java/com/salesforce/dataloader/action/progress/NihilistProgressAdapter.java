/*
 * Copyright (c) 2012, salesforce.com, inc.
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

import org.apache.log4j.Logger;


/**
 * This class implements the ILoaderProgress but does nothing with
 * the callbacks.
 * 
 * We're nihilists Lebowski, we believe in nothing.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public enum NihilistProgressAdapter implements ILoaderProgress {
    INSTANCE;

    public static NihilistProgressAdapter get() {
        return INSTANCE;
    }

    //logger
    private final Logger logger = Logger.getLogger(getClass());

    @Override
    public void beginTask(String name, int totalWork) {

    }

    @Override
    public void doneError(String msgs) {
        logger.error(msgs);
    }

    @Override
    public void doneSuccess(String msg) {
        logger.info(msg);

    }

    @Override
    public void worked(int worked) {

    }

    public void setTaskName(String name) {

    }

    @Override
    public void setSubTask(String name) {
        logger.info(name);
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setNumberBatchesTotal(int numberBatchesTotal) {
        // nothing
    }

}
