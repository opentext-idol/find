package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.promotions.CreateNewDynamicPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class CreateNewDynamicPromotionsITCase extends ABCTestBase {

	public CreateNewDynamicPromotionsITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private SearchPage searchPage;
	private PromotionsPage promotionsPage;
	private TopNavBar topNavBar;
	private CreateNewDynamicPromotionsPage dynamicPromotionsPage;

	@Before
	public void setUp() {
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();
		topNavBar = body.getTopNavBar();
		topNavBar.search("fox");
		searchPage = body.getSearchPage();
	}

	@Test
	public void testDynamicPromotionCreation() {
		topNavBar.search("lapin");
		searchPage.selectLanguage("French", getConfig().getType().getName());
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));

		final String firstDocTitle = searchPage.getSearchResultTitle(1);
		searchPage.promoteThisQueryButton().click();
		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.loadOrFadeWait();
		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("promotions/create-dynamic/"));
		assertThat("Wrong stage of wizard", dynamicPromotionsPage.spotlightType("Hotwire").isDisplayed());
		assertThat("Continue button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.continueButton("spotlightType"), "disabled"));

		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();
		assertThat("Wrong wizard step", dynamicPromotionsPage.triggerAddButton().isDisplayed());
		assertThat("Finish button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
		assertThat("Trigger add button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 0);

		dynamicPromotionsPage.addSearchTrigger("rabbit");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 1);
		assert(dynamicPromotionsPage.getSearchTriggersList().contains("rabbit"));

		dynamicPromotionsPage.addSearchTrigger("bunny");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 2);
		assert(dynamicPromotionsPage.getSearchTriggersList().containsAll(Arrays.asList("bunny", "rabbit")));

		dynamicPromotionsPage.addSearchTrigger("hare");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 3);
		assert(dynamicPromotionsPage.getSearchTriggersList().containsAll(Arrays.asList("bunny", "rabbit", "hare")));

		// Hare is not a word for bunny
		dynamicPromotionsPage.removeSearchTrigger("hare");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 2);
		assert(dynamicPromotionsPage.getSearchTriggersList().containsAll(Arrays.asList("bunny", "rabbit")));
		assert(!dynamicPromotionsPage.getSearchTriggersList().contains("hare"));

		dynamicPromotionsPage.finishButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		assertEquals(searchPage.getSelectedLanguage(), "French");
		assertThat("Wrong search performed", searchPage.searchTitle().getText().equals("bunny rabbit"));
		assertEquals(searchPage.promotionsSummaryList(false).get(0), firstDocTitle);
		assertEquals(searchPage.promotionsLabel().getText(), "Top Promotions");
	}

	@Test
	public void testAddRemoveTriggerTermsAndCancel() {
		topNavBar.search("orange");
		searchPage.selectLanguage("Afrikaans", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();

		assertThat("Wizard has not progressed to Select the position", dynamicPromotionsPage.getText().contains("Select Promotion Triggers"));
		assertThat("Trigger add button is not disabled when text box is empty", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));
		assertThat("Finish button is not disabled when there are no match terms", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
		assertThat("Cancel button is not enabled when there are no match terms", !dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.cancelButton("triggers"), "disabled"));

		dynamicPromotionsPage.addSearchTrigger("animal");
		assertThat("Finish button is not enabled when a trigger is added", !dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
		assertThat("animal search trigger not added", dynamicPromotionsPage.getSearchTriggersList().contains("animal"));

		dynamicPromotionsPage.removeSearchTrigger("animal");
		assertThat("animal search trigger not removed", !dynamicPromotionsPage.getSearchTriggersList().contains("animal"));
		assertThat("Promote button is not disabled when no triggers are added", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));

		dynamicPromotionsPage.addSearchTrigger("bushy tail");
		assertThat("Number of triggers does not equal 2", dynamicPromotionsPage.getSearchTriggersList().size() == 2);
		assertThat("bushy search trigger not added", dynamicPromotionsPage.getSearchTriggersList().contains("bushy"));
		assertThat("tail search trigger not added", dynamicPromotionsPage.getSearchTriggersList().contains("tail"));

		dynamicPromotionsPage.removeSearchTrigger("tail");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("bushy search trigger not present", dynamicPromotionsPage.getSearchTriggersList().contains("bushy"));
		assertThat("tail search trigger not removed", !dynamicPromotionsPage.getSearchTriggersList().contains("tail"));

		dynamicPromotionsPage.cancelButton("triggers").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
	}

	@Test
	public void testWhitespaceTrigger() {
		topNavBar.search("orange");
		searchPage.selectLanguage("Afrikaans", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();

		assertThat("Trigger add button is not disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));

		dynamicPromotionsPage.tryClickThenTryParentClick(dynamicPromotionsPage.triggerAddButton());
		assertThat("Number of triggers does not equal 0", dynamicPromotionsPage.getSearchTriggersList().size() == 0);

		dynamicPromotionsPage.addSearchTrigger("trigger");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

		dynamicPromotionsPage.addSearchTrigger("   ");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

		dynamicPromotionsPage.addSearchTrigger(" trigger");
		assertThat("Whitespace at beginning should be ignored", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

		dynamicPromotionsPage.addSearchTrigger("\t");
		assertThat("Whitespace at beginning should be ignored", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
	}

	@Test
	public void testQuotesTrigger() {
		topNavBar.search("orange");
		searchPage.selectLanguage("Afrikaans", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();

		assertThat("Trigger add button is not disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));

		dynamicPromotionsPage.tryClickThenTryParentClick(dynamicPromotionsPage.triggerAddButton());

		assertThat("Number of triggers does not equal 0", dynamicPromotionsPage.getSearchTriggersList().size() == 0);

		dynamicPromotionsPage.addSearchTrigger("bag");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

		dynamicPromotionsPage.addSearchTrigger("\"bag");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Correct error message not showing", dynamicPromotionsPage.getText().contains("Terms have an odd number of quotes, suggesting an unclosed phrase."));

		dynamicPromotionsPage.addSearchTrigger("bag\"");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Correct error message not showing", dynamicPromotionsPage.getText().contains("Terms have an odd number of quotes, suggesting an unclosed phrase."));

		dynamicPromotionsPage.addSearchTrigger("\"bag\"");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Error message should not show", !dynamicPromotionsPage.getText().contains("Terms have an odd number of quotes, suggesting an unclosed phrase."));

		dynamicPromotionsPage.removeSearchTrigger("bag");
		assertThat("Number of triggers does not equal 0", dynamicPromotionsPage.getSearchTriggersList().size() == 0);
	}

	@Test
	public void testCommasTrigger() {
		topNavBar.search("orange");
		searchPage.selectLanguage("Afrikaans", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();

		dynamicPromotionsPage.addSearchTrigger("France");
		assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

		dynamicPromotionsPage.addSearchTrigger(",Germany");
		assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		dynamicPromotionsPage.addSearchTrigger("Ita,ly Spain");
		assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		dynamicPromotionsPage.addSearchTrigger("Ireland, Belgium");
		assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		dynamicPromotionsPage.addSearchTrigger("UK , Luxembourg");
		assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		dynamicPromotionsPage.addSearchTrigger("Andorra");
		assertThat("Legitimate trigger not added", dynamicPromotionsPage.getSearchTriggersList().size() == 2);
		assertThat("Error message displayed with legitimate term", !dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));
	}

	@Test
	public void testHTMLTrigger() {
		topNavBar.search("orange");
		searchPage.selectLanguage("Afrikaans", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();

		final String searchTrigger = "<h1>Hey</h1>";
		dynamicPromotionsPage.addSearchTrigger(searchTrigger);

		final WebElement span = dynamicPromotionsPage.findElement(By.cssSelector(".trigger-words-form .term"));
		assertThat("HTML was not escaped", span.getText().equals(searchTrigger));
	}

	@Test
	public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
		topNavBar.search("simba");
		searchPage.selectLanguage("Swahili", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		topNavBar.sideBarToggle();
		dynamicPromotionsPage.cancelButton("spotlightType").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("dynamic"));

		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();
		topNavBar.sideBarToggle();
		dynamicPromotionsPage.cancelButton("triggers").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("dynamic"));
	}

	@Test
	public void testAddSpotlightSponsored() {
		addDynamicPromotion("car", "English", "Sponsored", "apples");
	}

	@Test
	public void testAddSpotlightHotwire() {
		addDynamicPromotion("Bastille", "French", "Hotwire", "grapes");
	}

	@Test
	public void testAddSpotlightTopPromotions() {
		addDynamicPromotion("Iran", "Urdu", "Top Promotions", "oranges");
	}

	private void addDynamicPromotion(final String searchTerm, final String language, final String promotionType, final String trigger) {
		topNavBar.search(searchTerm);
		searchPage.selectLanguage(language, getConfig().getType().getName());
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));

		final String firstDocTitle = searchPage.getSearchResultTitle(1);
		searchPage.promoteThisQueryButton().click();
		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.loadOrFadeWait();
		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("promotions/create-dynamic/"));
		assertThat("Wrong stage of wizard", dynamicPromotionsPage.spotlightType(promotionType).isDisplayed());
		assertThat("Continue button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.continueButton("spotlightType"), "disabled"));

		dynamicPromotionsPage.spotlightType(promotionType).click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();
		assertThat("Wrong wizard step", dynamicPromotionsPage.triggerAddButton().isDisplayed());
		assertThat("Finish button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
		assertThat("Trigger add button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 0);

		dynamicPromotionsPage.addSearchTrigger(trigger);
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 1);
		assert(dynamicPromotionsPage.getSearchTriggersList().contains(trigger));

		dynamicPromotionsPage.finishButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		assertEquals(searchPage.getSelectedLanguage(), language);
		assertThat("Wrong search performed", searchPage.searchTitle().getText().contains(trigger));
		assertEquals(searchPage.promotionsSummaryList(false).get(0), firstDocTitle);
		assertEquals(searchPage.promotionsLabel().getText(), promotionType);
	}

	@Test
	public void testTwoPromotionTypesForSameTrigger() {
		topNavBar.search("paris");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		int promotionResultsCount = searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.spotlightType("Sponsored").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();

		dynamicPromotionsPage.addSearchTrigger("cat");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 1);
		assert(dynamicPromotionsPage.getSearchTriggersList().contains("cat"));
		dynamicPromotionsPage.finishButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		int list = searchPage.promotionsSummaryList(true).size();
		searchPage.loadOrFadeWait();
		assertEquals(list, promotionResultsCount);

		topNavBar.search("rome");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		promotionResultsCount = promotionResultsCount + searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.spotlightType("Hotwire").click();
		dynamicPromotionsPage.continueButton("spotlightType").click();
		dynamicPromotionsPage.loadOrFadeWait();

		dynamicPromotionsPage.addSearchTrigger("cat");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 1);
		assert(dynamicPromotionsPage.getSearchTriggersList().contains("cat"));
		dynamicPromotionsPage.finishButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		list = searchPage.promotionsSummaryList(true).size();
		searchPage.loadOrFadeWait();
		assertEquals(promotionResultsCount, list);
	}

	@Test
	public void testDuplicateQueryAndTriggerDifferentSpotlightType() {
		topNavBar.search("berlin");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		int promotionResultsCount = searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.createDynamicPromotion("Sponsored", "Ida");

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		assertThat("promotions aren't labelled as Sponsored", searchPage.promotionsLabel().getText().equals("Sponsored"));

		topNavBar.search("berlin");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		promotionResultsCount = promotionResultsCount + searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.createDynamicPromotion("Hotwire", "Ida");

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		final List<String> promotionLabels = searchPage.getPromotionSummaryLabels();
		assertThat("No Hotwire labels in promotions summary", promotionLabels.contains("Hotwire"));
		assertThat("No Sponsored labels in promotions summary", promotionLabels.contains("Sponsored"));
		assertEquals(promotionResultsCount, searchPage.promotionsSummaryList(true).size());
	}

	@Test
	public void testNumberOfDocumentsPromotedOnPromotionsPage() {
		topNavBar.search("wors");
		searchPage.selectLanguage("Afrikaans", getConfig().getType().getName());
		final int promotionResultsCount = searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.createDynamicPromotion("Top Promotions", "sausage");

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		assertEquals(promotionResultsCount, searchPage.promotionsSummaryList(true).size());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("sausage").click();
		promotionsPage.loadOrFadeWait();
		assertEquals(promotionResultsCount, promotionsPage.getDynamicPromotedList(true).size());
	}

	@Test
	public void testDeletedPromotionIsDeleted() {
		topNavBar.search("Ulster");
		searchPage.selectLanguage("French", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.createDynamicPromotion("Top Promotions", "home");

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		assertThat("No promoted items displayed", searchPage.getPromotionSummarySize() != 0);

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("home").click();
		promotionsPage.deletePromotion();

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		assertThat("promotion should be deleted", promotionsPage.promotionsList().size() == 0);

		topNavBar.search("home");
		searchPage.selectLanguage("French", getConfig().getType().getName());
		assertThat("Some items were promoted despite deleting the promotion", searchPage.getPromotionSummarySize() == 0);
	}

	@Test
	public void testPromotionLanguage() {
		topNavBar.search("میں");
		searchPage.selectLanguage("Urdu", getConfig().getType().getName());
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.createDynamicPromotion("Top Promotions", "phrase");

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("phrase").click();
		promotionsPage.loadOrFadeWait();
		assertEquals("Urdu", promotionsPage.getLanguage());
	}
}
