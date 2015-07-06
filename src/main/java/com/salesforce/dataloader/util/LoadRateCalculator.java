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

package com.salesforce.dataloader.util;

import java.util.Date;

import com.salesforce.dataloader.config.Messages;

/**
 * Calculates of the progress strings
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class LoadRateCalculator {

    // TODO: we can probably move all references to this code into a base ProgressMonitor class
    private Date startTime = null;
    private int totalRecords;

    public LoadRateCalculator() {}

    public void start() {
        this.startTime = new Date();
    }

    public void setNumRecords(int numRecords) {
        this.totalRecords = numRecords;
    }

    public String calculateSubTask(int numProcessed, int numErrors) {

        final Date currentLoadTime = new Date();
        final int numSuccess = numProcessed - numErrors;
        final long currentPerMin = numSuccess * 60 * 60;
        long rate;

        final long totalElapsed = currentLoadTime.getTime() - this.startTime.getTime();
        if (totalElapsed == 0) {
            rate = 0;
        } else {
            rate = currentPerMin / totalElapsed * 1000;
        }

        long remainingSeconds = 0;
        if (this.totalRecords > 0) {
            // time_remaining = time_elapsed / percent_complete - time_elapsed
            remainingSeconds = (long)(((double)this.totalRecords / numSuccess - 1.0) * totalElapsed) / 1000;
        }

        final long mins = remainingSeconds / 60;

        final long seconds = remainingSeconds - mins * 60;

        return Messages.getMessage(getClass(), "processed", numProcessed, this.totalRecords, rate, mins, seconds,
                numSuccess,
                numErrors);
    }
}