package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.config.UserConfigParser;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.Dimension;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

class JsonConfig {
    private final AppConfig app;
    private final SeleniumConfig selenium;
    private final Map<String, User> users;
    private final Map<String, NewUser> newUsers;
    private UserConfigParser parser;

    private JsonConfig(JsonNode node) throws MalformedURLException {
        this.app = new AppConfig(node.path("app"));
        this.selenium = new SeleniumConfig(node.path("selenium"));

        this.users = new HashMap<>();
        this.newUsers = new HashMap<>();
        if (node.has("users") || node.has("newusers")) {
            parser = UserConfigParser.ofType(getAppType());

            Iterator<Map.Entry<String, JsonNode>> iterator = node.path("users").fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> userEntry = iterator.next();
                users.put(userEntry.getKey(), parser.parseUser(userEntry.getValue()));
            }

            iterator = node.path("newusers").fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> newUserEntry = iterator.next();
                newUsers.put(newUserEntry.getKey(), parser.parseNewUser(newUserEntry.getValue()));
            }
        }
    }

    private JsonConfig(JsonConfig overrides, JsonConfig defaults) {
        app = defaults.app.overrideUsing(overrides.app);
        selenium = defaults.selenium.overrideUsing(overrides.selenium);
        users = mapOverride(defaults.users, overrides.users);
        newUsers = mapOverride(defaults.newUsers, overrides.newUsers);
    }

    static JsonConfig readFile(File jsonFile) throws IOException {
        return (jsonFile == null) ? null : new JsonConfig(new ObjectMapper().readTree(jsonFile));
    }

    static JsonConfig fromURL(URL url) throws IOException {
        return (url == null) ? null : new JsonConfig(new ObjectMapper().readTree(url));
    }

    JsonConfig overrideUsing(JsonConfig overrides) {
        return (overrides == null) ? this : new JsonConfig(overrides, this);
    }

    URL getHubUrl() {
        return this.selenium.url;
    }

    List<Browser> getBrowsers() {
        return this.selenium.browsers;
    }

    Dimension getResolution() {
        return this.selenium.resolution;
    }

    /**
     * How long to implicitly wait
     * @return timeout, in seconds, or -1 if not set
     */
    int getTimeout() {
        return this.selenium.timeout;
    }

    User getUser(String name) {
        return this.users.get(name);
    }

    NewUser getNewUser(String name) {
        return this.newUsers.get(name);
    }

    NewUser generateRandomNewUser() {
        return parser.generateNewUser(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    ApplicationType getAppType() {
        return this.app.type;
    }

    URL getAppUrl(String appName) {
        return app.urls.get(appName);
    }

    private static class AppConfig {
        private final ApplicationType type;
        private final Map<String, URL> urls;

        private AppConfig(JsonNode node) throws MalformedURLException {
            type = readType(node.path("type"));
            urls = readUrls(node.fields());
        }

        private AppConfig(AppConfig overrides, AppConfig defaults) {
            type = override(defaults.type, overrides.type);
            urls = mapOverride(defaults.urls, overrides.urls);
        }

        private AppConfig overrideUsing(AppConfig overrides) {
            return overrides == null ? this : new AppConfig(overrides, this);
        }

        private ApplicationType readType(JsonNode node) {
            String typeString = node.asText();
            return typeString.isEmpty() ? null : ApplicationType.fromString(typeString);
        }

        private Map<String, URL> readUrls(Iterator<Map.Entry<String, JsonNode>> entries) throws MalformedURLException {
            Map<String, URL> urls = new HashMap<>();
            while (entries.hasNext()) {
                Map.Entry<String, JsonNode> entry = entries.next();
                if (!entry.getKey().equals("type")) {
                    urls.put(entry.getKey(), getUrlOrNull(entry.getValue()));
                }
            }
            return urls;
        }

        @Override
        public String toString() {
            return "{type=" + type + ", urls=" + urls + "}";
        }

    }

    private static class SeleniumConfig {
        private final URL url;
        private final List<Browser> browsers;
        private final Dimension resolution;
        private final int timeout;

        private SeleniumConfig(JsonNode node) throws MalformedURLException {
            url = getUrlOrNull(node.path("url"));
            browsers = readBrowsers(node.path("browsers"));
            resolution = readDimension(node.path("resolution"));
            timeout = node.path("timeout").asInt(-1);
        }

        private SeleniumConfig(SeleniumConfig overrides, SeleniumConfig defaults) {
            url = override(defaults.url, overrides.url);
            browsers = override(defaults.browsers, overrides.browsers);
            resolution = override(defaults.resolution, overrides.resolution);
            timeout = (overrides.timeout > 0) ? overrides.timeout : defaults.timeout;
        }

        private SeleniumConfig overrideUsing(SeleniumConfig overrides) {
            return overrides == null ? this : new SeleniumConfig(overrides, this);
        }

        private List<Browser> readBrowsers(JsonNode browsersNode) {
            if (browsersNode.isMissingNode()) {
                return null;
            }
            List<Browser> browsers = new ArrayList<>();
            for (JsonNode browserNode : browsersNode) {
                browsers.add(Browser.fromString(browserNode.asText()));
            }
            return browsers;
        }

        private Dimension readDimension(JsonNode dimensionNode) {
            if (dimensionNode.isMissingNode()) {
                return null;
            }
            int width = dimensionNode.get(0).asInt();
            int height = dimensionNode.get(1).asInt();
            return new Dimension(width, height);
        }

        @Override
        public String toString() {
            return "{browsers=" + browsers + ", url=" + url + ", resolution=" + resolution + ", timeout=" + timeout + "}";
        }
    }

    // helper methods
    private static URL getUrlOrNull(JsonNode node) throws MalformedURLException {
        return node.isMissingNode() ? null : new URL(node.asText());
    }

    private static <T> T override(T first, T second) {
        return second == null ? first : second;
    }

    private static <K, V> Map<K, V> mapOverride(Map<K, V> first, Map<K, V> second) {
        Map<K, V> newMap = new HashMap<>();
        if (first != null) {
            newMap.putAll(first);
        }
        if (second != null) {
            newMap.putAll(second);
        }
        return newMap;
    }

    @Override
    public String toString() {
        return "{app=" + app + ", selenium="
                + selenium + ", users=" + users + ", newusers=" + newUsers + "}";
    }
}
