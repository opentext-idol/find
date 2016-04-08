package com.autonomy.abc.base;

import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

@Ignore
@RunWith(Parameterized.class)
public abstract class HostedTestBase extends SOTestBase {
    public HostedTestBase(TestConfig config) {
        super(config);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.HOSTED));
    }

    @Override
    public HSODElementFactory getElementFactory() {
        return (HSODElementFactory) super.getElementFactory();
    }

    @Override
    public HSODApplication getApplication() {
        return (HSODApplication) super.getApplication();
    }

    /**
     * Use the user for index/connection tests
     *
     * Must be called in constructor of test
     * A separate account is needed to create/delete
     * indexes due to resource limits
     */
    protected void useIndexTestsUser() {
        setInitialUser(getConfig().getUser("index_tests"));
    }
}
