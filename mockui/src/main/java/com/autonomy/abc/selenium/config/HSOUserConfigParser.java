package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.users.HSONewUser;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;

import java.util.Map;

public class HSOUserConfigParser implements UserConfigParser {
    @Override
    public User parseUser(JsonNode userNode) {
        AuthProvider provider = authProvider(userNode.path("auth"));
        String username = userNode.path("username").asText();
        Role role = Role.fromString(userNode.path("role").asText());
        String apiKey = userNode.path("apikey").asText();

        return new User(provider, username, role).withApiKey(apiKey);
    }

    @Override
    public NewUser parseNewUser(JsonNode newUserNode) {
        AuthProvider provider = authProvider(newUserNode.path("auth"));
        String username = newUserNode.path("username").asText();
        String email = newUserNode.path("email").asText();

        return new HSONewUser(username, email, provider);
    }

    private AuthProvider authProvider(JsonNode authNode){
        Map<String, Object> authMap = new ObjectMapper().convertValue(authNode, new TypeReference<Map<String, Object>>() {
        });
        return HSOAuthFactory.fromMap(authMap);
    }

}
