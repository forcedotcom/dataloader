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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.process.DataLoaderRunner;

public class AppUtilTest extends ConfigTestBase {    
    @Test
    public void testIsValidHttpsUrl() {
        Assert.assertTrue(AppUtil.isValidHttpsUrl("https://my.com"));
        Assert.assertFalse(AppUtil.isValidHttpsUrl("http://my.com"));
        Assert.assertFalse(AppUtil.isValidHttpsUrl("my.com"));
        Assert.assertFalse(AppUtil.isValidHttpsUrl("ftp://my.com"));
    }
    
    @Test
    public void testGetFullPathOfJar() {
        String path = AppUtil.getFullPathOfJar(AppUtil.class);
        Assert.assertNotNull(path);
        Assert.assertTrue(path.endsWith(".jar"));
    }

    @Test
    public void testGetDirContainingClassJar() {
        String dir = AppUtil.getDirContainingClassJar(AppUtil.class);
        Assert.assertNotNull(dir);
        Assert.assertTrue(new File(dir).isDirectory());
    }

    @Test
    public void testConvertCommandArgsArrayToArgMap() {
        String[] args = {"key1=value1", "key2=value2"};
        Map<String, String> argMap = DataLoaderRunner.configureRunModeAndGetArgsMap(args);
        Assert.assertEquals("value1", argMap.get("key1"));
        Assert.assertEquals("value2", argMap.get("key2"));
    }

    @Test
    public void testIsRunningOnMacOS() {
        boolean isMac = AppUtil.isRunningOnMacOS();
        Assert.assertEquals(System.getProperty("os.name").contains("Mac"), isMac);
    }

    @Test
    public void testIsRunningOnWindows() {
        boolean isWindows = AppUtil.isRunningOnWindows();
        Assert.assertEquals(System.getProperty("os.name").contains("Windows"), isWindows);
    }

    @Test
    public void testIsRunningOnLinux() {
        boolean isLinux = AppUtil.isRunningOnLinux();
        Assert.assertEquals(System.getProperty("os.name").contains("Linux"), isLinux);
    }

    @Test
    public void testExec() {
        int exitCode = AppUtil.exec(List.of("echo", "Hello, World!"), "Execution failed");
        Assert.assertEquals(0, exitCode);
    }

    @Test
    public void testSerializeToJson() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        String json = AppUtil.serializeToJson(map);
        Assert.assertEquals("{\"key\":\"value\"}", json);
    }

    @Test
    public void testDeserializeJsonToObject() throws IOException {
        String json = "{\"key\":\"value\"}";
        Map<String, Object> map = AppUtil.deserializeJsonToObject(new ByteArrayInputStream(json.getBytes()), Map.class);
        Assert.assertEquals("value", map.get("key"));
    }

}
