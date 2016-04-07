package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.users.*;
import com.hp.autonomy.frontend.selenium.util.Factory;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.WebDriver;

public class OPUserConfigParser implements JsonUserConfigParser {
    @Override
    public User parseUser(JsonNode userNode) {
        String username = userNode.path("username").asText();
        IdolIsoAccount auth = new IdolIsoAccount(username, userNode.path("password").asText());
        Role role = Role.fromString(userNode.path("role").asText());
        return new User(auth, username, role);
    }

    @Override
    public NewUser parseNewUser(JsonNode newUserNode) {
        String username = newUserNode.path("username").asText();
        String password = newUserNode.path("password").asText();
        return new IdolIsoNewUser(username, password);
    }

    @Override
    public NewUser generateNewUser(String identifier) {
        return new IdolIsoNewUser(identifier, "pw");
    }

    @Override
    public AuthenticationStrategy getAuthenticationStrategy(Factory<WebDriver> driverFactory) {
        return NullAuthenticationStrategy.getInstance();
    }
}
