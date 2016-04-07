package com.autonomy.abc.config;

import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.users.UserConfigParser;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.fasterxml.jackson.databind.JsonNode;

class UserConfigParserFactory implements ParametrizedFactory<ApplicationType, UserConfigParser<JsonNode>> {
    @Override
    public UserConfigParser<JsonNode> create(ApplicationType context) {
        switch (context) {
            case HOSTED:
                return new HsodUserConfigParser();
            case ON_PREM:
                return new IdolIsoUserConfigParser();
            default:
                throw new IllegalStateException("Unexpected application type: " + context);
        }
    }
}
