package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.config.ParsesUserConfig;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.autonomy.abc.selenium.util.SafeClassLoader;

public class UserConfigParserFactory implements ParametrizedFactory<ApplicationType, ParsesUserConfig> {
    @Override
    public ParsesUserConfig create(ApplicationType context) {
        switch (context) {
            case HOSTED:
                return new SafeClassLoader<>(ParsesUserConfig.class, "com.autonomy.abc.selenium.config.HSODUserConfigParser").create();
            case ON_PREM:
                return new SafeClassLoader<>(ParsesUserConfig.class, "com.autonomy.abc.selenium.config.OPUserConfigParser").create();
            default:
                throw new IllegalStateException("Unexpected application type: " + context);
        }
    }
}
