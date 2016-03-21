package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.users.UserConfigParser;
import com.autonomy.abc.selenium.users.JsonUserConfigParser;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.autonomy.abc.selenium.util.SafeClassLoader;
import com.fasterxml.jackson.databind.JsonNode;

public class UserConfigParserFactory implements ParametrizedFactory<ApplicationType, UserConfigParser<JsonNode>> {
    @Override
    public UserConfigParser<JsonNode> create(ApplicationType context) {
        switch (context) {
            case HOSTED:
                return new SafeClassLoader<>(JsonUserConfigParser.class, "com.autonomy.abc.selenium.config.HSODUserConfigParser").create();
            case ON_PREM:
                return new SafeClassLoader<>(JsonUserConfigParser.class, "com.autonomy.abc.selenium.config.OPUserConfigParser").create();
            default:
                throw new IllegalStateException("Unexpected application type: " + context);
        }
    }
}
