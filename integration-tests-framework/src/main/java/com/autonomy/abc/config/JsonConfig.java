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
        users = new HashMap<>();
        users.putAll(defaults.users);
        if (overrides.users != null) {
            users.putAll(overrides.users);
        }
        newUsers = new HashMap<>();
        newUsers.putAll(defaults.newUsers);
        if (overrides.newUsers != null) {
            newUsers.putAll(overrides.newUsers);
        }
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

    URL getWebappUrl() {
        return this.app.url;
    }

    URL getFindUrl() {
        return this.app.findUrl;
    }

    URL getApiUrl() {
        return this.app.apiUrl;
    }

    List<Browser> getBrowsers() {
        return this.selenium.browsers;
    }

    Dimension getResolution() {
        return this.selenium.resolution;
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
        try {
            return getUrlOrNull(app.appNode.path(appName));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Application " + appName + " is not properly configured. Ensure that app." + appName + " is set in the config json");
        }
    }

    private static class AppConfig {
        private JsonNode appNode;
        private final ApplicationType type;
        private final URL url;
        private final URL findUrl;
        private final URL apiUrl;

        private AppConfig(JsonNode node) throws MalformedURLException {
            appNode = node;
            String typeString = node.path("type").asText();
            type = (typeString.isEmpty() ? null : ApplicationType.fromString(typeString));
            url = getUrlOrNull(node.path("url"));
            findUrl = getUrlOrNull(node.path("find"));
            apiUrl = getUrlOrNull(node.path("api"));
        }

        private AppConfig(AppConfig overrides, AppConfig defaults) {
            type = override(defaults.type, overrides.type);
            url = override(defaults.url, overrides.url);
            findUrl = override(defaults.findUrl, overrides.findUrl);
            apiUrl = override(defaults.apiUrl, overrides.apiUrl);
        }

        private AppConfig overrideUsing(AppConfig overrides) {
            return overrides == null ? this : new AppConfig(overrides, this);
        }
        @Override
        public String toString() {
            return "{type=" + type + ", url=" + url + ", find=" + findUrl + "}";
        }

    }

    private static class SeleniumConfig {
        private final URL url;
        private final List<Browser> browsers;
        private final Dimension resolution;

        private SeleniumConfig(JsonNode node) throws MalformedURLException {
            url = getUrlOrNull(node.path("url"));
            browsers = readBrowsers(node.path("browsers"));
            resolution = readDimension(node.path("resolution"));
        }

        private SeleniumConfig(SeleniumConfig overrides, SeleniumConfig defaults) {
            url = override(defaults.url, overrides.url);
            browsers = override(defaults.browsers, overrides.browsers);
            resolution = override(defaults.resolution, overrides.resolution);
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
            return "{browsers=" + browsers + ", url=" + url + ", resolution=" + resolution + "}";
        }
    }

    // helper methods
    private static URL getUrlOrNull(JsonNode node) throws MalformedURLException {
        return node.isMissingNode() ? null : new URL(node.asText());
    }

    private static <T> T override(T first, T second) {
        return second == null ? first : second;
    }

    @Override
    public String toString() {
        return "{app=" + app + ", selenium="
                + selenium + ", users=" + users + ", newusers=" + newUsers + "}";
    }
}
