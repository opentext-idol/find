/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription.Acceptable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class TestBase<A extends Application<? extends F>, F extends ElementFactoryBase> extends HybridAppTestBase<A, F> {
    TestBase(final TestConfig config, final A appUnderTest) {
        this(config, appUnderTest, null);
    }

    TestBase(final TestConfig config, final A appUnderTest, final UserRole initialUserRole) {
        super(config, appUnderTest, Optional.ofNullable(initialUserRole).map(UserRole::getConfigId).orElse("default"));
    }

    @Override
    protected List<Acceptable> rules(final SeleniumTest<A, F> test) {
        return Collections.singletonList(new UserRoleStrategy(UserRole.activeRole()));
    }
}
