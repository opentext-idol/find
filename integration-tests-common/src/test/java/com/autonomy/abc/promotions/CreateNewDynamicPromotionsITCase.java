package com.autonomy.abc.promotions;

import com.autonomy.abc.Trigger.SharedTriggerTests;
import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class CreateNewDynamicPromotionsITCase extends ABCTestBase {

    private SearchPage searchPage;
    private PromotionsPage promotionsPage;
    private CreateNewPromotionsPage dynamicPromotionsPage;
    private PromotionService promotionService;
    private SearchService searchService;
    private TriggerForm triggerForm;

    public CreateNewDynamicPromotionsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() throws InterruptedException {
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchService = getApplication().createSearchService(getElementFactory());

        promotionsPage = promotionService.deleteAll();
        searchPage = searchService.search("fox");
    }

    @After
    public void tearDown(){
        promotionService.deleteAll();
    }

    @Test
    public void testDynamicPromotionCreation() {
        searchPage = searchService.search(new SearchQuery("lapin").withFilter(new LanguageFilter(Language.FRENCH)));

        final String firstDocTitle = searchPage.getSearchResultTitle(1);
        searchPage.promoteThisQueryButton().click();
        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        Waits.loadOrFadeWait();
        assertThat("Wrong URL", getDriver().getCurrentUrl().contains("promotions/create-dynamic/"));

        if(getConfig().getType().equals(ApplicationType.ON_PREM)) {
            assertThat("Wrong stage of wizard", dynamicPromotionsPage.spotlightType(Promotion.SpotlightType.HOTWIRE), is(displayed()));
            assertThat("Wrong wizard step displayed, wrong title", dynamicPromotionsPage.getCurrentStepTitle(), containsString("Spotlight type"));
            assertThat("Continue button should be disabled", ElementUtil.isAttributePresent(dynamicPromotionsPage.continueButton(), "disabled"));

            dynamicPromotionsPage.spotlightType(Promotion.SpotlightType.TOP_PROMOTIONS).click();
        } else {
            assertThat("Wrong wizard step displayed, dial not present", ((HSOCreateNewPromotionsPage) dynamicPromotionsPage).dial(), is(displayed()));
            assertThat("Wrong wizard step displayed, wrong title", dynamicPromotionsPage.getCurrentStepTitle(), containsString("Results number"));
        }

        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();

        triggerForm = dynamicPromotionsPage.getTriggerForm();
        
        assertThat("Wrong wizard step", triggerForm.addButton(), is(displayed()));
        assertThat("Wrong wizard step displayed, wrong title", dynamicPromotionsPage.getCurrentStepTitle(), containsString("Trigger words"));
        assertThat("Finish button should be disabled", ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
        assertThat("Trigger add button should be disabled", ElementUtil.isAttributePresent(triggerForm.addButton(), "disabled"));
        assertThat(triggerForm.getNumberOfTriggers(), is(0));

        triggerForm.addTrigger("rabbit");
        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("rabbit"));
        
        triggerForm.addTrigger("bunny");
        assertThat(triggerForm.getNumberOfTriggers(), is(2));
        assertThat(triggerForm.getTriggersAsStrings(), hasItems("bunny", "rabbit"));

        triggerForm.addTrigger("hare");
        assertThat(triggerForm.getNumberOfTriggers(), is(3));
        assertThat(triggerForm.getTriggersAsStrings(), hasItems("bunny", "rabbit", "hare"));

        // Hare is not a word for bunny
        triggerForm.removeTrigger("hare");
        assertThat(triggerForm.getNumberOfTriggers(), is(2));
        assertThat(triggerForm.getTriggersAsStrings(), hasItems("bunny", "rabbit"));
        assertThat(triggerForm.getTriggersAsStrings(), not(hasItem("hare")));

        dynamicPromotionsPage.finishButton().click();
        Waits.loadOrFadeWait();

        searchPage.waitForPromotionsLoadIndicatorToDisappear();

        assertThat("Wrong search performed", searchPage.getHeadingSearchTerm(), is("bunny rabbit"));
        assertThat(searchPage.getPromotedDocumentTitles(false).get(0), is(firstDocTitle));
    }

    @Test
    public void testAddRemoveTriggerTermsAndCancel() {
        goToTriggers();
        
        assertThat("Wizard has not progressed to Select the position", dynamicPromotionsPage.getText(), containsString("Select Promotion Triggers"));

        SharedTriggerTests.addRemoveTriggers(triggerForm, dynamicPromotionsPage.cancelButton(), dynamicPromotionsPage.finishButton());

        dynamicPromotionsPage.cancelButton().click();
        assertThat("Wizard has not cancelled", getDriver().getCurrentUrl(), not(containsString("create")));
    }

    @Test
    public void testTriggers(){
        goToTriggers();
        SharedTriggerTests.badTriggersTest(triggerForm);
    }

    private void goToTriggers() {
        searchPage = searchService.search(new SearchQuery("orange").withFilter(new LanguageFilter(Language.AFRIKAANS)));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();

        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();

        clickTopPromotions();

        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();

        triggerForm = dynamicPromotionsPage.getTriggerForm();
    }

    private int getNumberOfPromotedDynamicResults() {
        int promotionResultsCount = 30;
        if (getConfig().getType().equals(ApplicationType.ON_PREM) || searchPage.getHeadingResultsCount() <= 30) {
            promotionResultsCount = searchPage.getHeadingResultsCount();
        }
        return promotionResultsCount;
    }

    @Test
    // CCUK-3586
    public void testNumberOfDocumentsPromotedOnPromotionsPage() {
        searchPage = searchService.search("arctic");
        final int promotionResultsCount = getNumberOfPromotedDynamicResults();

        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, promotionResultsCount, "sausage"), "arctic", 1);

        assertThat(searchPage.getPromotedDocumentTitles(true), hasSize(promotionResultsCount));

        PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails("sausage");
        assertThat("query results are displayed on details page", promotionsDetailPage.dynamicPromotedList(), not(hasItem(containsText("Search for something"))));
        assertThat(promotionsDetailPage.getDynamicPromotedTitles(), hasSize(promotionResultsCount));
    }

    @Test
    public void testDeletedPromotionIsDeleted() {
        String trigger = "home";
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, 10, trigger), "Ulster", 10);
        Waits.loadOrFadeWait();
        assertThat("No promoted items displayed", searchPage.getPromotionSummarySize(), not(0));

        promotionService.delete(trigger);
        assertThat("promotion should be deleted", promotionsPage.promotionsList(), hasSize(0));

        searchService.search(trigger).waitForPromotionsLoadIndicatorToDisappear();
        assertThat("Some items were promoted despite deleting the promotion", searchPage.getPromotionSummarySize(), is(0));
    }

    @Test
    public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
        searchPage = searchService.search(new SearchQuery("simba").withFilter(new LanguageFilter(Language.SWAHILI)));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();

        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        body.getSideNavBar().toggle();
        dynamicPromotionsPage.cancelButton().click();
        Waits.loadOrFadeWait();
        assertThat("Wizard has not cancelled", getDriver().getCurrentUrl(), not(containsString("dynamic")));

        Waits.loadOrFadeWait();
        assertThat("\"undefined\" returned as query text when wizard cancelled", searchPage.getHeadingSearchTerm(), not(containsString("undefined")));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();
        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        clickTopPromotions();
        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();

        body.getSideNavBar().toggle();
        dynamicPromotionsPage.cancelButton().click();
        Waits.loadOrFadeWait();

        assertThat("Wizard has not cancelled", getDriver().getCurrentUrl(), not(containsString("dynamic")));
        Waits.loadOrFadeWait();
        assertThat("\"undefined\" returned as query text when wizard cancelled", searchPage.getHeadingSearchTerm(), not(containsString("undefined")));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();

        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        clickTopPromotions();
        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();
        body.getSideNavBar().toggle();
        dynamicPromotionsPage.cancelButton().click();
        Waits.loadOrFadeWait();
        assertThat("Wizard has not cancelled", getDriver().getCurrentUrl(), not(containsString("dynamic")));
    }

    private void clickTopPromotions() {
        if(getConfig().getType().equals(ApplicationType.ON_PREM)) {
            dynamicPromotionsPage.spotlightType(Promotion.SpotlightType.TOP_PROMOTIONS).click();
        }
    }
}
