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

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.process.ProcessTestBase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to load/delete test records for a manual dataloader test
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class TestRecordsUtil extends ProcessTestBase {

    private TestRecordsUtil(Map<String, String> config) {
        super(config);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String,String> argMap = validateCmdLineArgs(args);

        TestRecordsUtil testUtil = new TestRecordsUtil(argMap);
        testUtil.execute(args);
    }

    /**
     * @param argMap
     * @throws Exception
     */
    private void execute(String[] args) throws Exception {
        Map<String, String> argMap = getTestConfig();
        String operation = argMap.get("-o");
        if(operation == null || ! (operation.equals("delete") || operation.equals("insert") || operation.equals("upsert"))) {
            System.out.println("Valid operation name is required");
            printUsage(args);
        }
        int numRecords = argMap.containsKey("-n") ? Integer.parseInt(argMap.get("-n")) : 100;
        String whereClause = argMap.get("-q");
        if(whereClause == null) {
            whereClause = ConfigTestBase.ACCOUNT_WHERE_CLAUSE;
        }

        setupController();
        if("insert".equals(operation)) {
            insertSfdcAccounts(numRecords, true);
        } else if("upsert".equals(operation)) {
            upsertSfdcAccounts(numRecords);
        } else if("delete".equals(operation)) {
            this.getBinding().deleteSfdcRecords("Account",whereClause, 0);
        }
    }

    private static Map<String,String> validateCmdLineArgs (String[] args) {
        Map<String,String> argMap = new HashMap<String,String>();
        for (int i = 0; i < args.length; i++) {
            String option = args[i];
            if(option.length() < 2 || option.charAt(0) != '-' || option.charAt(1) == 'h') {
                printUsage(args);
            }
            argMap.put(option, args[++i]);
        }
        return argMap;
    }

    private static void printUsage(String[] args) {
        System.out.println("Invalid arguments: " + Arrays.asList(args).toString() + "\n");
        System.out.println("Usage: TestRecordsUtil -n <numRecords> -o <delete|insert|upsert> -q <account number pattern>");
        System.out.println("\nExample:\n\tTestRecordsUtil -o delete -q \"where AccountNumber like 'ACCT%'\"");
        System.exit(-1);
    }
}

