package com.autonomy.abc.promotions;

import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.base.SOTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.OPSearchPage;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.autonomy.abc.framework.TestStateAssert.assertThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;

public class CreateNewDynamicPromotionsOnPremiseITCase extends SOTestBase {

	public CreateNewDynamicPromotionsOnPremiseITCase(final TestConfig config) {
		super(config);
	}

	private SearchPage searchPage;
	private CreateNewPromotionsPage dynamicPromotionsPage;
	private PromotionService promotionService;
    private SearchService searchService;
	private TriggerForm triggerForm;

	@Before
	public void setUp() throws InterruptedException {
        promotionService = getApplication().promotionService();
        searchService = getApplication().searchService();

        searchPage = searchService.search("fox");
    }

	@After
	public void tearDown(){
		SOTearDown.PROMOTIONS.tearDown(this);
	}

	@Test
	public void testAddSpotlightSponsored() {
		addDynamicPromotion("car", Language.ENGLISH, Promotion.SpotlightType.SPONSORED, "apples");
	}

	@Test
	public void testAddSpotlightHotwire() {
		addDynamicPromotion("Bastille", Language.FRENCH, Promotion.SpotlightType.HOTWIRE, "grapes");
	}

	@Test
	public void testAddSpotlightTopPromotions() {
		addDynamicPromotion("Iran", Language.URDU, Promotion.SpotlightType.TOP_PROMOTIONS, "oranges");
	}

	private void addDynamicPromotion(final String searchTerm, final Language language, final Promotion.SpotlightType promotionType, final String trigger) {
		searchPage = searchService.search(new Query(searchTerm).withFilter(new LanguageFilter(language)));

		final String firstDocTitle = searchPage.getSearchResult(1).getTitleString();
		searchPage.promoteThisQueryButton().click();
		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		Waits.loadOrFadeWait();
		assertThat(getWindow(), urlContains("promotions/create-dynamic/"));
		assertThat("Wrong stage of wizard", dynamicPromotionsPage.spotlightType(promotionType), is(displayed()));
		assertThat("Continue button should be disabled", ElementUtil.isAttributePresent(dynamicPromotionsPage.continueButton(), "disabled"));

		dynamicPromotionsPage.spotlightType(promotionType).click();
		dynamicPromotionsPage.continueButton().click();
		Waits.loadOrFadeWait();

        TriggerForm triggerForm = dynamicPromotionsPage.getTriggerForm();

        assertThat("Wrong wizard step", triggerForm.addButton(), is(displayed()));
        assertThat("Finish button should be disabled", ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
        assertThat("Trigger add button should be disabled", ElementUtil.isAttributePresent(triggerForm.addButton(), "disabled"));
        assertThat(triggerForm.getNumberOfTriggers(), is(0));

        triggerForm.addTrigger(trigger);
        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem(trigger));

        dynamicPromotionsPage.finishButton().click();
		Waits.loadOrFadeWait();

		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		} catch (final TimeoutException t) {
			fail("Promotions summary has not appeared");
		}

