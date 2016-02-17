package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.util.SafeClassLoader;
import com.fasterxml.jackson.databind.JsonNode;

// TODO: this does not belong in "selenium"!
public abstract class UserConfigParser {
    public abstract User parseUser(JsonNode userNode);

    public abstract NewUser parseNewUser(JsonNode newUserNode);

    public static UserConfigParser ofType(ApplicationType type) {
        switch (type) {
            case HOSTED:
                return new SafeClassLoader<>(UserConfigParser.class, "com.autonomy.abc.selenium.config.HSOUserConfigParser").create();
            case ON_PREM:
                return new SafeClassLoader<>(UserConfigParser.class, "com.autonomy.abc.selenium.config.OPUserConfigParser").create();
            default:
                throw new IllegalStateException("Unexpected application type: " + type);
        }
    }
}
