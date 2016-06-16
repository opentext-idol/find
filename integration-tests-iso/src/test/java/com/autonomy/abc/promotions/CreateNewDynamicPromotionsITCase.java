package com.autonomy.abc.promotions;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.fixtures.PromotionTearDownStrategy;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.shared.SharedTriggerTests;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;

public class CreateNewDynamicPromotionsITCase extends HybridIsoTestBase {

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
    public void setUp() {
        promotionService = getApplication().promotionService();
        searchService = getApplication().searchService();

        promotionsPage = promotionService.deleteAll();
        searchPage = searchService.search("fox");
    }

    @After
    public void tearDown(){
        new PromotionTearDownStrategy().tearDown(this);
    }

    @Test
    public void testAddRemoveTriggerTermsAndCancel() {
        goToTriggers();
        
        assertThat("Wizard has not progressed to Select the position", dynamicPromotionsPage.getText(), containsString("Select Promotion Triggers"));

        SharedTriggerTests.addRemoveTriggers(triggerForm, dynamicPromotionsPage.cancelButton(), dynamicPromotionsPage.finishButton());

        dynamicPromotionsPage.cancelButton().click();
        assertThat(getWindow(), url(not(containsString("create"))));
    }

    @Test
    public void testTriggers(){
        goToTriggers();
        SharedTriggerTests.badTriggersTest(triggerForm);
    }

    private void goToTriggers() {
        searchPage = searchService.search(new Query("orange").withFilter(new LanguageFilter(Language.AFRIKAANS)));
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
        if (isOnPrem() || searchPage.getHeadingResultsCount() <= 30) {
            promotionResultsCount = searchPage.getHeadingResultsCount();
        }
        return promotionResultsCount;
    }

    @Test
    @ResolvedBug("CCUK-3586")
    public void testNumberOfDocumentsPromotedOnPromotionsPage() {
        searchPage = searchService.search("arctic");
        final int promotionResultsCount = getNumberOfPromotedDynamicResults();

        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, promotionResultsCount, "sausage"), "arctic", 1);

        assertThat(searchPage.getPromotedDocumentTitles(true), hasSize(promotionResultsCount));

        final PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails("sausage");
        assertThat("query results are displayed on details page", promotionsDetailPage.dynamicPromotedList(), not(hasItem(containsText("Search for something"))));
        assertThat(promotionsDetailPage.getDynamicPromotedTitles(), hasSize(promotionResultsCount));
    }

    @Test
    public void testDeletedPromotionIsDeleted() {
        final String trigger = "home";
        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, 10, trigger), "France", 10);
        Waits.loadOrFadeWait();
        assertThat("No promoted items displayed", searchPage.getPromotionSummarySize(), not(0));

        promotionService.delete(trigger);
        assertThat("promotion should be deleted", promotionsPage.promotionsList(), hasSize(0));

        searchService.search(trigger).waitForPromotionsLoadIndicatorToDisappear();
        assertThat("Some items were promoted despite deleting the promotion", searchPage.getPromotionSummarySize(), is(0));
    }

    @Test
    public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
        searchPage = searchService.search(new Query("simba").withFilter(new LanguageFilter(Language.SWAHILI)));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();

        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        getElementFactory().getSideNavBar().toggle();
        dynamicPromotionsPage.cancelButton().click();
        Waits.loadOrFadeWait();
        assertThat(getWindow(), url(not(containsString("dynamic"))));

        Waits.loadOrFadeWait();
        assertThat("\"undefined\" returned as query text when wizard cancelled", searchPage.getHeadingSearchTerm(), not(containsString("undefined")));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();
        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        clickTopPromotions();
        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();

        getElementFactory().getSideNavBar().toggle();
        dynamicPromotionsPage.cancelButton().click();
        Waits.loadOrFadeWait();

        assertThat(getWindow(), url(not(containsString("dynamic"))));
        Waits.loadOrFadeWait();
        assertThat("\"undefined\" returned as query text when wizard cancelled", searchPage.getHeadingSearchTerm(), not(containsString("undefined")));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();

        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        clickTopPromotions();
        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();
        getElementFactory().getSideNavBar().toggle();
        dynamicPromotionsPage.cancelButton().click();
        Waits.loadOrFadeWait();
        assertThat(getWindow(), url(not(containsString("dynamic"))));
    }

    private void clickTopPromotions() {
        if(isOnPrem()) {
            dynamicPromotionsPage.spotlightType(Promotion.SpotlightType.TOP_PROMOTIONS).click();
        }
    }
}
