package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.base.SOTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.iso.AboutPage;
import com.autonomy.abc.selenium.iso.OPISOElementFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static com.autonomy.abc.framework.state.TestStateAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;


public class AboutPageITCase extends SOTestBase {

	public AboutPageITCase(final TestConfig config) {
		super(config);
	}

	private AboutPage aboutPage;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Override
	public OPISOElementFactory getElementFactory() {
		return (OPISOElementFactory) super.getElementFactory();
	}

	@Before
	public void setUp() throws InterruptedException {
		aboutPage = getApplication().switchTo(AboutPage.class);
	}

	@Test
	public void testTableNavigation() {
		aboutPage.setTableSize("10");
		assertThat(aboutPage, containsText("Showing 1 to 10 of"));
		assertThat("page 1 is active" , aboutPage.isPageinateNumberActive(1));
		assertThat("page 2 is not active" , !aboutPage.isPageinateNumberActive(2));
		assertThat("page 3 is not active" , !aboutPage.isPageinateNumberActive(3));
		assertThat("previous button is disabled", aboutPage.isPreviousDisabled());

		for (int i = 1; i < 4; i++) {
			aboutPage.nextButton().click();
			assertThat("Previous button is enabled on page " + i, !aboutPage.isPreviousDisabled());
		}

		assertThat("next button is disabled", aboutPage.isNextDisabled());
		assertThat("page 1 should not be active" , !aboutPage.isPageinateNumberActive(1));
		assertThat("Page 2 should not be active" , !aboutPage.isPageinateNumberActive(2));
		assertThat("Page 3 should be active" , aboutPage.isPageinateNumberActive(3));

		for (int j = 0; j < 3 ; j++) {
			aboutPage.previousButton().click();
			assertThat("Next button is not enabled", !aboutPage.isNextDisabled());
		}

		assertThat("Have not been returned to first page", aboutPage.isPageinateNumberActive(1));
		assertThat("Previous button is not disabled", aboutPage.isPreviousDisabled());
	}

	@Test
	public void testTableSize() {
		aboutPage.setTableSize("10");
		assertThat(aboutPage, containsText("Showing 1 to 10 of"));

		aboutPage.setTableSize("25");
        assertThat(aboutPage, containsText("Showing 1 to 21 of"));

		aboutPage.setTableSize("10");
        assertThat(aboutPage, containsText("Showing 1 to 10 of"));
	}

	@Test
	public void testSearchTable() {
		aboutPage.searchInSearchBox("store");
		assertThat(aboutPage.libraryName(1), containsText("store"));
	}

}
