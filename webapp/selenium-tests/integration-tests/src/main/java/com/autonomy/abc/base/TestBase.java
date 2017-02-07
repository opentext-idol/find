/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.base;

import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription.Acceptable;

import java.util.Collections;
import java.util.List;

public abstract class TestBase<A extends Application<? extends F>, F extends ElementFactoryBase> extends HybridAppTestBase<A, F> {

    protected TestBase(final TestConfig config, final A appUnderTest) {
        super(config, appUnderTest, role(config));
    }

    private static String role(final TestConfig config) {
        final String userRole = System.getProperty("userRole");

        if(userRole == null) {
            if(config.getType() == ApplicationType.HOSTED) {
                return "find";
            }

            return "bifhi";
        }

        return userRole;
    }

    @Override
    protected List<Acceptable> rules(final SeleniumTest<A, F> test) {
        return Collections.singletonList(new UserRoleStrategy(test));
    }
}
