package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.selenium.util.Factory;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.WebDriver;

public class OPUserConfigParser implements UserConfigParser {
    @Override
    public User parseUser(JsonNode userNode) {
        String username = userNode.path("username").asText();
        OPAccount auth = new OPAccount(username, userNode.path("password").asText());
        Role role = Role.fromString(userNode.path("role").asText());
        return new User(auth, username, role);
    }

    @Override
    public NewUser parseNewUser(JsonNode newUserNode) {
        String username = newUserNode.path("username").asText();
        String password = newUserNode.path("password").asText();
        return new OPNewUser(username, password);
    }

    @Override
    public NewUser generateNewUser(String identifier) {
        return new OPNewUser(identifier, "pw");
    }

    @Override
    public AuthenticationStrategy getAuthenticationStrategy(Factory<WebDriver> driverFactory) {
        return NullAuthenticationStrategy.getInstance();
    }
}
