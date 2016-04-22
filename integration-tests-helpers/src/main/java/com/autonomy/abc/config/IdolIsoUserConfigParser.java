package com.autonomy.abc.config;

import com.autonomy.abc.selenium.auth.IdolIsoAccount;
import com.autonomy.abc.selenium.auth.IdolIsoNewUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.hp.autonomy.frontend.selenium.users.*;
import com.hp.autonomy.frontend.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

public class IdolIsoUserConfigParser implements UserConfigParser<JsonNode> {
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
