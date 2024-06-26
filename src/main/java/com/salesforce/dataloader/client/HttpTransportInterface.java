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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.salesforce.dataloader.exception.HttpClientTransportException;
import com.sforce.async.AsyncApiException;
import com.sforce.ws.transport.Transport;

/**
 * This interface defines a Transport.
 *
 * @author http://cheenath.com
 * @version 1.0
 * @since 1.0  Nov 30, 2005
 */
public interface HttpTransportInterface extends Transport {
	enum SupportedHttpMethodType {
		PUT,
		POST,
		PATCH,
		DELETE
	}
    OutputStream connect(String endpoint, HashMap<String, String> httpHeaders, boolean enableCompression,
    		HttpTransportInterface.SupportedHttpMethodType httpMethod) throws IOException;

    void connect(String endpoint, HashMap<String, String> httpHeaders, boolean enableCompression,
    		HttpTransportInterface.SupportedHttpMethodType httpMethod, InputStream contentInputStream, String contentEncoding) throws IOException;
    
    HttpURLConnection openHttpGetConnection(String urlStr, Map<String, String> headers) throws IOException;
    InputStream httpGet(HttpURLConnection connection, String urlStr) throws IOException, AsyncApiException, HttpClientTransportException;
}
