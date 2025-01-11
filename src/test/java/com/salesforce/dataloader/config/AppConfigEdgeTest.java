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
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.HashMap; 
import java.util.Map; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;
import org.junit.Before; 
import org.junit.Test;
import com.salesforce.dataloader.exception.ConfigInitializationException; 

public class AppConfigEdgeTest {
    private AppConfig appConfig;
    
    @Before
    public void setUp() throws ConfigInitializationException, IOException {
        Map<String, String> testConfigMap = new HashMap<>();
        testConfigMap.put(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT, AppConfig.SERVER_PROD_ENVIRONMENT_VAL);
        appConfig = AppConfig.getInstance(testConfigMap);
    }
    
    @Test
    public void testInvalidPropertyName() {
        assertEquals(appConfig.getString("invalid.property.name"), "");
    }
    
    @Test
    public void testSpecialCharactersInPropertyValue() {
        String specialValue = "valueWith\nNewline\tTab\u2603Unicode";
        appConfig.setValue("special.property", specialValue);
        assertEquals(specialValue, appConfig.getString("special.property"));
    }
    
    @Test
    public void testEmptyConfigurationMap() throws ConfigInitializationException, IOException {
        AppConfig emptyConfig = AppConfig.getInstance(new HashMap<>());
        assertEquals(emptyConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT), AppConfig.SERVER_PROD_ENVIRONMENT_VAL);
    }
    
    @Test
    public void testLargeConfigurationMap() throws ConfigInitializationException, IOException {
        Map<String, String> largeConfigMap = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            largeConfigMap.put("property" + i, "value" + i);
        }
        AppConfig largeConfig = AppConfig.getInstance(largeConfigMap);
        assertEquals("value9999", largeConfig.getString("property9999"));
    }
    
    @Test
    public void testConcurrentModifications() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                appConfig.setValue(AppConfig.PROP_BULK_API_ENABLED, true);
                assertTrue(appConfig.getBoolean(AppConfig.PROP_BULK_API_ENABLED));
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));
    }
    
    @Test
    public void testDefaultValues() {
        assertEquals(AppConfig.STRING_DEFAULT, appConfig.getString("missing.property"));
    }
    
    @Test
    public void testCaseSensitivity() {
        appConfig.setValue("CaseSensitiveProperty", "value");
        assertEquals(appConfig.getString("casesensitiveproperty"), "");
    }
}
