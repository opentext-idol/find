package com.autonomy.abc.config.json;

import com.autonomy.abc.config.Browser;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Resolution;
import com.autonomy.abc.selenium.users.AuthenticationStrategy;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.users.UserConfigParser;
import com.autonomy.abc.selenium.util.Factory;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class JsonConfig {
    private final AppConfig app;
    private final SeleniumConfig selenium;
    private final Map<String, User> users;
    private final Map<String, NewUser> newUsers;
    private UserConfigParser<JsonNode> parser;

    public JsonConfig(JsonNode node, ParametrizedFactory<ApplicationType, UserConfigParser<JsonNode>> parserFactory) throws MalformedURLException {
        this.app = new AppConfig(node.path("app"));
        this.selenium = new SeleniumConfig(node.path("selenium"));

        this.users = new HashMap<>();
        this.newUsers = new HashMap<>();
        if (node.has("users") || node.has("newusers")) {
            this.parser = parserFactory.create(getAppType());

            Iterator<Map.Entry<String, JsonNode>> iterator = node.path("users").fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> userEntry = iterator.next();
                users.put(userEntry.getKey(), this.parser.parseUser(userEntry.getValue()));
            }

            iterator = node.path("newusers").fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> newUserEntry = iterator.next();
                newUsers.put(newUserEntry.getKey(), this.parser.parseNewUser(newUserEntry.getValue()));
            }
        }
    }

    private JsonConfig(JsonConfig overrides, JsonConfig defaults) {
        app = defaults.app.overrideUsing(overrides.app);
        selenium = defaults.selenium.overrideUsing(overrides.selenium);
        users = JsonConfigHelper.mapOverride(defaults.users, overrides.users);
        newUsers = JsonConfigHelper.mapOverride(defaults.newUsers, overrides.newUsers);
        parser = JsonConfigHelper.override(defaults.parser, overrides.parser);
    }

    public JsonConfig overrideUsing(JsonConfig overrides) {
        return (overrides == null) ? this : new JsonConfig(overrides, this);
    }

    public URL getHubUrl() {
        return this.selenium.getUrl();
    }

    public List<Browser> getBrowsers() {
        return this.selenium.getBrowsers();
    }

    public Resolution getResolution() {
        return this.selenium.getResolution();
    }

    /**
     * How long to implicitly wait
     * @return timeout, in seconds, or -1 if not set
     */
    public int getTimeout() {
        return this.selenium.getTimeout();
    }

    public User getUser(String name) {
        return this.users.get(name);
    }

    public NewUser getNewUser(String name) {
        return this.newUsers.get(name);
    }

    public NewUser generateRandomNewUser(String identifier) {
        return parser.generateNewUser(identifier);
    }

    public AuthenticationStrategy getAuthenticationStrategy(Factory<WebDriver> driverFactory) {
        return parser == null ? null : parser.getAuthenticationStrategy(driverFactory);
    }

    public ApplicationType getAppType() {
        return this.app.getType();
    }

    public URL getAppUrl(String appName) {
        return app.getUrl(appName);
    }

    @Override
    public String toString() {
        return "{app=" + app + ", selenium="
                + selenium + ", users=" + users + ", newusers=" + newUsers + "}";
    }
}
