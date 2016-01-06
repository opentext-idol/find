package com.autonomy.abc.promotions;

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
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;

public class CreateNewDynamicPromotionsITCase extends ABCTestBase {

    private SearchPage searchPage;
    private PromotionsPage promotionsPage;
    private CreateNewPromotionsPage dynamicPromotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private PromotionService promotionService;
    private SearchService searchService;
    private TriggerForm triggerForm;

    public CreateNewDynamicPromotionsITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
        super(config, browser, appType, platform);
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

        try {
            new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
        } catch (final TimeoutException t) {
            fail("Promotions summary has not appeared");
        }

        if(getConfig().getType().equals(ApplicationType.ON_PREM)) {
            assertThat(searchPage.getSelectedLanguage(), is(Language.FRENCH.toString()));
            assertThat(searchPage.promotionsLabel().getText(), equalToIgnoringCase(Promotion.SpotlightType.TOP_PROMOTIONS.getOption()));
        }
        assertThat("Wrong search performed", searchPage.getHeadingSearchTerm(), is("bunny rabbit"));
        assertThat(searchPage.promotionsSummaryList(false).get(0), is(firstDocTitle));
    }

    @Test
    public void testAddRemoveTriggerTermsAndCancel() {
        goToTriggers();
        
        assertThat("Wizard has not progressed to Select the position", dynamicPromotionsPage.getText(), containsString("Select Promotion Triggers"));
        assertThat("Trigger add button is not disabled when text box is empty", ElementUtil.isAttributePresent(triggerForm.addButton(), "disabled"));
        assertThat("Finish button is not disabled when there are no match terms", ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
        assertThat("Cancel button is not enabled when there are no match terms", !ElementUtil.isAttributePresent(dynamicPromotionsPage.cancelButton(), "disabled"));

        triggerForm.addTrigger("animal");
        assertThat("Finish button is not enabled when a trigger is added", !ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
        assertThat("animal search trigger not added", triggerForm.getTriggersAsStrings(), hasItem("animal"));

        triggerForm.removeTrigger("animal");
        assertThat("animal search trigger not removed", triggerForm.getTriggersAsStrings(), not(hasItem("animal")));
        assertThat("Promote button is not disabled when no triggers are added", ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));

        triggerForm.addTrigger("bushy tail");
        assertThat("Number of triggers does not equal 2", triggerForm.getNumberOfTriggers(), is(2));
        assertThat("bushy search trigger not added", triggerForm.getTriggersAsStrings(), hasItem("bushy"));
        assertThat("tail search trigger not added", triggerForm.getTriggersAsStrings(), hasItem("tail"));

        triggerForm.removeTrigger("tail");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("bushy search trigger not present", triggerForm.getTriggersAsStrings(), hasItem("bushy"));
        assertThat("tail search trigger not removed", triggerForm.getTriggersAsStrings(), not(hasItem("tail")));

        dynamicPromotionsPage.cancelButton().click();
        assertThat("Wizard has not cancelled", getDriver().getCurrentUrl(), not(containsString("create")));
    }

    @Test
    public void testWhitespaceTrigger() {
        goToTriggers();

        assertThat("Trigger add button is not disabled", ElementUtil.isAttributePresent(triggerForm.addButton(), "disabled"));

        ElementUtil.tryClickThenTryParentClick(triggerForm.addButton());
        assertThat("Number of triggers does not equal 0", triggerForm.getNumberOfTriggers(), is(0));

        triggerForm.addTrigger("trigger");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));

        triggerForm.addTrigger("   ");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));

        triggerForm.addTrigger(" trigger");
        assertThat("Whitespace at beginning should be ignored", triggerForm.getNumberOfTriggers(), is(1));

        triggerForm.addTrigger("\t");
        assertThat("Whitespace at beginning should be ignored", triggerForm.getNumberOfTriggers(), is(1));
    }

    @Test
    public void testQuotesTrigger() {
        goToTriggers();

        assertThat("Trigger add button is not disabled", ElementUtil.isAttributePresent(triggerForm.addButton(), "disabled"));

        ElementUtil.tryClickThenTryParentClick(triggerForm.addButton());

        assertThat("Number of triggers does not equal 0", triggerForm.getNumberOfTriggers(), is(0));

        triggerForm.addTrigger("bag");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));

        triggerForm.addTrigger("\"bag");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("Correct error message not showing", dynamicPromotionsPage.getText(), containsString("Terms have an odd number of quotes, suggesting an unclosed phrase."));

        triggerForm.addTrigger("bag\"");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("Correct error message not showing", dynamicPromotionsPage.getText(), containsString("Terms have an odd number of quotes, suggesting an unclosed phrase."));

        triggerForm.addTrigger("\"bag\"");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("Error message should not show", dynamicPromotionsPage.getText(), not(containsString("Terms have an odd number of quotes, suggesting an unclosed phrase.")));

        triggerForm.removeTrigger("bag");
        assertThat("Number of triggers does not equal 0", triggerForm.getNumberOfTriggers(), is(0));
    }

    @Test
    public void testCommasTrigger() {
        goToTriggers();

        triggerForm.addTrigger("France");
        assertThat("Number of triggers does not equal 1", triggerForm.getNumberOfTriggers(), is(1));

        triggerForm.addTrigger(",Germany");
        assertThat("Commas should not be included in triggers", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText(), containsString("Terms may not contain commas. Separate words and phrases with whitespace."));

        triggerForm.addTrigger("Ita,ly Spain");
        assertThat("Commas should not be included in triggers", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText(), containsString("Terms may not contain commas. Separate words and phrases with whitespace."));

        triggerForm.addTrigger("Ireland, Belgium");
        assertThat("Commas should not be included in triggers", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText(), containsString("Terms may not contain commas. Separate words and phrases with whitespace."));

        triggerForm.addTrigger("UK , Luxembourg");
        assertThat("Commas should not be included in triggers", triggerForm.getNumberOfTriggers(), is(1));
        assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText(), containsString("Terms may not contain commas. Separate words and phrases with whitespace."));

        triggerForm.addTrigger("Andorra");
        assertThat("Legitimate trigger not added", triggerForm.getNumberOfTriggers(), is(2));
        assertThat("Error message displayed with legitimate term", dynamicPromotionsPage.getText(), not(containsString("Terms may not contain commas. Separate words and phrases with whitespace.")));
    }

    @Test
    public void testHTMLTrigger() {
        goToTriggers();

        final String searchTrigger = "<h1>Hey</h1>";
        triggerForm.addTrigger(searchTrigger);

        final WebElement span = dynamicPromotionsPage.findElement(By.cssSelector(".trigger-words-form .term"));
        assertThat("HTML was not escaped", span.getText(), is(searchTrigger.toLowerCase()));		//Triggers are always lower case
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
    public void testNumberOfDocumentsPromotedOnPromotionsPage() {
        searchPage = searchService.search("arctic");
        final int promotionResultsCount = getNumberOfPromotedDynamicResults();

        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, promotionResultsCount, "sausage"), "arctic", 1);

        try {
            new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promotionsSummary()));
        } catch (final TimeoutException t) {
            fail("Promotions summary has not appeared");
        }
        assertThat(searchPage.promotionsSummaryList(true).size(), is(promotionResultsCount));

        promotionsDetailPage = promotionService.goToDetails("sausage");
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
