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
package com.salesforce.dataloader.client;

import java.util.Calendar;

import com.sforce.soap.partner.GetUserInfoResult;

@SuppressWarnings("serial")
public class SessionInfo {

    public static class NotLoggedInException extends RuntimeException {
        private NotLoggedInException(SessionInfo sess) {
            super(sess.sessionId == null ? "Not Logged In" : "Session is expired");
        }
    }

    private final String sessionId;
    private final String serverUrl;
    private final GetUserInfoResult userInfo;
    private long lastActivityTimeInMsec = 0;

    SessionInfo(String sessionId, String server, GetUserInfoResult userInfo) {
        this.sessionId = sessionId;
        this.serverUrl = server;
        this.userInfo = userInfo;
        if (this.userInfo != null) {
            this.lastActivityTimeInMsec = Calendar.getInstance().getTimeInMillis();
        }
    }

    SessionInfo() {
        this(null, null, null);
    }

    public boolean isSessionValid() {
        long currentTimeInMsec = Calendar.getInstance().getTimeInMillis();
        long inSessionElapsedTimeInSec = (currentTimeInMsec - this.lastActivityTimeInMsec)/1000;
        return (this.sessionId != null
                && userInfo != null
                && inSessionElapsedTimeInSec < userInfo.getSessionSecondsValid());
    }

    public void validate() throws NotLoggedInException {
        if (!isSessionValid()) throw new NotLoggedInException(this);
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getServer() {
        return this.serverUrl;
    }
    
    public GetUserInfoResult getUserInfoResult() {
        return this.userInfo;
    }
    
    public void performedSessionActivity() {
        this.lastActivityTimeInMsec = Calendar.getInstance().getTimeInMillis();
    }
}
