package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;

public class TopNavBarITCase extends ABCTestBase {
	public TopNavBarITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private TopNavBar topNavBar;

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = new TopNavBar(getDriver());
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final Collection<ApplicationType> applicationTypes = Arrays.asList(ApplicationType.HOSTED, ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Test
	public void testSideNavBarMinimize() {
		topNavBar.sideBarToggle();
		final String hiddenClass = "hide-navbar";
		assertThat("side bar not minimised", body.getAttribute("class").contains(hiddenClass));

		topNavBar.sideBarToggle();
		assertThat("side bar not maximised", !body.getAttribute("class").contains(hiddenClass));

		topNavBar.sideBarToggle();
		assertThat("side bar not minimised", body.getAttribute("class").contains(hiddenClass));

		getDriver().manage().window().setSize(new Dimension(500, 600));
		topNavBar.sideBarToggle();
		assertThat("side bar not maximised", !body.getAttribute("class").contains(hiddenClass));

		topNavBar.sideBarToggle();
		assertThat("side bar not minimised", body.getAttribute("class").contains(hiddenClass));

		topNavBar.sideBarToggle();
		assertThat("side bar not maximised", !body.getAttribute("class").contains(hiddenClass));
	}
}
