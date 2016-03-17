package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class TestConfigLoader {
    private final static String BASE_CONFIG_LOCATION = System.getProperty("com.autonomy.baseConfig");
    private final static String OVERRIDE_CONFIG_LOCATION = System.getProperty("com.autonomy.configFile");
        // system property set in the POM
    private final static boolean MAVEN = System.getProperty("com.autonomy.mavenFlag") != null;
    private final static Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

    // used when running whole suite via mvn
    private static JsonConfig getMavenConfig(String path) throws IOException {
        if (path == null) {
            return null;
        }
        LOGGER.info("using config " + path);
        return JsonConfig.fromURL(TestConfig.class.getClassLoader().getResource(path));
    }

    // used when running single tests via IDE
    private JsonConfig getLocalConfig(String path) throws IOException {
        if (path == null) {
            return null;
        }
        LOGGER.info("using config " + path);
        return JsonConfig.readFile(new File(path));
    }

    public List<Object[]> readConfigs(final Collection<ApplicationType> applicationTypes) throws IOException {
        List<Object[]> configs = new ArrayList<>();

        JsonConfig defaultConfig;
        JsonConfig userSpecifiedConfig;
        if (MAVEN) {
            defaultConfig = getMavenConfig(BASE_CONFIG_LOCATION);
            userSpecifiedConfig = getMavenConfig(OVERRIDE_CONFIG_LOCATION);
        } else {
            String basePath = BASE_CONFIG_LOCATION == null ? "../config/hsod-dev.json" : BASE_CONFIG_LOCATION;
            defaultConfig = getLocalConfig(basePath);
            userSpecifiedConfig = getLocalConfig(OVERRIDE_CONFIG_LOCATION);
        }

        JsonConfig jsonConfig = defaultConfig.overrideUsing(userSpecifiedConfig);

        if (applicationTypes.contains(jsonConfig.getAppType())) {
            for (int i = 0; i < jsonConfig.getBrowsers().size(); i++) {
                TestConfig config = new TestConfig(i, jsonConfig);
                // for compatibility
                configs.add(new Object[]{
                        config
                });
            }
        }
        return configs;
    }

    public JsonNode getRawBaseConfig() throws IOException {
        if (MAVEN) {
            return new ObjectMapper().readTree(TestConfig.class.getClassLoader().getResource(BASE_CONFIG_LOCATION));
        } else {
            return new ObjectMapper().readTree(new File(BASE_CONFIG_LOCATION));
        }
    }
}
