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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.ConfigInitializationException;
import com.salesforce.dataloader.exception.ParameterLoadException;

public abstract class ConfigTestBase extends TestBase {

    /** Each enum represents a property that we read from test.properties and use as dataloader config settings. */
    protected static enum TestProperties {
        USER_ADMIN(Config.USERNAME),
        USER_STANDARD(Config.USERNAME),
        PASSWORD(Config.PASSWORD),
        REDIRECT(Config.RESET_URL_ON_LOGIN),
        ENDPOINT(Config.ENDPOINT),
        ENTITY_DEFAULT(Config.ENTITY),
        ACCOUNT_EXTID(Config.EXTERNAL_ID_FIELD);

        private static final Properties TEST_PROPS;

        static {
            TEST_PROPS = new Properties();
            loadTestProperties();
        }

        private static void loadTestProperties() {
            final URL url = TestBase.class.getClassLoader().getResource("test.properties");
            if (url == null)
                throw new IllegalStateException("Failed to locate test.properties.  Is it in the classpath?");
            try {
                final InputStream propStream = url.openStream();
                try {
                    TEST_PROPS.load(propStream);
                } finally {
                    propStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load test properties from resource: " + url, e);
            }
        }

        private final String configName;

        TestProperties(String configName) {
            this.configName = configName;
        }

        /**
         * Translates the enum name into a property name found in test.properties.
         * 
         * @return A property name in the test.properties file. EG USER_ADMIN becomes "test.user.admin"
         */
        private String getPropertyName() {
            return "test." + name().toLowerCase().replace('_', '.');
        }

        public void putConfigSetting(Map<String, String> destConfig) {
            destConfig.put(this.configName, TEST_PROPS.getProperty(getPropertyName()));
        }
    }

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
        final HashMap<String, String> configBase = new HashMap<String, String>(this.testConfig);
        configBase.put(Config.LAST_RUN_OUTPUT_DIR, getTestStatusDir());
        for (TestProperties prop : getDefaultTestPropertiesSet()) {
            prop.putConfigSetting(configBase);
        }
        return configBase;
    }

    protected Set<TestProperties> getDefaultTestPropertiesSet() {
        Set<TestProperties> propSet = EnumSet.noneOf(TestProperties.class);
        propSet.add(TestProperties.USER_ADMIN);
        propSet.add(TestProperties.PASSWORD);
        propSet.add(TestProperties.ENDPOINT);
        propSet.add(TestProperties.REDIRECT);
        propSet.add(TestProperties.ENTITY_DEFAULT);
        propSet.add(TestProperties.ACCOUNT_EXTID);
        return propSet;
    }

    protected ConfigTestBase(String name, Map<String, String> testConfig) {
        super(name);
        if (testConfig == null) testConfig = new HashMap<String, String>();
        this.testConfig = testConfig;
    }

    protected ConfigTestBase(String name) {
        this(name, null);
    }

    @Override
    public void setUp() {
        super.setUp();
        try {
            getController().getConfig().loadParameterOverrides(getTestConfig());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Override
    protected void initController() {
        super.initController();
        try {
            getController().getConfig().loadParameterOverrides(getTestConfig());
        } catch (ParameterLoadException e) {
            fail(e);
        } catch (ConfigInitializationException e) {
            fail(e);
        }
    }

}
