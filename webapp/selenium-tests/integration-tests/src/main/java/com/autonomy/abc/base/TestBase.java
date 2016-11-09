package com.autonomy.abc.base;

import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription;

import java.util.Collections;
import java.util.List;

public abstract class TestBase<A extends Application<? extends F>, F extends ElementFactoryBase> extends HybridAppTestBase<A, F> {

    protected TestBase(TestConfig config, A appUnderTest) {
        super(config, appUnderTest, role(config));
    }

    private static String role(TestConfig config) {
        String userRole = System.getProperty("userRole");

        if(userRole == null) {
            if(config.getType() == ApplicationType.HOSTED) {
                return "find";
            }

            return "bifhi";
        }

        return userRole;
    }

    @Override
    protected List<RunOnlyIfDescription.Acceptable> rules(SeleniumTest<A, F> test) {
        return Collections.singletonList(new UserRoleStrategy(test));
    }
}