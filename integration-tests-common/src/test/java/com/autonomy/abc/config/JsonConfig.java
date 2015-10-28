package com.autonomy.abc.config;

import com.autonomy.abc.selenium.config.ApplicationType;
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
    private final Map<String, UserConfig> users;

    private JsonConfig(JsonNode node) throws MalformedURLException {
        this.app = new AppConfig(node.path("app"));
        this.selenium = new SeleniumConfig(node.path("selenium"));
        this.users = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = node.path("users").fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> user = iterator.next();
            users.put(user.getKey(), new UserConfig(user.getValue()));
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
    }

    public static JsonConfig readFile(File jsonFile) throws IOException {
        return (jsonFile == null) ? null : new JsonConfig(new ObjectMapper().readTree(jsonFile));
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

    public List<Browser> getBrowsers() {
        return this.selenium.browsers;
    }

    public User getDefaultUser() {
        return getUser("default");
    }

    public User getUser(String name) {
        return this.users.get(name).getUser();
    }

    public ApplicationType getAppType() {
        return this.app.type;
    }

    public static class AppConfig {
        private final ApplicationType type;
        private final URL url;

        private AppConfig(JsonNode node) throws MalformedURLException {
            String typeString = node.path("type").asText();
            type = (typeString.isEmpty() ? null : ApplicationType.fromString(typeString));
            url = getUrlOrNull(node.path("url"));
        }

        private AppConfig(AppConfig overrides, AppConfig defaults) {
            type = override(defaults.type, overrides.type);
            url = override(defaults.url, overrides.url);
        }

        private AppConfig overrideUsing(AppConfig overrides) {
            return overrides == null ? this : new AppConfig(overrides, this);
        }
        @Override
        public String toString() {
            return "{type=" + type + ", url=" + url + "}";
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

    public static class UserConfig {
        private final String auth;
        private final String username;
        private final String password;
        private final String email;
        private final User user;
        private final User.Role role;

        private UserConfig(JsonNode userNode) {
            auth = userNode.path("auth").asText();
            username = userNode.path("username").asText();
            password = userNode.path("password").asText();
            email = userNode.path("email").asText();
            // TODO: use auth
            role = User.Role.fromString(userNode.path("role").asText());
            role = new User(username, password, email, role);
        }

        public User getUser() {
            return user;
        }

        @Override
        public String toString() {
            return getUser().toString();
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
                + selenium + ", users=" + users + "}";
    }
}
