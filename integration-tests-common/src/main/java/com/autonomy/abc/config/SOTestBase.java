package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.fail;

@Ignore
@RunWith(Parameterized.class)
public abstract class SOTestBase extends SeleniumTest<SearchOptimizerApplication<?>, SOElementFactory> {
	private User initialUser;
	private boolean hasSetUp = false;

	public SOTestBase(final TestConfig config) {
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
	public final void maybeLogIn() {
		if (!initialUser.equals(User.NULL)) {
			try {
				getApplication().loginService().login(initialUser);
				postLogin();
				hasSetUp = true;
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

	protected final User getInitialUser() {
		return initialUser;
	}

	public boolean hasSetUp() {
		return hasSetUp;
	}
}
