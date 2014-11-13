package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.page.AboutPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;

import static org.hamcrest.MatcherAssert.assertThat;

public class AboutPageITCase extends ABCTestBase {

	public AboutPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private AboutPage aboutPage;

	@Before
	public void setUp() throws MalformedURLException {
		aboutPage = body.getAboutPage();
	}

	@Test
	public void testTableNavigation() {
		aboutPage.setTableSize("10");
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 10 of"));
		assertThat("Page 1 should be active" , aboutPage.isPageinateNumberActive(1));
		assertThat("Page 2 should not be active" , !aboutPage.isPageinateNumberActive(2));
		assertThat("Page 3 should not be active" , !aboutPage.isPageinateNumberActive(3));
		assertThat("Previous button is not disabled", aboutPage.isPreviousDisabled());

		for (int i = 0; i < 3; i++) {
			aboutPage.nextButton().click();
			assertThat("Previous button is not enabled", !aboutPage.isPreviousDisabled());
		}

		assertThat("Next button is not disabled", aboutPage.isNextDisabled());
		assertThat("Page 1 should not be active" , !aboutPage.isPageinateNumberActive(1));
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
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 10 of"));

		aboutPage.setTableSize("25");
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 25 of"));

		aboutPage.setTableSize("10");
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 10 of"));
	}

}
