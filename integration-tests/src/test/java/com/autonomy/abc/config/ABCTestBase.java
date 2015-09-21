package com.autonomy.abc.config;

import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.rules.StateHelperRule;
import com.autonomy.abc.framework.rules.TestArtifactRule;
import com.autonomy.abc.framework.statements.StatementArtifactHandler;
import com.autonomy.abc.framework.statements.StatementLoggingHandler;
import com.autonomy.abc.selenium.config.ApplicationType;
//import com.autonomy.abc.selenium.config.Timeouts;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.*;
//import com.autonomy.abc.selenium.util.ImplicitWaits;
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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

import static org.junit.Assert.fail;

@Ignore
@RunWith(Parameterized.class)
public class ABCTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ABCTestBase.class);
	private final static Set<String> USER_BROWSERS;
	private final static Set<ApplicationType> APPLICATION_TYPES;
	// testState is used by Rules/StatementHandlers
	private final TestState testState = TestState.get();

	public final TestConfig config;
	public final String browser;
	private final Platform platform;
	private final Application application;
	private final ApplicationType type;
	private WebDriver driver;
	// TODO: use getBody() instead
	public AppBody body;
	private ElementFactory elementFactory;

	static {
		final String[] allBrowsers = {"firefox", "internet explorer", "chrome"};
		final String browserProperty = System.getProperty("com.autonomy.browsers");
		final String applicationTypeProperty = System.getProperty("com.autonomy.applicationType");

		if (browserProperty == null) {
			USER_BROWSERS = new HashSet<>(Arrays.asList(allBrowsers));
		} else {
			USER_BROWSERS = new HashSet<>();

			for (final String browser : allBrowsers) {
				if (browserProperty.contains(browser)) {
					USER_BROWSERS.add(browser);
				}
			}
		}

		if (applicationTypeProperty == null) {
			APPLICATION_TYPES = EnumSet.allOf(ApplicationType.class);
		} else {
			APPLICATION_TYPES = EnumSet.noneOf(ApplicationType.class);

			for (final ApplicationType applicationType : ApplicationType.values()) {
				if (applicationTypeProperty.contains(applicationType.getName())) {
					APPLICATION_TYPES.add(applicationType);
				}
			}
		}
	}

	public ABCTestBase(final TestConfig config, final String browser, final ApplicationType type, final Platform platform) {
		this.config = config;
		this.browser = browser;
		this.platform = platform;
		this.type = type;
		this.application = Application.ofType(type);
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final Collection<ApplicationType> applicationType = Arrays.asList(ApplicationType.HOSTED, ApplicationType.ON_PREM);
		return parameters(applicationType);
	}

	protected static List<Object[]> parameters(final Iterable<ApplicationType> applicationTypes) throws MalformedURLException {
		final List<Object[]> output = new ArrayList<>();

		for (final ApplicationType type : applicationTypes) {
			if (APPLICATION_TYPES.contains(type)) {
				for (final String browser : USER_BROWSERS) {
					output.add(new Object[]{
							new TestConfig(output.size(), type),
							browser,
							type,
							Platform.WINDOWS
					});
				}
			}
		}
		return output;
	}
	// StateHelperRule.finished() calls WebDriver.quit so must be the last thing called
	@Rule
	public RuleChain chain = RuleChain.outerRule(new StateHelperRule(this)).around(new TestArtifactRule(this));

	@Before
	public void baseSetUp() throws MalformedURLException {
		LOGGER.info("parameter-set: [" + config.getIndex() + "]; browser: " + browser + "; platform: " + platform + "; type: " + type);
		driver = config.createWebDriver(browser, platform);
		ImplicitWaits.setImplicitWait(driver);

		testState.addStatementHandler(new StatementLoggingHandler(this));
		testState.addStatementHandler(new StatementArtifactHandler(this));

		driver.get(config.getWebappUrl());
		getDriver().manage().window().maximize();

		// no side/top bar until logged in
		body = application.createAppBody(driver, null, null);
		elementFactory = application.createElementFactory(driver);
		try {
			elementFactory.getLoginPage().loginWith(application.createCredentials());
			// now has side/top bar
			body = getBody();
		} catch (Exception e) {
			LOGGER.error("Unable to login");
			fail("Unable to login");
		}
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
		return application.createAppBody(driver);
	}
}
