package com.autonomy.abc.base;

import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription;

import java.util.Collections;
import java.util.List;

public abstract class TestBase<A extends Application<? extends F>, F extends ElementFactoryBase> extends HybridAppTestBase<A, F> {

    protected TestBase(TestConfig config, A appUnderTest) {
        super(config, appUnderTest);
    }

    @Override
    protected List<RunOnlyIfDescription.Acceptable> rules() {
        return Collections.singletonList(new UserRoleStrategy());
    }
}