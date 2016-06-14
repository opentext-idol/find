package com.autonomy.abc.config;

import com.autonomy.abc.selenium.auth.HodAuthenticationStrategy;
import com.autonomy.abc.selenium.auth.HsodNewUser;
import com.autonomy.abc.selenium.auth.HsodUserBuilder;
import com.autonomy.abc.selenium.external.GoesToHodAuthPageFromGmail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import com.hp.autonomy.frontend.selenium.users.*;
import com.hp.autonomy.frontend.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class HsodUserConfigParser implements UserConfigParser<JsonNode> {
    private final String emailPrefix = "hodtestqa401";
    private final String emailSuffix = "@gmail.com";

    @Override
    public User parseUser(final JsonNode userNode) {
        final AuthProvider provider = authProvider(userNode.path("auth"));
        final String username = userNode.path("username").asText();
        final Role role = Role.fromString(userNode.path("role").asText());
        final String apiKey = userNode.path("apikey").asText();
        final String domain = userNode.path("domain").asText();

        return new HsodUserBuilder(username)
                .setAuthProvider(provider)
                .setRole(role)
                .setApiKey(apiKey)
                .setDomain(domain)
                .build();
    }

    @Override
    public NewUser parseNewUser(final JsonNode newUserNode) {
        final AuthProvider provider = authProvider(newUserNode.path("auth"));
        final String username = newUserNode.path("username").asText();
        final String email = newUserNode.path("email").asText();

        return new HsodNewUser(username, email, provider);
    }

    private AuthProvider authProvider(final JsonNode authNode){
        final Map<String, Object> authMap = new ObjectMapper().convertValue(authNode, new TypeReference<Map<String, Object>>() {
        });
        return HsodAuthFactory.fromMap(authMap);
    }

    @Override
    public NewUser generateNewUser(final String identifier) {
        return new HsodNewUser(identifier, gmailString(identifier), getAuthProvider());
    }

    private String gmailString(final String extra) {
        return emailPrefix + "+" + extra + emailSuffix;
    }

    private GoogleAuth getAuthProvider() {
        final String password = "qoxntlozubjaamyszerfk";
        return new GoogleAuth(emailPrefix + emailSuffix, password);
    }

    @Override
    public AuthenticationStrategy getAuthenticationStrategy(final Factory<WebDriver> driverFactory) {
        return new HodAuthenticationStrategy(driverFactory, new GoesToHodAuthPageFromGmail(getAuthProvider()));
    }
}
