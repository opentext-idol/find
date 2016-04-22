package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.selenium.iso.IsoAboutPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;


public class AboutPageITCase extends IdolIsoTestBase {
	private IsoAboutPage aboutPage;

	public AboutPageITCase(final TestConfig config) {
		super(config);
	}

	@Before
	public void setUp() throws InterruptedException {
		aboutPage = getApplication().switchTo(IsoAboutPage.class);
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
