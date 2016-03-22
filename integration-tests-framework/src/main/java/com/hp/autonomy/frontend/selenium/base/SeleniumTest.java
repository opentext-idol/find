package com.hp.autonomy.frontend.selenium.base;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.state.TestState;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBugRule;
import com.hp.autonomy.frontend.selenium.framework.state.SessionRegistryResource;
import com.hp.autonomy.frontend.selenium.framework.state.StateHelperRule;
import com.hp.autonomy.frontend.selenium.framework.artifacts.TestArtifactRule;
import com.hp.autonomy.frontend.selenium.framework.artifacts.StatementArtifactHandler;
import com.hp.autonomy.frontend.selenium.framework.logging.StatementLoggingHandler;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.SessionRegistry;
import com.hp.autonomy.frontend.selenium.control.Window;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runners.model.MultipleFailureException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
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
