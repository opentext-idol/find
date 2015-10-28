package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import com.fasterxml.jackson.databind.JsonNode;

// TODO: this does not belong in "selenium"!
public interface UserConfigParser {
    User parseUser(JsonNode userNode);
    NewUser parseNewUser(JsonNode newUserNode);
}
