package com.autonomy.abc.config;

import com.autonomy.abc.selenium.auth.IdolIsoAccount;
import com.autonomy.abc.selenium.auth.IdolIsoNewUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.hp.autonomy.frontend.selenium.users.*;
import com.hp.autonomy.frontend.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

public class IdolIsoUserConfigParser implements UserConfigParser<JsonNode> {
    @Override
    public User parseUser(final JsonNode userNode) {
        final String username = userNode.path("username").asText();
        final IdolIsoAccount auth = new IdolIsoAccount(username, userNode.path("password").asText());
        final Role role = Role.fromString(userNode.path("role").asText());
        return new User(auth, username, role);
    }

    @Override
    public NewUser parseNewUser(final JsonNode newUserNode) {
        final String username = newUserNode.path("username").asText();
        final String password = newUserNode.path("password").asText();
        return new IdolIsoNewUser(username, password);
    }

    @Override
    public NewUser generateNewUser(final String identifier) {
        return new IdolIsoNewUser(identifier, "pw");
    }

    @Override
    public AuthenticationStrategy getAuthenticationStrategy(final Factory<WebDriver> driverFactory) {
        return NullAuthenticationStrategy.getInstance();
    }
}
