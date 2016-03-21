package com.autonomy.abc.base;

import com.autonomy.abc.base.HybridTestParameterFactory;
import com.autonomy.abc.base.SeleniumTest;
import com.autonomy.abc.config.SOConfigLocator;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final List<ApplicationType> applicationType = Arrays.asList(ApplicationType.HOSTED, ApplicationType.ON_PREM);
		return parameters(applicationType);
	}

	protected static List<Object[]> parameters(final Collection<ApplicationType> applicationTypes) throws IOException {
		return new HybridTestParameterFactory(applicationTypes).create(new SOConfigLocator().getJsonConfig());
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
