package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.navigation.SOElementFactory;
import com.autonomy.abc.selenium.users.SSOFailureException;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.fail;

@Ignore
@RunWith(Parameterized.class)
public abstract class ABCTestBase extends SeleniumTest<SearchOptimizerApplication<?>, SOElementFactory> {
	private User initialUser;
	private User currentUser;

	public ABCTestBase(final TestConfig config) {
		super(config, SearchOptimizerApplication.ofType(config.getType()));
		this.initialUser = config.getDefaultUser();
	}

	protected void postLogin() throws Exception {
		//Wait for page to load
		Thread.sleep(2000);
		// wait for the first page to load
		getElementFactory().getPromotionsPage();
	}

	@Before
	public final void abcSetUp() {
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

	protected final void loginAs(User user) {
		loginTo(getElementFactory().getLoginPage(), getDriver(), user);
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
		getElementFactory().getTopNavBar().logOut();
		currentUser = User.NULL;
	}

	protected final User getCurrentUser() {
		return currentUser;
	}
}
