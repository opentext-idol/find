package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.promotions.CreateNewDynamicPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
//import com.autonomy.abc.selenium.page.promotions.SchedulePage;
//import com.autonomy.abc.selenium.page.search.SearchBase;
//import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private TopNavBar topNavBar;
	private PromotionsPage promotionsPage;
	private CreateNewDynamicPromotionsPage dynamicPromotionsPage;
	private DatePicker datePicker;
	private final Pattern pattern = Pattern.compile("\\s+");

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		promotionsPage = getElementFactory().getPromotionsPage();
//		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.newPromotionButton().click();
		assertThat("linked to wrong page", getDriver().getCurrentUrl().endsWith("promotions/new"));
		assertThat("linked to wrong page", topNavBar.getText(), containsString("Create New Promotion"));
	}


}
