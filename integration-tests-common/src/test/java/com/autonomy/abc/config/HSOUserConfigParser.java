package com.autonomy.abc.config;

import com.autonomy.abc.selenium.users.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;

import java.util.Map;

public class HSOUserConfigParser implements UserConfigParser {
    @Override
    public User parseUser(JsonNode userNode) {
        Map<String, Object> authMap = new ObjectMapper().convertValue(userNode.path("auth"), new TypeReference<Map<String, Object>>() {
        });
        AuthProvider provider = HSOAuthFactory.fromMap(authMap);
        String username = userNode.path("username").asText();
        User.Role role = User.Role.fromString(userNode.path("role").asText());

        return new User(provider, username, role);
    }

}
