package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.external.GoesToHodAuthPageFromGmail;
import com.autonomy.abc.selenium.users.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class HsodUserConfigParser implements JsonUserConfigParser {
    // TODO: move to config
    private final String emailPrefix = "hodtestqa401";
    private final String emailSuffix = "@gmail.com";
    private final String password = "qoxntlozubjaamyszerfk";

    @Override
    public User parseUser(JsonNode userNode) {
        AuthProvider provider = authProvider(userNode.path("auth"));
        String username = userNode.path("username").asText();
        Role role = Role.fromString(userNode.path("role").asText());
        String apiKey = userNode.path("apikey").asText();
        String domain = userNode.path("domain").asText();

        return new HsodUserBuilder(username)
                .setAuthProvider(provider)
                .setRole(role)
                .setApiKey(apiKey)
                .setDomain(domain)
                .build();
    }

    @Override
    public NewUser parseNewUser(JsonNode newUserNode) {
        AuthProvider provider = authProvider(newUserNode.path("auth"));
        String username = newUserNode.path("username").asText();
        String email = newUserNode.path("email").asText();

        return new HsodNewUser(username, email, provider);
    }

    private AuthProvider authProvider(JsonNode authNode){
        Map<String, Object> authMap = new ObjectMapper().convertValue(authNode, new TypeReference<Map<String, Object>>() {
        });
        return HsodAuthFactory.fromMap(authMap);
    }

    @Override
    public NewUser generateNewUser(String identifier) {
        return new HsodNewUser(identifier, gmailString(identifier), getAuthProvider());
    }

    private String gmailString(String extra) {
        return emailPrefix + "+" + extra + emailSuffix;
    }

    private GoogleAuth getAuthProvider() {
        return new GoogleAuth(emailPrefix + emailSuffix, password);
    }

    @Override
    public AuthenticationStrategy getAuthenticationStrategy(Factory<WebDriver> driverFactory) {
        return new HodAuthenticationStrategy(driverFactory, new GoesToHodAuthPageFromGmail(getAuthProvider()));
    }
}
