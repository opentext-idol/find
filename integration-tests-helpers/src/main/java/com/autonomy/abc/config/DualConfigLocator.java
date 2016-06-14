package com.autonomy.abc.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.config.json.JsonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class DualConfigLocator {
    private final static String BASE_CONFIG_LOCATION = System.getProperty("com.autonomy.baseConfig");
    private final static String OVERRIDE_CONFIG_LOCATION = System.getProperty("com.autonomy.configFile");
    private final static Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

    private final ResourceJsonConfigReader reader;

    public DualConfigLocator() {
        reader = new ResourceJsonConfigReader();
    }

    public JsonConfig getJsonConfig() throws IOException {
        final JsonConfig base = maybeReadConfig(BASE_CONFIG_LOCATION);
        final JsonConfig override = maybeReadConfig(OVERRIDE_CONFIG_LOCATION);
        if (base == null) {
            LOGGER.warn("base config not found");
            return override;
        }
        return base.overrideUsing(override);
    }

    public JsonNode getJsonNode() throws IOException {
        return reader.toJsonNode(BASE_CONFIG_LOCATION);
    }

    private JsonConfig maybeReadConfig(final String path) throws IOException {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return reader.toJsonConfig(path);
    }

    private static class ResourceJsonConfigReader {
        public JsonConfig toJsonConfig(final String path) throws IOException {
            return path == null ? null : new JsonConfig(toJsonNode(path), new UserConfigParserFactory());
        }

        public JsonNode toJsonNode(final String path) throws IOException {
            return new ObjectMapper().readTree(urlForPath(path));
        }

        private static URL urlForPath(final String path) throws FileNotFoundException {
            final URL resource = ClassLoader.getSystemResource(path);
            if (resource == null) {
                throw new FileNotFoundException("Config file " + path + " not found in the resources directory");
            }
            LOGGER.info("using config " + resource);
            return resource;
        }
    }
}
