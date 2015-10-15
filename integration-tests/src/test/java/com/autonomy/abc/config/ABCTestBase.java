package com.autonomy.abc.config;

import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.rules.StateHelperRule;
import com.autonomy.abc.framework.rules.TestArtifactRule;
import com.autonomy.abc.framework.statements.StatementArtifactHandler;
import com.autonomy.abc.framework.statements.StatementLoggingHandler;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.config.HSOApplication;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.*;
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

import java.net.MalformedURLException;
import java.util.*;

import static org.junit.Assert.fail;

@Ignore
@RunWith(Parameterized.class)
public abstract class ABCTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ABCTestBase.class);
	private final static Set<String> USER_BROWSERS;
	private final static Set<ApplicationType> APPLICATION_TYPES;
	// application should really be a final instance variable, but this would require changing the constructor of every ITCase
	private final static Application APPLICATION;
	// testState is used by Rules/StatementHandlers
	private final TestState testState = TestState.get();

	public final TestConfig config;
	public final String browser;
	private final Platform platform;
	private final ApplicationType type;
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

	static {
		final String[] allBrowsers = {"firefox", "internet explorer", "chrome"};
		final String browserProperty = System.getProperty("com.autonomy.browsers");
		final String applicationTypeProperty = System.getProperty("com.autonomy.applicationType");
		final String applicationProperty = System.getProperty("com.autonomy.applicationClass");

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

		if (applicationProperty == null) {
			// TODO: should instead throw IllegalStateException
			APPLICATION = new HSOApplication();
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
		} else {
			try {
				APPLICATION = (Application) Class.forName(applicationProperty).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
			APPLICATION_TYPES = EnumSet.noneOf(ApplicationType.class);
			APPLICATION_TYPES.add(APPLICATION.getType());
		}

	}

	public ABCTestBase(final TestConfig config, final String browser, final ApplicationType type, final Platform platform) {
		this.config = config;
		this.browser = browser;
		this.platform = platform;
		this.type = type;
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
		body = getApplication().createAppBody(driver, null, null);
		elementFactory = getApplication().createElementFactory(driver);
		try {
			elementFactory.getLoginPage().loginWith(getApplication().createCredentials());
			// now has side/top bar
			body = getBody();
		} catch (Exception e) {
			LOGGER.error("Unable to login");
			fail("Unable to login");
		}
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
		return APPLICATION;
	}

	public ElementFactory getElementFactory() {
		return elementFactory;
	}

	public AppBody getBody() {
		return getApplication().createAppBody(driver);
	}
}
