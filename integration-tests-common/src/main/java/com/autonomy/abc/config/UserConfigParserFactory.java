package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.config.ParsesUserConfig;
import com.autonomy.abc.selenium.config.UserConfigParser;
import com.autonomy.abc.selenium.util.ParametrizedFactory;

public class UserConfigParserFactory implements ParametrizedFactory<ApplicationType, ParsesUserConfig> {
    @Override
    public ParsesUserConfig create(ApplicationType context) {
        return UserConfigParser.ofType(context);
    }
}
