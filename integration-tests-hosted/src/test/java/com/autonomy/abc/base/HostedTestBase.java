package com.autonomy.abc.base;

import com.autonomy.abc.fixtures.IsoPostLoginHook;
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
public abstract class HostedTestBase extends HybridAppTestBase<HSODApplication, HSODElementFactory> {
    public HostedTestBase(TestConfig config) {
        super(config, new HSODApplication());
        setPostLoginHook(new IsoPostLoginHook(getApplication()));
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.HOSTED));
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
