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
package com.salesforce.dataloader.util;

import static com.salesforce.dataloader.dao.database.DatabaseTestUtil.NAME_COL;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator for the account rows from the database reader.
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class AccountRowComparator implements Comparator<Map<String,Object>> {

    private boolean isReverse = false;

    public AccountRowComparator() {
        this.isReverse = false;
    }

    /**
     * @param isReverse if true, the comparison will be reversed
     */
    public AccountRowComparator(boolean isReverse) {
        this.isReverse = isReverse;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(T, T)
     */
    @Override
    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        String name1 = o1.get(NAME_COL).toString();
        String name2 = o2.get(NAME_COL).toString();
        if(!isReverse) {
            return name1.compareTo(name2);
        } else {
            return name2.compareTo(name1);
        }
    }

}