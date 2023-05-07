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
package com.salesforce.dataloader.action.visitor;

import com.salesforce.dataloader.client.ClientBase;
import com.salesforce.dataloader.config.Config;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.async.JobInfo;

public class BulkClientConnection {
    private BulkV2Connection bulkV2Connection = null;
    private BulkConnection bulkV1Connection = null;
    private Config config = null;

    public BulkClientConnection(BulkConnection conn, Config config) {
        this.bulkV1Connection = conn;
        this.config = config;
        addClientNameHeader();
    }

    public BulkClientConnection(BulkV2Connection conn, Config config) {
        this.bulkV2Connection = conn;
        addClientNameHeader();
    }
    
    public JobInfo createJob(JobInfo job) throws AsyncApiException {
        if (this.bulkV1Connection != null) {
            return this.bulkV1Connection.createJob(job);
        } else if (this.bulkV2Connection != null) {
            return this.bulkV2Connection.createJob(job);
        }
        return null;
    }
    
    public void addHeader(String headerName, String headerValue) {
        if (this.bulkV1Connection != null) {
            this.bulkV1Connection.addHeader(headerName, headerValue);
        }
        if (this.bulkV2Connection != null) {
            this.bulkV2Connection.addHeader(headerName, headerValue);
        }
    }

    public JobInfo getJobStatus(String jobId, boolean isQuery) throws AsyncApiException {
        addClientNameHeader();
        if (this.bulkV1Connection != null) {
            return this.bulkV1Connection.getJobStatus(jobId);
        } else if (this.bulkV2Connection != null) {
            return this.bulkV2Connection.getJobStatus(jobId, isQuery);
        }
        return null;
    }

    public JobInfo closeJob(String jobId, boolean isQuery) throws AsyncApiException {
        if (this.bulkV1Connection != null) {
            return this.bulkV1Connection.closeJob(jobId);
        } else if (this.bulkV2Connection != null) {
            return this.bulkV2Connection.getJobStatus(jobId, isQuery);
        }
        return null;
    }

    private void addClientNameHeader() {
       this.addHeader("Sforce-Call-Options",
                "client=" + ClientBase.getClientName(this.config));
    }
}
