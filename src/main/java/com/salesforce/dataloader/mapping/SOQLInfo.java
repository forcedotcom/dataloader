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

package com.salesforce.dataloader.mapping;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to parse information used by DataLoader from a soql expression
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
class SOQLInfo {

    static class SOQLParserException extends Exception {
        public SOQLParserException(String msg) {
            super(msg);
        }
    }

    static class SOQLFieldInfo {
        private final String alias;
        private final String aggregateFunction;
        private final String fieldName;

        SOQLFieldInfo(String fieldString, AtomicInteger aggregateFieldIdx) throws SOQLParserException {
            fieldString = getTrimmed(fieldString);
            int lparenIdx = fieldString.indexOf('(');
            // no nested queries!
            if (lparenIdx == 0) throw invalidSoql("Nested queries are not supported");
            if (lparenIdx < 0) {
                // normal field
                this.fieldName = fieldString;
                this.aggregateFunction = null;
                this.alias = null;
            } else {
                // parse aggregate expression
                int rparenIdx = fieldString.indexOf(')', lparenIdx + 1);
                if (rparenIdx < 0) throw invalidSoql("Could not find closing paren");
                this.aggregateFunction = fieldString.substring(0, lparenIdx);
                this.fieldName = getTrimmed(fieldString.substring(lparenIdx + 1, rparenIdx));
                this.alias = determineAggregateAlias(aggregateFieldIdx, fieldString.substring(rparenIdx + 1));
            }
        }

        SOQLFieldInfo(String fieldString) throws SOQLParserException {
            this(fieldString, null);
        }

        private String determineAggregateAlias(AtomicInteger aggregateFieldIdx, String aliasString) {
            String alias = aliasString.trim();
            if (alias.length() == 0) {
                alias = aggregateFieldIdx == null ? null : "expr" + aggregateFieldIdx.getAndIncrement();
            }
            return alias;
        }

        String getAlias() {
            return alias;
        }

        String getAggregateFunction() {
            return aggregateFunction;
        }

        String getFieldName() {
            return fieldName;
        }

        boolean isAggregate() {
            return this.aggregateFunction != null;
        }

        boolean hasAlias() {
            return this.alias != null;
        }

        @Override
        public String toString() {
            if (isAggregate()) return getAggregateFunction() + "(" + getFieldName() + ")";
            return getFieldName();
        }

    }

    private final List<SOQLFieldInfo> selectedFields;

    private final String tableName;
    private final String tableAlias;

    private static final String SELECT_KEYWORD = "select ";
    private static final String FROM_KEYWORD_WITHOUT_SPACE = "from";
    private static final String FROM_KEYWORD = FROM_KEYWORD_WITHOUT_SPACE + " ";
    private static final String WHERE_KEYWORD = "where";

    SOQLInfo(String soql) throws SOQLParserException {
        soql = getTrimmed(soql);

        this.selectedFields = new ArrayList<SOQLFieldInfo>();

        String soqlLower = soql.toLowerCase();
        if (!soqlLower.startsWith(SELECT_KEYWORD)) throw invalidSoql("No 'SELECT' keyword");
        int fromIdx = soqlLower.indexOf(FROM_KEYWORD);
        if (fromIdx < 0) {
            if (soqlLower.indexOf(FROM_KEYWORD_WITHOUT_SPACE) >= 0) {
                throw invalidSoql("No sobject specified after 'FROM' keyword");
            }
            throw invalidSoql("No 'FROM' keyword");
        }

        String rawFields = soql.substring(SELECT_KEYWORD.length(), fromIdx).trim();
        AtomicInteger aggIdx = new AtomicInteger();
        for (String fieldString : rawFields.split(",")) {
            SOQLFieldInfo soqlFieldInfo = new SOQLFieldInfo(fieldString, aggIdx);
            this.selectedFields.add(soqlFieldInfo);
        }

        String remainder = getTrimmed(soql.substring(fromIdx + FROM_KEYWORD.length()).trim());
        StringTokenizer stok = new StringTokenizer(remainder);
        String tableName = null;
        if (stok.hasMoreTokens()) tableName = stok.nextToken();
        if (WHERE_KEYWORD.equalsIgnoreCase(tableName)) {
            throw invalidSoql("Failed to parse table name");
        }
        this.tableName = tableName;
        String alias = null;
        if (stok.hasMoreTokens()) {
            alias = stok.nextToken();
            if (WHERE_KEYWORD.equalsIgnoreCase(alias)) alias = "";
        }
        this.tableAlias = alias;
    }

    private static String getTrimmed(String s) {
        if (s == null) throw new NullPointerException();
        s = s.trim();
        if (s.length() == 0) throw new IllegalArgumentException("Cannot parse empty string");
        return s;
    }

    private static SOQLParserException invalidSoql(String msg) {
        return new SOQLParserException("Invalid soql: " + msg);
    }

    List<SOQLFieldInfo> getSelectedFields() {
        return this.selectedFields;
    }

    String getTableName() {
        return tableName;
    }

    String getTableAlias() {
        return tableAlias == null ? "" : tableAlias;
    }

}
