package com.autonomy.abc.config;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.UserConfigParser;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class JsonConfig {

    private final AppConfig app;
    private final SeleniumConfig selenium;
    private final Map<String, User> users;
    private final Map<String, NewUser> newUsers;

    private JsonConfig(JsonNode node) throws MalformedURLException {
        this.app = new AppConfig(node.path("app"));
        this.selenium = new SeleniumConfig(node.path("selenium"));

        // user config is app-specific, must initialise after app
        // (this means that currently the app type must be specified in both configs) TODO: expose override constructor/factory
        Application application = Application.ofType(getAppType());
        UserConfigParser userConfigParser = application.getUserConfigParser();
        this.users = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = node.path("users").fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> userEntry = iterator.next();
            users.put(userEntry.getKey(), userConfigParser.parseUser(userEntry.getValue()));
        }

        this.newUsers = new HashMap<>();
        iterator = node.path("newusers").fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> newUserEntry = iterator.next();
            newUsers.put(newUserEntry.getKey(), userConfigParser.parseNewUser(newUserEntry.getValue()));
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

    public static JsonConfig readFile(File jsonFile) throws IOException {
        return (jsonFile == null) ? null : new JsonConfig(new ObjectMapper().readTree(jsonFile));
    }

    public static JsonConfig fromURL(URL url) throws IOException {
        return (url == null) ? null : new JsonConfig(new ObjectMapper().readTree(url));
    }

    public JsonConfig overrideUsing(JsonConfig overrides) throws IOException {
        return (overrides == null) ? this : new JsonConfig(overrides, this);
    }

    public URL getHubUrl() {
        return this.selenium.url;
    }

    public URL getWebappUrl() {
        return this.app.url;
    }

    public URL getFindUrl() {
        return this.app.findUrl;
    }

    public List<Browser> getBrowsers() {
        return this.selenium.browsers;
    }

    public User getDefaultUser() {
        return getUser("default");
    }

    public User getUser(String name) {
        return this.users.get(name);
    }

    public NewUser getNewUser(String name) {
        return this.newUsers.get(name);
    }

    public ApplicationType getAppType() {
        return this.app.type;
    }

    public URL getApiUrl() {
        return this.app.apiUrl;
    }

    public static class AppConfig {
        private final ApplicationType type;
        private final URL url;
        private final URL findUrl;
        private final URL apiUrl;

        private AppConfig(JsonNode node) throws MalformedURLException {
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

    public static class SeleniumConfig {
        private final URL url;
        private final List<Browser> browsers;

        private SeleniumConfig(JsonNode node) throws MalformedURLException {
            url = getUrlOrNull(node.path("url"));
            JsonNode browsersNode = node.path("browsers");
            if (browsersNode.isMissingNode()) {
                browsers = null;
            } else {
                browsers = new ArrayList<>();
                for (JsonNode browserNode : browsersNode) {
                    browsers.add(Browser.fromString(browserNode.asText()));
                }
            }
        }

        private SeleniumConfig(SeleniumConfig overrides, SeleniumConfig defaults) {
            url = override(defaults.url, overrides.url);
            browsers = override(defaults.browsers, overrides.browsers);
        }

        private SeleniumConfig overrideUsing(SeleniumConfig overrides) {
            return overrides == null ? this : new SeleniumConfig(overrides, this);
        }

        @Override
        public String toString() {
            return "{browsers=" + browsers + ", url=" + url + "}";
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
