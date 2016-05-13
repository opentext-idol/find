package com.autonomy.abc.search;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Pagination;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchPaginationITCase extends HybridIsoTestBase {
    private SearchPage searchPage;
    private SearchService searchService;

    public SearchPaginationITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        searchService = getApplication().searchService();
    }

    @Test
    @ResolvedBug("CCUK-2661")
    public void testSearchResultsPagination() {
        searchPage = searchService.search("grass");
        Waits.loadOrFadeWait();
        checkPageButtonDisabled(Pagination.FIRST);
        checkPageButtonDisabled(Pagination.PREVIOUS);

        searchPage.switchResultsPage(Pagination.NEXT);
        checkPageButtonEnabled(Pagination.FIRST);
        checkPageButtonEnabled(Pagination.PREVIOUS);
        checkOnPage(2);

        searchPage.switchResultsPage(Pagination.NEXT);
        searchPage.switchResultsPage(Pagination.NEXT);
        searchPage.switchResultsPage(Pagination.PREVIOUS);
        checkOnPage(3);

        searchPage.switchResultsPage(Pagination.FIRST);
        checkOnPage(1);

        searchPage.switchResultsPage(Pagination.LAST);
        checkPageButtonDisabled(Pagination.NEXT);
        checkPageButtonDisabled(Pagination.LAST);

        final int numberOfPages = searchPage.getCurrentPageNumber();
        for (int i = numberOfPages - 1; i > 0; i--) {
            searchPage.switchResultsPage(Pagination.PREVIOUS);
            checkOnPage(i);
        }

        for (int j = 2; j < numberOfPages + 1; j++) {
            searchPage.switchResultsPage(Pagination.NEXT);
            checkOnPage(j);
        }
    }

    @Test
    @ResolvedBug("CCUK-2565")
    public void testPaginationAndBackButton() {
        searchPage = searchService.search("safe");
        searchPage.switchResultsPage(Pagination.LAST);
        final int lastPage = searchPage.getCurrentPageNumber();

        checkPageButtonDisabled(Pagination.LAST);
        checkPageButtonDisabled(Pagination.NEXT);
        checkPageButtonEnabled(Pagination.FIRST);
        checkPageButtonEnabled(Pagination.PREVIOUS);

        getDriver().navigate().back();
        checkOnPage(1);

        getDriver().navigate().forward();
        checkOnPage(lastPage);
    }

    @Test
    @ActiveBug("CSA-1819")
    public void testNavigateBeyondEndOfResults() {
        searchPage = searchService.search("nice");
        searchPage.switchResultsPage(Pagination.LAST);
        final int lastPage = searchPage.getCurrentPageNumber();
        final String docTitle = searchPage.getSearchResult(1).getTitleString();

        assertThat(getWindow(), urlContains("nice/" + lastPage));

        final String illegitimateUrl = getWindow().getUrl().replace("nice/" + lastPage, "nice/" + (lastPage + 5));
        getWindow().goTo(illegitimateUrl);
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();

        assertThat("Page doesn't contain error",searchPage.getText(),not(containsString("Error Occurred")));
        checkOnPage(lastPage);
        assertThat(searchPage.getSearchResult(1).getTitleString(), is(docTitle));
    }

    @Test
    @ResolvedBug("CSA-1629")
    public void testPinToPositionPagination(){
        PromotionService promotionService = getApplication().promotionService();

        try {
            promotionService.setUpPromotion(new PinToPositionPromotion(1, "thiswillhavenoresults"), "*", SearchPage.RESULTS_PER_PAGE + 2);
            searchPage = getElementFactory().getSearchPage();
            searchPage.waitForSearchLoadIndicatorToDisappear();

            checkPageButtonEnabled(Pagination.NEXT);
            searchPage.switchResultsPage(Pagination.NEXT);

            verifyThat(searchPage.visibleDocumentsCount(), is(2));
        } finally {
            promotionService.deleteAll();
        }
    }

    private void checkPageButtonDisabled(Pagination button) {
        assertThat(searchPage.resultsPaginationButton(button), disabled());
    }

    private void checkPageButtonEnabled(Pagination button) {
        assertThat(searchPage.resultsPaginationButton(button), not(disabled()));
    }

    private void checkOnPage(int i) {
        assertThat("on page " + i, searchPage.getCurrentPageNumber(), is(i));
        assertThat(getWindow(), url(endsWith(String.valueOf(i))));
    }

    @Test
    public void testMultiDocPromotionDrawerExpandAndPagination() {
        Promotion promotion = new SpotlightPromotion("boat");

        PromotionService promotionService = getApplication().promotionService();
        promotionService.deleteAll();
        promotionService.setUpPromotion(promotion, "freeze", 18);

        try {
            PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails(promotion);

            promotionsDetailPage.getTriggerForm().clickTrigger("boat");
            searchPage = getElementFactory().getSearchPage();
            searchPage.waitForPromotionsLoadIndicatorToDisappear();

            assertThat("two promotions visible", searchPage.getPromotionSummarySize(), is(2));
            assertThat("can show more", searchPage.showMorePromotionsButton(), displayed());

            searchPage.showMorePromotions();
            assertThat("showing more", searchPage.getPromotionSummarySize(), is(5));

            searchPage.showLessPromotions();
            assertThat("showing less", searchPage.getPromotionSummarySize(), is(2));

            searchPage.showMorePromotions();
            assertThat("showing more again", searchPage.getPromotionSummarySize(), is(5));

            searchPage.switchPromotionPage(Pagination.NEXT);
            LOGGER.info("on page 2");
            verifyPromotionPagination(true, true);

            searchPage.switchPromotionPage(Pagination.NEXT);
            searchPage.switchPromotionPage(Pagination.NEXT);
            LOGGER.info("on last page");
            verifyPromotionPagination(true, false);

            for (int unused=0; unused < 3; unused++) {
                searchPage.switchPromotionPage(Pagination.PREVIOUS);
            }
            LOGGER.info("on first page");
            verifyPromotionPagination(false, true);

            searchPage.switchPromotionPage(Pagination.LAST);
            LOGGER.info("on last page");
            verifyPromotionPagination(true, false);

            searchPage.switchPromotionPage(Pagination.FIRST);
            LOGGER.info("on first page");
            verifyPromotionPagination(false, true);
        } finally {
            promotionService.deleteAll();
        }
    }

    private void verifyPromotionPagination(boolean previousEnabled, boolean nextEnabled) {
        verifyButtonEnabled("back to start", searchPage.promotionPaginationButton(Pagination.FIRST), previousEnabled);
        verifyButtonEnabled("back", searchPage.promotionPaginationButton(Pagination.PREVIOUS), previousEnabled);
        verifyButtonEnabled("forward", searchPage.promotionPaginationButton(Pagination.NEXT), nextEnabled);
        verifyButtonEnabled("forward to end", searchPage.promotionPaginationButton(Pagination.LAST), nextEnabled);
    }

    private void verifyButtonEnabled(String name, WebElement element, boolean enabled) {
        if (enabled) {
            verifyThat(name + " button enabled", element, not(hasClass("disabled")));
        } else {
            verifyThat(name + " button disabled", element, hasClass("disabled"));
        }
    }
}
