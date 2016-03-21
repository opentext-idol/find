package com.autonomy.abc.config;

import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.users.UserConfigParser;
import com.autonomy.abc.selenium.users.JsonUserConfigParser;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.SafeClassLoader;
import com.fasterxml.jackson.databind.JsonNode;

class UserConfigParserFactory implements ParametrizedFactory<ApplicationType, UserConfigParser<JsonNode>> {
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
