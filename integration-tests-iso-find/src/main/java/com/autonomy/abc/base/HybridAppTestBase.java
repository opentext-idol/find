package com.autonomy.abc.base;

import com.autonomy.abc.config.DualConfigLocator;
import com.autonomy.abc.fixtures.HybridTestSetupStrategy;
import com.autonomy.abc.selenium.actions.Command;
import com.autonomy.abc.selenium.actions.NullCommand;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.base.HybridTestParameterFactory;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.Before;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HybridAppTestBase<A extends Application<? extends F>, F extends ElementFactoryBase> extends SeleniumTest<A, F> {
    private HybridTestSetupStrategy setup;
    private Command postSetUpHook = NullCommand.getInstance();

    protected HybridAppTestBase(TestConfig config, A appUnderTest) {
        super(config, appUnderTest);
        setInitialUser(config.getDefaultUser());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        final List<ApplicationType> applicationType = Arrays.asList(ApplicationType.HOSTED, ApplicationType.ON_PREM);
        return parameters(applicationType);
    }

    protected static List<Object[]> parameters(final Collection<ApplicationType> applicationTypes) throws IOException {
        return new HybridTestParameterFactory(applicationTypes).create(new DualConfigLocator().getJsonConfig());
    }

    @Before
    public final void maybeLogIn() {
        setup.setUp(postSetUpHook);
    }

    protected final void setInitialUser(User user) {
        setup = new HybridTestSetupStrategy(getApplication(), user);
    }

    protected final User getInitialUser() {
        return setup.getInitialUser();
    }

    protected void setPostLoginHook(Command hook) {
        postSetUpHook = hook;
    }

    public boolean hasSetUp() {
        return setup.hasSetUp();
    }

    protected TestConfig getConfig() {
        return (TestConfig) super.getConfig();
    }
}
