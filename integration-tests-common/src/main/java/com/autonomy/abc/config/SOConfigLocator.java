package com.autonomy.abc.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SOConfigLocator {
    private final static String DEFAULT_CONFIG_LOCATION = "../config/hsod-dev.json";
    private final static String BASE_CONFIG_LOCATION = System.getProperty("com.autonomy.baseConfig");
    private final static String OVERRIDE_CONFIG_LOCATION = System.getProperty("com.autonomy.configFile");
    // system property set in the POM
    private final static boolean MAVEN = System.getProperty("com.autonomy.mavenFlag") != null;
    private final static Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

    private final JsonConfigReader reader;

    public SOConfigLocator() {
        reader = MAVEN ? new MavenJsonConfigReader() : new LocalJsonConfigReader();
    }

    public JsonConfig getJsonConfig() throws IOException {
        JsonConfig base = maybeReadConfig(getBaseLocation());
        JsonConfig override = maybeReadConfig(OVERRIDE_CONFIG_LOCATION);
        if (base == null) {
            LOGGER.warn("base config not found");
            return override;
        }
        return base.overrideUsing(override);
    }

    public JsonNode getJsonNode() throws IOException {
        return reader.toJsonNode(getBaseLocation());
    }

    private String getBaseLocation() {
        return BASE_CONFIG_LOCATION == null ? DEFAULT_CONFIG_LOCATION : BASE_CONFIG_LOCATION;
    }

    private JsonConfig maybeReadConfig(String path) throws IOException {
        if (path == null) {
            return null;
        }
        LOGGER.info("using config " + path);
        return reader.toJsonConfig(path);
    }

    private static class MavenJsonConfigReader implements JsonConfigReader {
        @Override
        public JsonConfig toJsonConfig(String path) throws IOException {
            return JsonConfig.fromURL(convert(path));
        }

        @Override
        public JsonNode toJsonNode(String path) throws IOException {
            return new ObjectMapper().readTree(convert(path));
        }

        private URL convert(String path) {
            return SOConfigLocator.class.getClassLoader().getResource(path);
        }
    }

    private static class LocalJsonConfigReader implements JsonConfigReader {
        @Override
        public JsonConfig toJsonConfig(String path) throws IOException {
            return JsonConfig.readFile(convert(path));
        }

        @Override
        public JsonNode toJsonNode(String path) throws IOException {
            return new ObjectMapper().readTree(convert(path));
        }

        private File convert(String path) {
            return new File(path);
        }
    }

    private interface JsonConfigReader {
        JsonConfig toJsonConfig(String path) throws IOException;
        JsonNode toJsonNode(String path) throws IOException;
    }

}
