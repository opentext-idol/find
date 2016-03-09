package com.autonomy.abc.config;

import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.rules.KnownBugRule;
import com.autonomy.abc.framework.rules.SessionRegistryResource;
import com.autonomy.abc.framework.rules.StateHelperRule;
import com.autonomy.abc.framework.rules.TestArtifactRule;
import com.autonomy.abc.framework.statements.StatementArtifactHandler;
import com.autonomy.abc.framework.statements.StatementLoggingHandler;
import com.autonomy.abc.selenium.application.Application;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.SessionRegistry;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.navigation.ElementFactoryBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.MultipleFailureException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Ignore
@RunWith(Parameterized.class)
public abstract class SeleniumTest<A extends Application<? extends F>, F extends ElementFactoryBase> {
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    // testState is used by Rules/StatementHandlers
    private final TestState testState = TestState.get();
    private final TestConfig config;
    private final SessionRegistry sessions;
    private final A application;

    private String initialUrl;

    protected SeleniumTest(TestConfig config, A appUnderTest) {
        this.config = config;
        this.sessions = new SessionRegistry(config.getWebDriverFactory(), config.getWindowFactory());
        this.application = appUnderTest;
        this.initialUrl = getAppUrl();
    }

    protected final void setInitialUrl(String url) {
        initialUrl = url;
    }

    // StateHelperRule.finished() calls WebDriver.quit so must be the last thing called
    @Rule
    public RuleChain chain = RuleChain
            .outerRule(new SessionRegistryResource(this))
            .around(new StateHelperRule(this))
            .around(new TestArtifactRule(this))
            .around(new KnownBugRule());

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        final List<ApplicationType> applicationType = Arrays.asList(ApplicationType.HOSTED, ApplicationType.ON_PREM);
        return parameters(applicationType);
    }

    protected static List<Object[]> parameters(final Collection<ApplicationType> applicationTypes) throws IOException {
        return TestConfig.readConfigs(applicationTypes);
    }

    @Before
    public final void baseSetUp() {
        testState.addStatementHandler(new StatementLoggingHandler(this));
        testState.addStatementHandler(new StatementArtifactHandler(this));
        LOGGER.info("Starting " + testState.getTestName());
        LOGGER.info(config.toString());
        getDriver().get(initialUrl);
    }

    @After
    public final void baseTearDown() throws MultipleFailureException {
        testState.throwIfFailed();
    }

    public final SessionRegistry getSessionRegistry() {
        return sessions;
    }

    protected Session getMainSession() {
        return getSessionRegistry().getSession(0);
    }

    protected Window getWindow() {
        return getMainSession().getActiveWindow();
    }

    protected WebDriver getDriver() {
        return getMainSession().getDriver();
    }

    protected final TestConfig getConfig() {
        return config;
    }

    public A getApplication() {
        return application;
    }

    protected F getElementFactory() {
        return getApplication().elementFactory();
    }

    protected final String getAppUrl() {
        return getConfig().getAppUrl(getApplication());
    }

    protected Window launchInNewWindow(Application<?> newApp) {
        String newUrl = config.getAppUrl(newApp);
        Window window = getMainSession().openWindow(newUrl);
        newApp.inWindow(window);
        return window;
    }

    protected Session launchInNewSession(Application<?> newApp) {
        String newUrl = config.getAppUrl(newApp);
        Session session = getSessionRegistry().startSession(newUrl);
        newApp.inWindow(session.getActiveWindow());
        return session;
    }

    protected void redirectTo(Application<?> someApp) {
        String newUrl = config.getAppUrl(someApp);
        getWindow().goTo(newUrl);
        someApp.inWindow(getWindow());
    }

    protected boolean isHosted() {
        return getApplication().isHosted();
    }

    protected boolean isOnPrem() {
        return !getApplication().isHosted();
    }
}
