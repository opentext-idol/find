package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.hamcrest.MatcherAssert;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CreateNewDynamicPromotionsITCase extends ABCTestBase {

    private SearchPage searchPage;
    private PromotionsPage promotionsPage;
    private CreateNewPromotionsPage dynamicPromotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private PromotionService promotionService;
    private SearchService searchService;

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
    public void testAddRemoveTriggerTermsAndCancel() {
        goToTriggers();

        assertThat("Wizard has not progressed to Select the position", dynamicPromotionsPage.getText().contains("Select Promotion Triggers"));
        assertThat("Trigger add button is not disabled when text box is empty", ElementUtil.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));
        assertThat("Finish button is not disabled when there are no match terms", ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
        assertThat("Cancel button is not enabled when there are no match terms", !ElementUtil.isAttributePresent(dynamicPromotionsPage.cancelButton(), "disabled"));

        dynamicPromotionsPage.addSearchTrigger("animal");
        assertThat("Finish button is not enabled when a trigger is added", !ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));
        assertThat("animal search trigger not added", dynamicPromotionsPage.getSearchTriggersList().contains("animal"));

        dynamicPromotionsPage.removeSearchTrigger("animal");
        assertThat("animal search trigger not removed", !dynamicPromotionsPage.getSearchTriggersList().contains("animal"));
        assertThat("Promote button is not disabled when no triggers are added", ElementUtil.isAttributePresent(dynamicPromotionsPage.finishButton(), "disabled"));

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

        MatcherAssert.assertThat("Trigger add button is not disabled", ElementUtil.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));

        ElementUtil.tryClickThenTryParentClick(dynamicPromotionsPage.triggerAddButton(), getDriver());
        MatcherAssert.assertThat("Number of triggers does not equal 0", dynamicPromotionsPage.getSearchTriggersList().size() == 0);

        dynamicPromotionsPage.addSearchTrigger("trigger");
        MatcherAssert.assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

        dynamicPromotionsPage.addSearchTrigger("   ");
        MatcherAssert.assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

        dynamicPromotionsPage.addSearchTrigger(" trigger");
        MatcherAssert.assertThat("Whitespace at beginning should be ignored", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

        dynamicPromotionsPage.addSearchTrigger("\t");
        MatcherAssert.assertThat("Whitespace at beginning should be ignored", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
    }

    @Test
    public void testQuotesTrigger() {
        goToTriggers();

        MatcherAssert.assertThat("Trigger add button is not disabled", ElementUtil.isAttributePresent(dynamicPromotionsPage.triggerAddButton(), "disabled"));

        ElementUtil.tryClickThenTryParentClick(dynamicPromotionsPage.triggerAddButton(), getDriver());

        MatcherAssert.assertThat("Number of triggers does not equal 0", dynamicPromotionsPage.getSearchTriggersList().size() == 0);

        dynamicPromotionsPage.addSearchTrigger("bag");
        MatcherAssert.assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

        dynamicPromotionsPage.addSearchTrigger("\"bag");
        MatcherAssert.assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
        MatcherAssert.assertThat("Correct error message not showing", dynamicPromotionsPage.getText().contains("Terms have an odd number of quotes, suggesting an unclosed phrase."));

        dynamicPromotionsPage.addSearchTrigger("bag\"");
        MatcherAssert.assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
        MatcherAssert.assertThat("Correct error message not showing", dynamicPromotionsPage.getText().contains("Terms have an odd number of quotes, suggesting an unclosed phrase."));

        dynamicPromotionsPage.addSearchTrigger("\"bag\"");
        MatcherAssert.assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
        MatcherAssert.assertThat("Error message should not show", !dynamicPromotionsPage.getText().contains("Terms have an odd number of quotes, suggesting an unclosed phrase."));

        dynamicPromotionsPage.removeSearchTrigger("bag");
        MatcherAssert.assertThat("Number of triggers does not equal 0", dynamicPromotionsPage.getSearchTriggersList().size() == 0);
    }

    @Test
    public void testCommasTrigger() {
        goToTriggers();

        dynamicPromotionsPage.addSearchTrigger("France");
        MatcherAssert.assertThat("Number of triggers does not equal 1", dynamicPromotionsPage.getSearchTriggersList().size() == 1);

        dynamicPromotionsPage.addSearchTrigger(",Germany");
        MatcherAssert.assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
        MatcherAssert.assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        dynamicPromotionsPage.addSearchTrigger("Ita,ly Spain");
        MatcherAssert.assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
        MatcherAssert.assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        dynamicPromotionsPage.addSearchTrigger("Ireland, Belgium");
        MatcherAssert.assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
        MatcherAssert.assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        dynamicPromotionsPage.addSearchTrigger("UK , Luxembourg");
        MatcherAssert.assertThat("Commas should not be included in triggers", dynamicPromotionsPage.getSearchTriggersList().size() == 1);
        MatcherAssert.assertThat("Incorrect/No error message displayed", dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        dynamicPromotionsPage.addSearchTrigger("Andorra");
        MatcherAssert.assertThat("Legitimate trigger not added", dynamicPromotionsPage.getSearchTriggersList().size() == 2);
        MatcherAssert.assertThat("Error message displayed with legitimate term", !dynamicPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));
    }

    @Test
    public void testHTMLTrigger() {
        goToTriggers();

        final String searchTrigger = "<h1>Hey</h1>";
        dynamicPromotionsPage.addSearchTrigger(searchTrigger);

        final WebElement span = dynamicPromotionsPage.findElement(By.cssSelector(".trigger-words-form .term"));
        MatcherAssert.assertThat("HTML was not escaped", span.getText(), is(searchTrigger.toLowerCase()));		//Triggers are always lower case
    }

    private void goToTriggers() {
        searchPage = searchService.search(new SearchQuery("orange").withFilter(new LanguageFilter(Language.AFRIKAANS)));
        searchPage.promoteThisQueryButton().click();
        Waits.loadOrFadeWait();

        dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();

        if(getConfig().getType().equals(ApplicationType.ON_PREM)) {
            dynamicPromotionsPage.spotlightType("Top Promotions").click();
        }

        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();
    }

    @Test
    //TODO maybe take this out, OP only?
    public void testTwoPromotionTypesForSameTrigger() {
        String trigger = "cat";

        searchPage = searchService.search("paris");
        int promotionResultsCount = getNumberOfPromotedDynamicResults();

        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.SPONSORED, promotionResultsCount, trigger), "paris", promotionResultsCount);
        assertThat("Wrong number of promoted documents displayed", searchPage.promotionsSummaryList(true).size(), is(promotionResultsCount));

        searchPage = searchService.search("rome");
        int romeResults = getNumberOfPromotedDynamicResults();
        promotionResultsCount += romeResults;

        promotionService.setUpPromotion(new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, romeResults, trigger), "rome", promotionResultsCount);
        assertThat("Wrong number of promoted documents displayed", searchPage.promotionsSummaryList(true).size(), is(promotionResultsCount));
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
}
