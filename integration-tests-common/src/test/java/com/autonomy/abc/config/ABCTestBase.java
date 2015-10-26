package com.autonomy.abc.config;

import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.rules.StateHelperRule;
import com.autonomy.abc.framework.rules.TestArtifactRule;
import com.autonomy.abc.framework.statements.StatementArtifactHandler;
import com.autonomy.abc.framework.statements.StatementLoggingHandler;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.Browser;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.util.ImplicitWaits;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.MultipleFailureException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

import static org.junit.Assert.fail;

@Ignore
@RunWith(Parameterized.class)
public abstract class ABCTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ABCTestBase.class);
	// testState is used by Rules/StatementHandlers
	private final TestState testState = TestState.get();
	public final TestConfig config;

	public final Browser browser;
	private final Platform platform;
	private final ApplicationType type;
	private final Application application;
	private WebDriver driver;
	// TODO: use getBody() instead
	public AppBody body;
	private ElementFactory elementFactory;

	// TODO: remove
	// only used for compatibility with on-prem tests that have not yet been updated
	@Deprecated
	protected SideNavBar sideNavBar;
	@Deprecated
	protected TopNavBar navBar;

	// TODO: replace with single argument constructor
	public ABCTestBase(final TestConfig config, final String browser, final ApplicationType type, final Platform platform) {
		this(config);
	}

	public ABCTestBase(final TestConfig config) {
		this.config = config;
		this.browser = config.getBrowser();
		this.platform = config.getPlatform();
		this.type = config.getType();
		this.application = Application.ofType(type);
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final List<ApplicationType> applicationType = Arrays.asList(ApplicationType.HOSTED, ApplicationType.ON_PREM);
		return parameters(applicationType);
	}

	protected static List<Object[]> parameters(final Collection<ApplicationType> applicationTypes) throws IOException {
		return TestConfig.readConfigs(applicationTypes);
	}
	// StateHelperRule.finished() calls WebDriver.quit so must be the last thing called
	@Rule
	public RuleChain chain = RuleChain.outerRule(new StateHelperRule(this)).around(new TestArtifactRule(this));

	protected void regularSetUp(){
		LOGGER.info("parameter-set: [" + config.getIndex() + "]; browser: " + browser + "; platform: " + platform + "; type: " + type);
		driver = config.createWebDriver(platform);
		ImplicitWaits.setImplicitWait(driver);

		testState.addStatementHandler(new StatementLoggingHandler(this));
		testState.addStatementHandler(new StatementArtifactHandler(this));

		driver.get(config.getWebappUrl());
		getDriver().manage().window().maximize();

		// no side/top bar until logged in
		body = getApplication().createAppBody(driver, null, null);
		elementFactory = getApplication().createElementFactory(driver);
	}

	protected void tryLogIn(){
		try {
			elementFactory.getLoginPage().loginWith(getApplication().createCredentials());
			//Wait for page to load
			Thread.sleep(2000);
			// now has side/top bar
			body = getBody();
		} catch (Exception e) {
			LOGGER.error("Unable to login");
			fail("Unable to login");
		}
	}

	@Before
	public void baseSetUp() throws MalformedURLException {
		regularSetUp();
		tryLogIn();
		sideNavBar = body.getSideNavBar();
		navBar = body.getTopNavBar();
	}

	@After
	public void baseTearDown() throws MultipleFailureException {
		testState.throwIfFailed();
	}

	public WebDriver getDriver() {
		return driver;
	}

	public TestConfig getConfig() {
		return config;
	}

	public Application getApplication() {
		return application;
	}

	public ElementFactory getElementFactory() {
		return elementFactory;
	}

	public AppBody getBody() {
		return getApplication().createAppBody(driver);
	}
}
