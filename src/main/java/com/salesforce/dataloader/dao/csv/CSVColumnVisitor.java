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

package com.salesforce.dataloader.dao.csv;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

/**
 * Describe your class here.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class CSVColumnVisitor {

    private boolean first = true;

    private static final char QUOTE = '"';
    private static final char COMMA = ',';

    private Writer writer;

    //logger
    private static Logger logger = Logger.getLogger(CSVColumnVisitor.class);

    public CSVColumnVisitor(Writer writer) {
        this.writer = writer;
    }

    public void newRow() {
        first = true;
    }

    public void visit(String column) throws IOException {
        // prevent failure on nulls -- treat nulls as blanks
        if (column == null) {
            column = "";
        }
        try {
            if (!first)
                writer.write(COMMA);
            else
                first = false;

            writer.write(QUOTE);

            for (int i = 0, len = column.length(); i < len; i++) {
                char c = column.charAt(i);
                if (c == QUOTE)
                    writer.write("\"\"");
                else
                    writer.write(c);
            }

            writer.write(QUOTE);

        } catch (IOException e) {
            logger.error(e);
            throw e;
        }

    }

}
