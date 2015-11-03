package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.promotions.*;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CreateNewDynamicPromotionsOnPremiseITCase extends ABCTestBase {

	public CreateNewDynamicPromotionsOnPremiseITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private SearchPage searchPage;
	private PromotionsPage promotionsPage;
	private CreateNewPromotionsPage dynamicPromotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private PromotionService promotionService;
    private SearchActionFactory searchActionFactory;

	@Before
	public void setUp() throws InterruptedException {
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());

		promotionsPage = promotionService.deleteAll();
        searchPage = searchActionFactory.makeSearch("fox").apply();
	}

    private void goToTriggers() {
        searchPage = searchActionFactory.makeSearch("orange").applyFilter(new LanguageFilter("Afrikaans")).apply();
        searchPage.promoteThisQueryButton().click();
        searchPage.loadOrFadeWait();

        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        dynamicPromotionsPage.spotlightType("Top Promotions").click();
        dynamicPromotionsPage.continueButton().click();
        dynamicPromotionsPage.loadOrFadeWait();
        if (getConfig().getType().equals(ApplicationType.HOSTED)) {
            dynamicPromotionsPage.continueButton().click();
            dynamicPromotionsPage.loadOrFadeWait();
        }
    }

	@Test
	public void testDynamicPromotionCreation() {
		searchPage = searchActionFactory.makeSearch("lapin").applyFilter(new LanguageFilter("French")).apply();

		final String firstDocTitle = searchPage.getSearchResultTitle(1);
		searchPage.promoteThisQueryButton().click();
		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		dynamicPromotionsPage.loadOrFadeWait();
		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("promotions/create-dynamic/"));
		assertThat("Wrong stage of wizard", dynamicPromotionsPage.spotlightType("Hotwire").isDisplayed());
		assertThat("Wrong wizard step displayed, wrong title", dynamicPromotionsPage.getCurrentStepTitle().contains("Spotlight type"));
		assertThat("Continue button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.continueButton(), "disabled"));

		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		if (getConfig().getType().equals(ApplicationType.HOSTED)) {
			assertThat("Wrong wizard step displayed, dial not present", ((HSOCreateNewPromotionsPage) dynamicPromotionsPage).dial().isDisplayed());
			assertThat("Wrong wizard step displayed, wrong title", dynamicPromotionsPage.getCurrentStepTitle().contains("Results number"));
			dynamicPromotionsPage.continueButton().click();
			dynamicPromotionsPage.loadOrFadeWait();
		}

		assertThat("Wrong wizard step", dynamicPromotionsPage.triggerAddButton().isDisplayed());
		assertThat("Wrong wizard step displayed, wrong title", dynamicPromotionsPage.getCurrentStepTitle().contains("Trigger words"));
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

		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		} catch (final TimeoutException t) {
			fail("Promotions summary has not appeared");
		}

		assertEquals(searchPage.getSelectedLanguage(), "French");
		assertThat("Wrong search performed", searchPage.searchTitle().getText().equals("bunny rabbit"));
		assertEquals(searchPage.promotionsSummaryList(false).get(0), firstDocTitle);
		assertThat(searchPage.promotionsLabel().getText(), equalToIgnoringCase("Top Promotions"));
	}

	@Test
	public void testAddRemoveTriggerTermsAndCancel() {
        goToTriggers();

		assertThat("Wizard has not progressed to Select the position", dynamicPromotionsPage.getText().contains("Select Promotion Triggers"));
		assertThat("Trigger add button is not disabled when text box is empty", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));
		assertThat("Finish button is not disabled when there are no match terms", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
		assertThat("Cancel button is not enabled when there are no match terms", !dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.cancelButton(), "disabled"));

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

		dynamicPromotionsPage.cancelButton().click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
	}

	@Test
	public void testWhitespaceTrigger() {
        goToTriggers();

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
        goToTriggers();

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
        goToTriggers();

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
        goToTriggers();

		final String searchTrigger = "<h1>Hey</h1>";
		dynamicPromotionsPage.addSearchTrigger(searchTrigger);

		final WebElement span = dynamicPromotionsPage.findElement(By.cssSelector(".trigger-words-form .term"));
		assertThat("HTML was not escaped", span.getText(), is(searchTrigger.toLowerCase()));		//Triggers are always lower case
	}

	@Test
	public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
        searchPage = searchActionFactory.makeSearch("simba").applyFilter(new LanguageFilter("Swahili")).apply();
        searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

        TopNavBar navBar = body.getTopNavBar();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		navBar.sideBarToggle();
		dynamicPromotionsPage.cancelButton().click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("dynamic"));

		searchPage.loadOrFadeWait();
		assertFalse("\"undefined\" returned as query text when wizard cancelled", searchPage.searchTitle().getText().contains("undefined"));
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();
		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton().click();
		dynamicPromotionsPage.loadOrFadeWait();
		if (getConfig().getType().equals(ApplicationType.HOSTED)) {
			navBar.sideBarToggle();
			dynamicPromotionsPage.cancelButton().click();
		} else {
			body.getTopNavBar().sideBarToggle();
			dynamicPromotionsPage.cancelButton().click();
		}

		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("dynamic"));
		searchPage.loadOrFadeWait();
		assertFalse("\"undefined\" returned as query text when wizard cancelled", searchPage.searchTitle().getText().contains("undefined"));
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();
		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		dynamicPromotionsPage.spotlightType("Top Promotions").click();
		dynamicPromotionsPage.continueButton().click();
		dynamicPromotionsPage.loadOrFadeWait();
		if (getConfig().getType().equals(ApplicationType.HOSTED)) {
			dynamicPromotionsPage.continueButton().click();
			dynamicPromotionsPage.loadOrFadeWait();
		}
		navBar.sideBarToggle();
		dynamicPromotionsPage.cancelButton().click();
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
		searchPage = searchActionFactory.makeSearch(searchTerm).applyFilter(new LanguageFilter(language)).apply();

		final String firstDocTitle = searchPage.getSearchResultTitle(1);
		searchPage.promoteThisQueryButton().click();
		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		dynamicPromotionsPage.loadOrFadeWait();
		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("promotions/create-dynamic/"));
		assertThat("Wrong stage of wizard", dynamicPromotionsPage.spotlightType(promotionType).isDisplayed());
		assertThat("Continue button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.continueButton(), "disabled"));

		dynamicPromotionsPage.spotlightType(promotionType).click();
		dynamicPromotionsPage.continueButton().click();
		dynamicPromotionsPage.loadOrFadeWait();
		if (promotionType.equals("Hosted")) {
			dynamicPromotionsPage.continueButton().click();
			dynamicPromotionsPage.loadOrFadeWait();
		}
		assertThat("Wrong wizard step", dynamicPromotionsPage.triggerAddButton().isDisplayed());
		assertThat("Finish button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
		assertThat("Trigger add button should be disabled", dynamicPromotionsPage.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 0);

		dynamicPromotionsPage.addSearchTrigger(trigger);
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 1);
		assert(dynamicPromotionsPage.getSearchTriggersList().contains(trigger));

		dynamicPromotionsPage.finishButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		} catch (final TimeoutException t) {
			fail("Promotions summary has not appeared");
		}

		assertEquals(searchPage.getSelectedLanguage(), language);
		assertThat("Wrong search performed", searchPage.searchTitle().getText().contains(trigger));
		assertEquals(searchPage.promotionsSummaryList(false).get(0), firstDocTitle);
		assertThat(searchPage.promotionsLabel().getText(), equalToIgnoringCase(promotionType));
	}

	@Test
	public void testTwoPromotionTypesForSameTrigger() {
        searchPage = searchActionFactory.makeSearch("paris").apply();
		int promotionResultsCount = searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		dynamicPromotionsPage.spotlightType("Sponsored").click();
		dynamicPromotionsPage.continueButton().click();
		dynamicPromotionsPage.loadOrFadeWait();
		if (getConfig().getType().equals(ApplicationType.HOSTED)) {
			dynamicPromotionsPage.continueButton().click();
			dynamicPromotionsPage.loadOrFadeWait();
		}

		dynamicPromotionsPage.addSearchTrigger("cat");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 1);
		assert(dynamicPromotionsPage.getSearchTriggersList().contains("cat"));
		dynamicPromotionsPage.finishButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		int list = searchPage.promotionsSummaryList(true).size();
		searchPage.loadOrFadeWait();
		assertEquals("Wrong number of promoted documents displayed", promotionResultsCount, list);

        searchPage = searchActionFactory.makeSearch("rome").apply();
		promotionResultsCount = promotionResultsCount + searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		dynamicPromotionsPage.spotlightType("Hotwire").click();
		dynamicPromotionsPage.continueButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		dynamicPromotionsPage.addSearchTrigger("cat");
		assertEquals(dynamicPromotionsPage.getSearchTriggersList().size(), 1);
		assert(dynamicPromotionsPage.getSearchTriggersList().contains("cat"));
		dynamicPromotionsPage.finishButton().click();
		dynamicPromotionsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		list = searchPage.promotionsSummaryList(true).size();
		searchPage.loadOrFadeWait();
		assertEquals("Wrong number of promoted documents displayed", promotionResultsCount, list);
	}

	@Test
	public void testDuplicateQueryAndTriggerDifferentSpotlightType() {
        Search search = searchActionFactory.makeSearch("berlin");
        searchPage = search.apply();
		int promotionResultsCount = searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.SPONSORED, "Ida"), search, 1);

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		assertThat("promotions aren't labelled as Sponsored", searchPage.promotionsLabel().getText(), equalToIgnoringCase("Sponsored"));

		search.apply();
		promotionResultsCount = promotionResultsCount + searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, "Ida"), search, 1);

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		final List<String> promotionLabels = searchPage.getPromotionLabels();
		assertThat("No Hotwire labels in promotions summary", promotionLabels, hasItem("Hotwire"));
		assertThat("No Sponsored labels in promotions summary", promotionLabels, hasItem("Sponsored"));
		assertEquals(promotionResultsCount, searchPage.promotionsSummaryList(true).size());
	}

	@Test
	public void testNumberOfDocumentsPromotedOnPromotionsPage() {
        Search search = searchActionFactory.makeSearch("wors").applyFilter(new LanguageFilter("Afrikaans"));
        searchPage = search.apply();
		final int promotionResultsCount = searchPage.countSearchResults();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "sausage"), search, 1);

		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		} catch (final TimeoutException t) {
			fail("Promotions summary has not appeared");
		}
		assertEquals(promotionResultsCount, searchPage.promotionsSummaryList(true).size());

        promotionsDetailPage = promotionService.goToDetails("sausage");
		assertThat(promotionsDetailPage.getDynamicPromotedTitles(), hasSize(promotionResultsCount));
	}

	@Test
	public void testDeletedPromotionIsDeleted() {
        Search search = searchActionFactory.makeSearch("Ulster").applyFilter(new LanguageFilter("French"));

        searchPage = search.apply();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "home"), search, 1);

		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		} catch (final TimeoutException t) {
			fail("Promotions summary has not appeared");
		}

		assertThat("No promoted items displayed", searchPage.getPromotionSummarySize() != 0);

		promotionsPage = promotionService.goToPromotions();
		promotionService.delete("home");
		assertThat("promotion should be deleted", promotionsPage.promotionsList().size() == 0);

        searchPage = searchActionFactory.makeSearch("home").applyFilter(new LanguageFilter("French")).apply();
		assertThat("Some items were promoted despite deleting the promotion", searchPage.getPromotionSummarySize() == 0);
	}

	@Test
	public void testPromotionLanguage() {
        Search search = searchActionFactory.makeSearch("میں").applyFilter(new LanguageFilter("Urdu"));
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "phrase"), search, 1);
        searchPage = getElementFactory().getSearchPage();

		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		} catch (final TimeoutException t) {
			fail("Promotions summary has not appeared");
        }

        promotionsDetailPage = promotionService.goToDetails("phrase");
		assertEquals("Promotion has been created in the wrong language", "Urdu", promotionsDetailPage.getLanguage());
	}
}