		assertThat(searchPage.getSelectedLanguage(), is(language));
		assertThat("Wrong search performed", searchPage.getHeadingSearchTerm(), containsString(trigger));
		assertThat(searchPage.getPromotedDocumentTitles(false).get(0), is(firstDocTitle));
		assertThat(((OPSearchPage) searchPage).promotionsLabel().getText(), equalToIgnoringCase(promotionType.getOption()));
	}

	@Test
	public void testTwoPromotionTypesForSameTrigger() {
		DynamicPromotion promotion1 = new DynamicPromotion(Promotion.SpotlightType.SPONSORED, "cat");
		DynamicPromotion promotion2 = new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, "cat");
		Query query1 = new Query("paris");
		Query query2 = new Query("rome");

        searchPage = searchService.search(query1);
        int expected = searchPage.getHeadingResultsCount();

		promotionService.setUpPromotion(promotion1, query1, 0);
		searchPage = getElementFactory().getSearchPage();
		assertThat(searchPage.getPromotedDocumentTitles(true), hasSize(expected));

		searchPage = searchService.search(query2);
		expected += searchPage.getHeadingResultsCount();

		promotionService.setUpPromotion(promotion2, query2, 0);
		searchPage = getElementFactory().getSearchPage();
		assertThat(searchPage.getPromotedDocumentTitles(true), hasSize(expected));
	}

	@Test
	public void testDuplicateQueryAndTriggerDifferentSpotlightType() {
        Query query = new Query("berlin");
        searchPage = searchService.search(query);
        int promotionResultsCount = searchPage.getHeadingResultsCount();
		searchPage.promoteThisQueryButton().click();
		Waits.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.SPONSORED, "Ida"), query, 1);

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		assertThat("promotions aren't labelled as Sponsored", ((OPSearchPage) searchPage).promotionsLabel().getText(), equalToIgnoringCase("Sponsored"));

        searchService.search(query);
        promotionResultsCount = promotionResultsCount + searchPage.getHeadingResultsCount();
		searchPage.promoteThisQueryButton().click();
		Waits.loadOrFadeWait();

		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, "Ida"), query, 1);

		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		final List<String> promotionLabels = ((OPSearchPage) searchPage).getPromotionLabels();
		assertThat("No Hotwire labels in promotions summary", promotionLabels, hasItem(Promotion.SpotlightType.HOTWIRE.getOption().toUpperCase()));
		assertThat("No Sponsored labels in promotions summary", promotionLabels, hasItem(Promotion.SpotlightType.SPONSORED.getOption().toUpperCase()));
		assertThat(searchPage.getPromotedDocumentTitles(true), hasSize(promotionResultsCount));
	}

	@Test
	public void testPromotionLanguage() {
        Query query = new Query("میں").withFilter(new LanguageFilter(Language.URDU));
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "phrase"), query, 1);
        searchPage = getElementFactory().getSearchPage();

		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
		} catch (final TimeoutException t) {
			fail("Promotions summary has not appeared");
        }

		PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails("phrase");
		assertThat("Promotion has been created in the wrong language", promotionsDetailPage.getLanguage(), is(Language.URDU.toString()));
	}

	@Test
	public void testDynamicPromotionCreation() {
		searchPage = searchService.search(new Query("lapin").withFilter(new LanguageFilter(Language.FRENCH)));

		final String firstDocTitle = searchPage.getSearchResult(1).getTitleString();
		searchPage.promoteThisQueryButton().click();
		dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		Waits.loadOrFadeWait();

		assertThat(getWindow(), urlContains("promotions/create-dynamic/"));
		assertThat(dynamicPromotionsPage.spotlightType(Promotion.SpotlightType.HOTWIRE), displayed());
		assertThat(dynamicPromotionsPage.getCurrentStepTitle(), containsString("Spotlight type"));
		assertThat(dynamicPromotionsPage.continueButton(), disabled());

		dynamicPromotionsPage.spotlightType(Promotion.SpotlightType.TOP_PROMOTIONS).click();
		dynamicPromotionsPage.continueButton().click();
		Waits.loadOrFadeWait();
		triggerForm = dynamicPromotionsPage.getTriggerForm();

		assertThat(triggerForm.addButton(), displayed());
		assertThat(dynamicPromotionsPage.getCurrentStepTitle(), containsString("Trigger words"));
		assertThat(dynamicPromotionsPage.finishButton(), disabled());
		assertThat(triggerForm.addButton(), disabled());
		assertThat(triggerForm.getNumberOfTriggers(), is(0));

		checkAddTrigger("rabbit");
		checkAddTrigger("bunny");
		checkAddTrigger("hare");

		// Hare is not a word for bunny
		triggerForm.removeTrigger("hare");
		assertThat(triggerForm.getNumberOfTriggers(), is(2));
		assertThat(triggerForm.getTriggersAsStrings(), hasItems("bunny", "rabbit"));
		assertThat(triggerForm.getTriggersAsStrings(), not(hasItem("hare")));

		dynamicPromotionsPage.finishButton().click();
		Waits.loadOrFadeWait();
		searchPage.waitForPromotionsLoadIndicatorToDisappear();

		assertThat(searchPage.getHeadingSearchTerm(), is("bunny rabbit"));
		assertThat(searchPage.getPromotedDocumentTitles(false).get(0), is(firstDocTitle));
	}

	private void checkAddTrigger(String trigger) {
		List<String> beforeTriggers = triggerForm.getTriggersAsStrings();

		triggerForm.addTrigger(trigger);
		List<String> afterTriggers = triggerForm.getTriggersAsStrings();

		assertThat(afterTriggers, hasSize(beforeTriggers.size() + 1));
		assertThat(afterTriggers, hasItem(trigger));
		assertThat(afterTriggers, containsItems(beforeTriggers));
	}
}
