package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.query.FieldTextFilter;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchPageOnPremiseITCase extends ABCTestBase{

    private SearchService searchService;
    private SearchPage searchPage;

    public SearchPageOnPremiseITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        searchService = getApplication().searchService();
        searchPage = searchService.search("text");

    }

    @Test
    public void testFieldTextFilter() {
        final String searchResultTitle = searchPage.getSearchResult(1).getTitleString();
        final String firstWord = getFirstWord(searchResultTitle);

        final int comparisonResult = searchResultNotStarting(firstWord);
        final String comparisonString = searchPage.getSearchResult(comparisonResult).getTitleString();

        searchPage.expand(SearchBase.Facet.FIELD_TEXT);
        searchPage.fieldTextAddButton().click();
        Waits.loadOrFadeWait();
        assertThat("input visible", searchPage.fieldTextInput().getElement(), displayed());
        assertThat("confirm button visible", searchPage.fieldTextTickConfirm(), displayed());

        searchPage.filterBy(new FieldTextFilter("WILD{" + firstWord + "*}:DRETITLE"));
        assertThat(searchPage, not(containsText(Errors.Search.HOD)));

        assertThat("edit button visible", searchPage.fieldTextEditButton(), displayed());
        assertThat("remove button visible", searchPage.fieldTextRemoveButton(), displayed());
        assertThat(searchPage.getSearchResult(1).getTitleString(), is(searchResultTitle));

        try {
            assertThat(searchPage.getSearchResult(comparisonResult).getTitleString(), not(comparisonString));
        } catch (final NoSuchElementException e) {
            // The comparison document is not present
        }

        searchPage.fieldTextRemoveButton().click();
        Waits.loadOrFadeWait();
        assertThat(searchPage.getSearchResult(comparisonResult).getTitleString(), is(comparisonString));
        assertThat("Field text add button not visible", searchPage.fieldTextAddButton().isDisplayed());
        assertThat(searchPage.getSearchResult(1).getTitleString(), is(searchResultTitle));
    }

    private String getFirstWord(String string) {
        return string.substring(0, string.indexOf(' '));
    }

    private int searchResultNotStarting(String prefix) {
        for (int result = 1; result <= SearchPage.RESULTS_PER_PAGE; result++) {
            String comparisonString = searchPage.getSearchResult(result).getTitleString();
            if (!comparisonString.startsWith(prefix)) {
                return result;
            }
        }
        throw new IllegalStateException("Cannot test field text filter with this search");
    }


    @Test
    public void testEditFieldText() {
        searchService.search(new Query("boer").withFilter(IndexFilter.ALL));

        searchPage.selectLanguage(Language.AFRIKAANS);

        searchPage.clearFieldText();

        final String firstSearchResult = searchPage.getSearchResult(1).getTitleString();
        final String secondSearchResult = searchPage.getSearchResult(2).getTitleString();

        searchPage.filterBy(new FieldTextFilter("MATCH{" + firstSearchResult + "}:DRETITLE"));
        assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(Errors.Search.HOD)));
        assertThat(searchPage.getText(), not(containsString("No results found")));
        assertThat(searchPage.getSearchResult(1).getTitleString(), is(firstSearchResult));

        searchPage.filterBy(new FieldTextFilter("MATCH{" + secondSearchResult + "}:DRETITLE"));
        assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(Errors.Search.HOD)));
        assertThat(searchPage.getSearchResult(1).getTitleString(), is(secondSearchResult));
    }

    //TODO
    @Test
    public void testFieldTextRestrictionOnPromotions(){
        PromotionService promotionService = getApplication().promotionService();
        promotionService.deleteAll();

        promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "boat"), "darth", 2);
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
        Waits.loadOrFadeWait();

        assertThat(searchPage.getPromotionSummarySize(), is(2));

        final List<String> initialPromotionsSummary = searchPage.getPromotedDocumentTitles(false);
        searchPage.filterBy(new FieldTextFilter("MATCH{" + initialPromotionsSummary.get(0) + "}:DRETITLE"));

        assertThat(searchPage.getPromotionSummarySize(), is(1));
        assertThat(searchPage.getPromotedDocumentTitles(false).get(0), is(initialPromotionsSummary.get(0)));

        searchPage.filterBy(new FieldTextFilter("MATCH{" + initialPromotionsSummary.get(1) + "}:DRETITLE"));

        assertThat(searchPage.getPromotionSummarySize(), is(1));
        assertThat(searchPage.getPromotedDocumentTitles(false).get(0), is(initialPromotionsSummary.get(1)));
    }

    @Test
    public void testFieldTextRestrictionOnPinToPositionPromotions(){
        PromotionService<?> promotionService = getApplication().promotionService();
        promotionService.deleteAll();
        List<String> promotedDocs = promotionService.setUpPromotion(new SpotlightPromotion("duck"), new Query("horse").withFilter(new LanguageFilter(Language.ENGLISH)), 2);

        searchPage.waitForPromotionsLoadIndicatorToDisappear();

        assertThat(promotedDocs.get(0) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(0)));
        assertThat(promotedDocs.get(1) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(1)));

        searchPage.filterBy(new FieldTextFilter("WILD{*horse*}:DRETITLE"));

        searchPage.waitForSearchLoadIndicatorToDisappear();
        Waits.loadOrFadeWait();

        assertThat(promotedDocs.get(0) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(0)));
        assertThat(promotedDocs.get(1) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(1)));	//TODO Seems like this shouldn't be visible
        assertThat("Wrong number of results displayed", searchPage.getHeadingResultsCount(), is(2));
        assertThat("Wrong number of pin to position labels displayed", searchPage.countPinToPositionLabels(), is(2));

        searchPage.filterBy(new FieldTextFilter("MATCH{" + promotedDocs.get(0) + "}:DRETITLE"));

        assertThat(searchPage.getSearchResult(1).getTitleString(), is(promotedDocs.get(0)));
        assertThat(searchPage.getHeadingResultsCount(), is(1));
        assertThat(searchPage.countPinToPositionLabels(), is(1));

        searchPage.filterBy(new FieldTextFilter("MATCH{" + promotedDocs.get(1) + "}:DRETITLE"));

        assertThat(promotedDocs.get(1) + " not visible in the search title", searchPage.getSearchResult(1).getTitleString(), is(promotedDocs.get(1)));
        assertThat("Wrong number of search results", searchPage.getHeadingResultsCount(), is(1));
        assertThat("Wrong number of pin to position labels", searchPage.countPinToPositionLabels(), is(1));
    }
}
