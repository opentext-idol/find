package com.autonomy.abc.config;

import com.autonomy.abc.selenium.users.User;
import com.fasterxml.jackson.databind.JsonNode;

interface UserConfigParser {
    User parseUser(JsonNode userNode);
}
