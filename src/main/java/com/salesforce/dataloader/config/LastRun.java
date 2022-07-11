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
package com.salesforce.dataloader.config;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Class to manage last run information.  Currently based on properties.
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class LastRun extends Properties {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private static Logger logger = LogManager.getLogger(LastRun.class);

    // last run statistics
    public static final String LAST_LOAD_BATCH_ROW = "process.lastLoadBatchRow"; //$NON-NLS-1$
    public static final String LAST_RUN_DATE = "process.lastRunDate"; //$NON-NLS-1$

    private static Map<String,String> paramNames = new HashMap<String,String>();

    private String filePath;
    private String filename;
    private boolean outputEnabled;

    /**
     * Initialize lastRun with filename, this is needed if last run output is not enabled (yet)
     */
    public LastRun(String filename, String filePath, boolean outputEnabled) {
        super(); // init properties
        this.filename = filename;
        this.filePath = filePath;
        this.outputEnabled = outputEnabled;

        paramNames.put(LAST_RUN_DATE,"");
        paramNames.put(LAST_LOAD_BATCH_ROW,"");
    }

    public String getFullPath() {
        return new File(this.filePath, this.filename).getAbsolutePath();
    }

    /**
     * @throws IOException
     */
    public void load() throws IOException {
        if(! outputEnabled) {
            return;
        }
        if (filePath == null) {
            logger.fatal(Messages.getString("LastRun.fileMissing"));
            throw new IOException(Messages.getString("LastRun.fileMissing")); //$NON-NLS-1$
        }
        File lastRunFile = new File(filePath, filename);
        logger.info(Messages.getFormattedString("LastRun.fileInfo", lastRunFile.getAbsolutePath()));
        if(!lastRunFile.exists()) {
            lastRunFile.createNewFile();
        }
        FileInputStream in = new FileInputStream(lastRunFile);
        try {
            load(in);
        } finally {
            in.close();
        }
    }

    public void save() throws IOException {
        if(! outputEnabled) {
            return;
        }
        if (filePath == null) {
            throw new IOException(Messages.getString("LastRun.fileMissing")); //$NON-NLS-1$
        }

        final FileOutputStream out = new FileOutputStream(new File(filePath, filename));
        try {
            store(out, "Last Run Config"); //$NON-NLS-1$
        } finally {
            out.close();
        }
    }

    public boolean hasParameter(String paramName) {
        return paramNames.containsKey(paramName);
    }

    public void setDefault(String paramName, String paramValue) {
        if (!containsKey(paramName)) {
            setProperty(paramName, paramValue);
        }
    }
}
