package com.autonomy.abc.config;

import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.rules.StateHelperRule;
import com.autonomy.abc.framework.rules.TestArtifactRule;
import com.autonomy.abc.framework.statements.StatementArtifactHandler;
import com.autonomy.abc.framework.statements.StatementLoggingHandler;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.util.ImplicitWaits;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.MultipleFailureException;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.fail;

@Ignore
@RunWith(Parameterized.class)
public abstract class ABCTestBase {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	// testState is used by Rules/StatementHandlers
	private final TestState testState = TestState.get();
	protected final TestConfig config;

	private final Application application;
	private WebDriver driver;
	// TODO: use getBody() instead
	public AppBody body;
	private ElementFactory elementFactory;
	private User initialUser;
	private String initialUrl;
	private User currentUser;

	// TODO: replace with single argument constructor
	public ABCTestBase(final TestConfig config, final String browser, final ApplicationType type, final Platform platform) {
		this(config);
	}

	public ABCTestBase(final TestConfig config) {
		this.config = config;
		this.application = Application.ofType(config.getType());
		this.initialUser = config.getDefaultUser();
		this.initialUrl = config.getWebappUrl();
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
		driver = config.getWebDriverFactory().create();
		ImplicitWaits.setImplicitWait(driver);

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

				try {
					new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()='Signed in']")));
					getDriver().get(config.getWebappUrl());
					currentUser = initialUser;
					postLogin();
				} catch (Exception f){
					LOGGER.error("Unable to login");
					LOGGER.error(e.toString());
					fail("Unable to login");
				}

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

	public final TestConfig getConfig() {
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

	protected final void loginAs(User user) {
		getElementFactory().getLoginPage().loginWith(user.getAuthProvider());
		currentUser = user;
	}

	protected final void logout() {
		getBody().logout();
		currentUser = User.NULL;
	}

	protected final User getCurrentUser() {
		return currentUser;
	}
}
