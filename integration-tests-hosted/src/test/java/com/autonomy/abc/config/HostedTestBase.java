package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Ignore
@RunWith(Parameterized.class)
public abstract class HostedTestBase extends ABCTestBase {
    public HostedTestBase(TestConfig config) {
        super(config);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.HOSTED);
        return parameters(applicationTypes);
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
