package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.util.SafeClassLoader;

// TODO: this does not belong in "selenium"!
@Deprecated
public abstract class UserConfigParser implements ParsesUserConfig {
    public static UserConfigParser ofType(ApplicationType type) {
        switch (type) {
            case HOSTED:
                return new SafeClassLoader<>(UserConfigParser.class, "com.autonomy.abc.selenium.config.HSODUserConfigParser").create();
            case ON_PREM:
                return new SafeClassLoader<>(UserConfigParser.class, "com.autonomy.abc.selenium.config.OPUserConfigParser").create();
            default:
                throw new IllegalStateException("Unexpected application type: " + type);
        }
    }
}
