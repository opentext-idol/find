package com.autonomy.abc.base;

import com.autonomy.abc.config.SOConfigLocator;
import com.hp.autonomy.frontend.selenium.base.HybridTestParameterFactory;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.hp.autonomy.frontend.selenium.users.User;
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
	private IsoSetupStrategy setup;

	public SOTestBase(final TestConfig config) {
		super(config, SearchOptimizerApplication.ofType(config.getType()));
		setInitialUser(config.getDefaultUser());
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final List<ApplicationType> applicationType = Arrays.asList(ApplicationType.HOSTED, ApplicationType.ON_PREM);
		return parameters(applicationType);
	}

	protected static List<Object[]> parameters(final Collection<ApplicationType> applicationTypes) throws IOException {
		return new HybridTestParameterFactory(applicationTypes).create(new SOConfigLocator().getJsonConfig());
	}

	@Before
	public final void maybeLogIn() {
		setup.setUp();
	}

	protected final void setInitialUser(User user) {
		setup = new IsoSetupStrategy(getApplication(), user);
	}

	protected final User getInitialUser() {
		return setup.getInitialUser();
	}

	public boolean hasSetUp() {
		return setup.hasSetUp();
	}
}
