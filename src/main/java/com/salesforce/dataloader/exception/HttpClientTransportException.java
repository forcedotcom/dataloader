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

package com.salesforce.dataloader.exception;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 *
 * @since 60.0
 */
@SuppressWarnings("serial")
public class HttpClientTransportException extends OperationException {

    private InputStream inputStream;
    private HttpURLConnection connection;
    
    public HttpClientTransportException() {
        super();
    }

    public HttpClientTransportException(String message, HttpURLConnection conn, InputStream is) {
        super(message);
        connection = conn;
        inputStream = is;
    }

    public HttpClientTransportException(Throwable cause, HttpURLConnection conn, InputStream is) {
        super(cause);
        connection = conn;
        inputStream = is;
    }

    public HttpClientTransportException(String message, Throwable cause, HttpURLConnection conn, InputStream is) {
        super(message, cause);
        connection = conn;
        inputStream = is;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }

    public HttpURLConnection getConnection() {
        return connection;
    }
}
