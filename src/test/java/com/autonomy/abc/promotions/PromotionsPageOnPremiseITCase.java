package com.autonomy.abc.promotions;

import com.autonomy.abc.config.OPTestBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.SchedulePage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class PromotionsPageOnPremiseITCase extends OPTestBase {

	public PromotionsPageOnPremiseITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private OPPromotionsPage promotionsPage;
	private SearchPage searchPage;
	private SchedulePage schedulePage;
	private DatePicker datePicker;
	private final Pattern pattern = Pattern.compile("\\s+");
    private PromotionService promotionService;
    private SearchActionFactory searchActionFactory;

	@Before
	public void setUp() throws MalformedURLException {
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());

		promotionsPage = (OPPromotionsPage) promotionService.deleteAll();
	}

	@Test
	public void testPromotionFieldTextRestriction() {
//        Search search = searchActionFactory.makeSearch("hot");
//        promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "hot"), search, 1);
//
//		promotionsPage.addFieldText("MATCH{hot}:DRECONTENT");
//
//		topNavBar.search("hot");
//		searchPage.selectLanguage("English", getConfig().getType().getName());
//		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("hot pot");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("hots");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		navBar.switchPage(NavBarTabId.PROMOTIONS);
//		promotionsPage.getPromotionLinkWithTitleContaining("hot").click();
//		promotionsPage.loadOrFadeWait();
//
//		promotionsPage.removableFieldText().removeAndWait();
//
//		topNavBar.search("hot");
//		searchPage.loadOrFadeWait();
//		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("hot chocolate");
//		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("hots");
//		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
//
//		navBar.switchPage(NavBarTabId.PROMOTIONS);
//		promotionsPage.getPromotionLinkWithTitleContaining("hot").click();
//		promotionsPage.loadOrFadeWait();
//
//		promotionsPage.fieldTextAddButton().click();
//		promotionsPage.fieldTextInputBox().sendKeys("<h1>hi</h1>");
//		promotionsPage.fieldTextTickConfirmButton().click();
//		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));
//		assertEquals("<h1>hi</h1>", promotionsPage.fieldTextValue());
//
//		promotionsPage.fieldTextEditButton().click();
//		promotionsPage.fieldTextInputBox().clear();
//		promotionsPage.fieldTextInputBox().sendKeys("MATCH{hot dog}:DRECONTENT");
//		promotionsPage.fieldTextTickConfirmButton().click();
//		promotionsPage.loadOrFadeWait();
//		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));
//
//		topNavBar.search("hot dog");
//		searchPage.loadOrFadeWait();
//		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("hot chocolate");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("hot");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("dog");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("hot dogs");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
        fail("TODO");
    }

	@Test
	public void testPromotionFieldTextOrRestriction() {
//		promotionsPage.setUpANewPromotion("English", "road", "Hotwire", "highway street", getConfig().getType().getName());
//
//		promotionsPage.addFieldText("MATCH{highway}:DRECONTENT OR MATCH{street}:DRECONTENT");
//
//		topNavBar.search("highway street");
//		searchPage.selectLanguage("English", getConfig().getType().getName());
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("road");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("ROAD");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("highway");
//		assertThat("Promoted documents are not visible", searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("street");
//		assertThat("Promoted documents are not visible", searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("highway street");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("street highway");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("street street");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("highwaystreet");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		topNavBar.search("highway AND street");
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
        fail("TODO");
	}

	@Test
	public void testFieldTextSubmitTextOnEnter() {
//		promotionsPage.setUpANewPromotion("English", "road", "Hotwire", "highway street", getConfig().getType().getName());
//
//		promotionsPage.fieldTextAddButton().click();
//		promotionsPage.loadOrFadeWait();
//		promotionsPage.fieldTextInputBox().sendKeys("TEST");
//		promotionsPage.fieldTextInputBox().sendKeys(Keys.RETURN);
//		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));
//		assertTrue("Field text cannot be submitted with an enter key", promotionsPage.fieldTextRemoveButton().isDisplayed());
        fail("TODO");
	}

	@Test
	public void testCreateFieldTextField() {
//		promotionsPage.setUpANewPromotion("Telugu", "మింగ్ వంశము", "Top Promotions", "Ming", getConfig().getType().getName());
//
//		promotionsPage.addFieldText("MATCH{Richard}:NAME");
//		topNavBar.search("Ming");
//		searchPage = getElementFactory().getSearchPage();
//		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
//
//		searchPage.expandFilter(SearchBase.Filter.FIELD_TEXT);
//		searchPage.loadOrFadeWait();
//		searchPage.fieldTextAddButton().click();
//		searchPage.fieldTextInput().sendKeys("MATCH{Richard}:NAME");
//		searchPage.fieldTextTickConfirm().click();
//		searchPage.loadOrFadeWait();
//		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
        fail("TODO");
	}
}
