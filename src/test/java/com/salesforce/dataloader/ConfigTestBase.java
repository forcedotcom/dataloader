/*
 * Copyright (c) 2012, salesforce.com, inc.
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

import java.util.*;

public abstract class ConfigTestBase extends TestBase {

    public static ConfigGenerator DEFAULT_CONFIG_GEN = new ConfigGenerator() {

        @Override
        public List<Map<String, String>> getConfigurations() {
            List<Map<String, String>> result = new ArrayList<Map<String, String>>();
            result.add(new HashMap<String, String>());
            return result;
        }

        @Override
        public int getNumConfigurations() {
            return 1;
        }

    };

    public static class UnionConfigGenerator implements ConfigGenerator {
        private final ConfigGenerator[] gens;

        public UnionConfigGenerator(ConfigGenerator... gens) {
            this.gens = gens == null ? new ConfigGenerator[0] : gens;
        }

        @Override
        public List<Map<String, String>> getConfigurations() {
            List<Map<String, String>> configs = new ArrayList<Map<String, String>>(getNumConfigurations());
            for (ConfigGenerator g : this.gens) {
                if (g != null) configs.addAll(g.getConfigurations());
            }
            return configs;
        }

        @Override
        public int getNumConfigurations() {
            int n = 0;
            for (ConfigGenerator g : this.gens)
                if (g != null) n += g.getNumConfigurations();
            return n;
        }

    }

    public static class ConfigSettingGenerator implements ConfigGenerator {
        private final ConfigGenerator gen;
        private final String setting;
        private final String[] values;

        public static ConfigSettingGenerator getBooleanGenerator(ConfigGenerator gen, String setting) {
            return new ConfigSettingGenerator(gen, setting, Boolean.TRUE.toString(), Boolean.FALSE.toString());
        }

        public ConfigSettingGenerator(String setting, String... values) {
            this(null, setting, values);
        }

        public ConfigSettingGenerator(ConfigGenerator gen, String setting, String... values) {
            this.gen = gen == null ? DEFAULT_CONFIG_GEN : gen;
            this.setting = setting;
            this.values = values;
            assert this.values != null && this.values.length > 0;
        }

        @Override
        public List<Map<String, String>> getConfigurations() {
            final List<Map<String, String>> result = this.gen.getConfigurations();
            final int startSize = result.size();
            assert startSize > 0;
            for (int idx = 0; idx < startSize; idx++) {
                Map<String, String> config = result.get(idx);
                config.put(this.setting, this.values[0]);
                for (int j = 1; j < this.values.length; j++) {
                    config = new HashMap<String, String>(config);
                    config.put(this.setting, this.values[j]);
                    result.add(config);
                }
            }
            return result;
        }

        @Override
        public int getNumConfigurations() {
            return this.gen.getNumConfigurations() * this.values.length;
        }
    }

    private final Map<String, String> testConfig;

    protected Map<String, String> getTestConfig() {
        return testConfig;
    }

    protected ConfigTestBase(String name, Map<String, String> testConfig) {
        super(name);
        this.testConfig = testConfig;
    }

    protected ConfigTestBase(String name) {
        this(name, null);
    }
    
    @Override
    public void setUp() {
        super.setUp();
        try {
            if (getTestConfig() != null) {
                getController().getConfig().loadParameterOverrides(getTestConfig());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Override
    protected void initController() {
        super.initController();
        try {
            if (getTestConfig() != null) {
                getController().getConfig().loadParameterOverrides(getTestConfig());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

}
