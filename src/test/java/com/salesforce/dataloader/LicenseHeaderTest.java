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

package com.salesforce.dataloader;

import java.io.*;
import java.util.*;

import com.sforce.ws.util.FileUtil;

/**
 * 
 * Test class to validate that all sources files have the license header comment.
 *
 * @author Colin Jarvis
 * @since 23.0
 * 
 */
public class LicenseHeaderTest extends TestBase {

    public LicenseHeaderTest(String name) {
        super(name);
    }

    public void testSourcesHaveLicense() throws IOException {
        // get a list of all the files in our source directories
        final List<File> sourceFiles = getSourceFiles();
        
        // check each source file and add it to the failure set if it doesn't contain the license header comment
        final Set<String> failures = new HashSet<String>();
        for (File src : sourceFiles) {
            if (src.getPath().toLowerCase().endsWith(".java") && !sourceHasLicense(src)) failures.add(src.getPath());
        }
        
        // fail if there were failures
        if (!failures.isEmpty())
            fail("the following files do not have the correct license header" + failures);
    }

    private static final String DATALOADER_SRC = "src/main/java";
    private static final String TEST_SRC = "src/test/java";

    /** @returns A list containing all files in dataloader source folders */
    private List<File> getSourceFiles() {
        final List<File> allFiles = new ArrayList<File>();
        allFiles.addAll(FileUtil.listFilesRecursive(getResource(DATALOADER_SRC), false));
        allFiles.addAll(FileUtil.listFilesRecursive(getResource(TEST_SRC), false));
        return allFiles;
    }

    /** The license header text lines to validate against */
    private static String[] LICENSE_HDR = new String[] {
            " * Copyright (c) " + Calendar.getInstance().get(Calendar.YEAR) + ", salesforce.com, inc.",
            " * All rights reserved.",
            " *",
            " * Redistribution and use in source and binary forms, with or without modification, are permitted provided",
            " * that the following conditions are met:",
            " *",
            " *    Redistributions of source code must retain the above copyright notice, this list of conditions and the",
            " *    following disclaimer.",
            " *",
            " *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and",
            " *    the following disclaimer in the documentation and/or other materials provided with the distribution.",
            " *",
            " *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or",
            " *    promote products derived from this software without specific prior written permission.",
            " *",
            " * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED",
            " * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A",
            " * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR",
            " * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED",
            " * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)",
            " * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING",
            " * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE",
            " * POSSIBILITY OF SUCH DAMAGE." };

    private boolean sourceHasLicense(File src) throws IOException {
        final BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(src)));
        
        // Read the block comment opener
        String line = rdr.readLine();
        if (!"/*".equals(line)) return false;

        // read the header comment, and compare line by line with the expected text 
        int i = 0;
        while ((line = rdr.readLine()) != null) {
            // stop reading lines with we have reached the end of the block comment
            if (" */".equals(line)) break;
            if (!line.equals(LICENSE_HDR[i++])) return false;
        }
        return true;
    }

}
