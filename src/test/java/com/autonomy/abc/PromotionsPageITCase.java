package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.page.PromotionsPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;

import static org.hamcrest.MatcherAssert.assertThat;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private PromotionsPage promotionsPage;

	@Before
	 public void setUp() throws MalformedURLException {
		promotionsPage = body.getPromotionsPage();
	}

	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.newPromotionButton().click();
		assertThat("linked to wrong page", getDriver().getCurrentUrl().endsWith("promotions/new"));
		assertThat("linked to wrong page", body.getText().contains("Create New Promotion"));
	}
}
