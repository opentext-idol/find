package com.autonomy.abc.config;

import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.rules.StateHelperRule;
import com.autonomy.abc.framework.rules.TestArtifactRule;
import com.autonomy.abc.framework.statements.StatementArtifactHandler;
import com.autonomy.abc.framework.statements.StatementLoggingHandler;
import com.autonomy.abc.selenium.config.SearchOptimizerApplication;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.control.SessionRegistry;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.login.SSOFailureException;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
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

import static org.junit.Assert.fail;

@Ignore
@RunWith(Parameterized.class)
public abstract class ABCTestBase {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	// testState is used by Rules/StatementHandlers
	private final TestState testState = TestState.get();
	protected final TestConfig config;

	private final SearchOptimizerApplication application;
	private final SessionRegistry sessionRegistry;
	private WebDriver driver;
	// TODO: use getBody() instead
	public AppBody body;
	private ElementFactory elementFactory;
	private User initialUser;
	private String initialUrl;
	private User currentUser;

	public ABCTestBase(final TestConfig config) {
		this.config = config;
		this.application = SearchOptimizerApplication.ofType(config.getType());
		this.initialUser = config.getDefaultUser();
		this.initialUrl = config.getWebappUrl();
		this.sessionRegistry = new SessionRegistry(config.getWebDriverFactory());
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

	private void initialiseTest() {
		LOGGER.info(config.toString());
		driver = sessionRegistry.startSession().getDriver();

		testState.addStatementHandler(new StatementLoggingHandler(this));
		testState.addStatementHandler(new StatementArtifactHandler(this));
	}

	private void goToInitialPage() {
		getDriver().get(initialUrl);
		getDriver().manage().window().maximize();

		// no side/top bar until logged in
		body = getApplication().createAppBody(driver, null, null);
		elementFactory = getApplication().createElementFactory(driver);
	}

	protected void postLogin() throws Exception {
		//Wait for page to load
		Thread.sleep(2000);
		// now has side/top bar
		body = getBody();
		// wait for the first page to load
		getElementFactory().getPromotionsPage();
	}

	@Before
	public final void baseSetUp() {
		initialiseTest();
		goToInitialPage();
		if (!initialUser.equals(User.NULL)) {
			try {
				loginAs(initialUser);
				postLogin();
			} catch (Exception e) {
				LOGGER.error("Unable to login");
				LOGGER.error(e.toString());
				fail("Unable to login");
			}
		}
	}

	protected final void setInitialUser(User user) {
		initialUser = user;
	}

	protected final void setInitialUrl(String url) {
		initialUrl = url;
	}

	@After
	public final void baseTearDown() throws MultipleFailureException {
		testState.throwIfFailed();
	}

	public final WebDriver getDriver() {
		return driver;
	}

	public final SessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}

	public final TestConfig getConfig() {
		return config;
	}

	public SearchOptimizerApplication getApplication() {
		return application;
	}

	public ElementFactory getElementFactory() {
		return elementFactory;
	}

	public AppBody getBody() {
		return getApplication().createAppBody(driver);
	}

	protected final void loginAs(User user) {
		loginTo(getElementFactory().getLoginPage(), driver, user);
		currentUser = user;
	}

	protected final void loginTo(LoginPage loginPage, WebDriver webDriver, User user) {
		String redirectUrl = extractRedirectUrl(webDriver.getCurrentUrl()).replace("%23","#");
		try {
			loginPage.loginWith(user.getAuthProvider());
		} catch (SSOFailureException e) {
			LOGGER.info("Login failed, redirecting to " + redirectUrl);
			webDriver.get(redirectUrl);
		}
	}

	private String extractRedirectUrl(String fullUrl) {
		int startIndex = fullUrl.indexOf("redirect_url=") + "redirect_url=".length();
		String redirectUrl = fullUrl.substring(startIndex);
		return redirectUrl
				.replaceAll("%3A", ":")
				.replaceAll("%2F", "/");
	}

	protected final void logout() {
		getBody().logout();
		currentUser = User.NULL;
	}

	protected final User getCurrentUser() {
		return currentUser;
	}
}
