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
    private long totalRecordsInJob = 0;
    private boolean started = false;

    public LoadRateCalculator() {
        // do nothing
    }

    public synchronized void start(int numRecords) {
        if (!started) {
            started = true;
            this.startTime = new Date();
            this.totalRecordsInJob = numRecords;
        }
    }

    public String calculateSubTask(long processedRecordsInJob, long numErrorsInJob) {

        final Date currentLoadTime = new Date();
        final long numSuccessInJob = processedRecordsInJob - numErrorsInJob;
        //final long currentPerMin = numSuccess * 60 * 60;
        long hourlyProcessingRate;

        final long totalElapsedTimeInSec = (currentLoadTime.getTime() - this.startTime.getTime())/1000;
        if (totalElapsedTimeInSec == 0) {
            hourlyProcessingRate = 0;
        } else {
            hourlyProcessingRate = (processedRecordsInJob * 60 * 60) / totalElapsedTimeInSec;
        }

        long remainingTimeInSec = 0;
        long estimatedTotalTimeInSec = 0;
        if (this.totalRecordsInJob > 0 && processedRecordsInJob > 0) {
            // can estimate remaining time only if a few records are processed already.
            estimatedTotalTimeInSec = (long) (totalElapsedTimeInSec * this.totalRecordsInJob / processedRecordsInJob);
            remainingTimeInSec = estimatedTotalTimeInSec - totalElapsedTimeInSec;
        }

        final long remainingTimeInMinutes = remainingTimeInSec / 60;
        final long remainingSeconds = remainingTimeInSec - remainingTimeInMinutes * 60;

        if (hourlyProcessingRate <= 0 || (remainingTimeInMinutes > 7 * 24 * 60)) { // processing time not calculated or imprecise
            // LoadRateCalculator.processedTimeUnknown=Processed {0} of {1} total records. 
            // There are {2} successes and {3} errors.
            return Messages.getMessage(getClass(), "processedTimeUnknown", 
                    processedRecordsInJob, // {0}
                    this.totalRecordsInJob,  // {1}
                    numSuccessInJob,       // {2}
                    numErrorsInJob);       // {3}
        }
        // LoadRateCalculator.processed=Processed {0} of {1} total records in {7} seconds. 
        // There are {5} successes and {6} errors. \nRate: {2} records per hour. 
        // Estimated time to complete: {3} minutes and {4} seconds. 
        return Messages.getMessage(getClass(), "processed", 
                processedRecordsInJob, // {0}
                this.totalRecordsInJob,  // {1}
                hourlyProcessingRate,    // {2}
                remainingTimeInMinutes,  // {3}
                remainingSeconds,        // {4}
                numSuccessInJob,       // {5}
                numErrorsInJob,       // {6}
                totalElapsedTimeInSec // {7}
            );
    }
}