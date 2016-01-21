package com.autonomy.abc.analytics;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.analytics.Container;
import com.autonomy.abc.selenium.page.analytics.Term;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

/* This test case is a self-fulfilling prophecy:
* assuming that nothing else touches this account,
* the tests will perform actions to make the test valid
* the next time it is run
* (is there a better way to do this?)
*/
public class AnalyticsITCase extends HostedTestBase {
    private AnalyticsPage analytics;
    private SearchService searchService;

    public AnalyticsITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("analytics_tests"));
    }

    @Before
    public void setUp(){
        searchService = getApplication().createSearchService(getElementFactory());

        goToAnalytics();
    }

    private void goToAnalytics() {
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        analytics = getElementFactory().getAnalyticsPage();
    }

    @Test
    public void testSortDirection() {
        for (Container container : analytics.containers()) {
            verifySorted(container.getTerms(), Term.COUNT_DESCENDING);
            container.toggleSortDirection();
            verifySorted(container.getTerms(), Term.COUNT_ASCENDING);
            container.toggleSortDirection();
            verifySorted(container.getTerms(), Term.COUNT_DESCENDING);
        }
    }

    private <T> void verifySorted(List<T> toCheck, Comparator<T> sorter) {
        List<T> sorted = new ArrayList<>(toCheck);
        sorted.sort(sorter);
        verifyThat("sorted " + sorter, toCheck, is(sorted));
    }

    @Test
    public void testTimePeriods() {
        for (Container container : analytics.containers()) {
            container.selectPeriod(Container.Period.DAY);
            verifyThat(container.getSelectedPeriod(), is(Container.Period.DAY));
            List<Term> dayTerms = container.getTerms();

            container.selectPeriod(Container.Period.WEEK);
            verifyThat(container.getSelectedPeriod(), is(Container.Period.WEEK));
            List<Term> weekTerms = container.getTerms();
            verifyFirstListBigger(weekTerms, dayTerms, Term.COUNT_DESCENDING);

            container.selectPeriod(Container.Period.MONTH);
            verifyThat(container.getSelectedPeriod(), is(Container.Period.MONTH));
            List<Term> monthTerms = container.getTerms();
            verifyFirstListBigger(monthTerms, weekTerms, Term.COUNT_DESCENDING);
        }
    }

    private <T> void verifyFirstListBigger(List<T> bigList, List<T> smallList, Comparator<T> comparator) {
        verifyThat(bigList, hasSize(greaterThanOrEqualTo(smallList.size())));
        for (int i=0; i<Math.min(bigList.size(), smallList.size()); i++) {
            T bigItem = bigList.get(i);
            T smallItem = smallList.get(i);
            verifyThat(bigItem + " bigger than " + smallItem, comparator.compare(bigItem, smallItem) >= 0);
        }
    }

    @Test
    public void testPopularTerms() {
        final String mostPopularSearchTerm = "cat";
        final String mostPopularFindTerm = "dog";
        repeatedSearch(mostPopularSearchTerm, 5);
        repeatedFind(mostPopularFindTerm, 4);

        verifyThat(analytics.getPopularSearch(0), equalToIgnoringCase(mostPopularSearchTerm));
        verifyThat(analytics.getPopularSearch(1), equalToIgnoringCase(mostPopularFindTerm));
    }

    @Test
    public void testZeroHitTerms() {
        final String zeroHitTerm = "zerohitterm";
        final String zeroHitTermFind = "zerohitfind";
        repeatedSearch(zeroHitTerm, 3);
        repeatedFind(zeroHitTermFind, 2);

        verifyThat(analytics.getZeroHitSearch(0), equalToIgnoringCase(zeroHitTerm));
        verifyThat(analytics.getZeroHitSearch(1), equalToIgnoringCase(zeroHitTermFind));
    }

    private void repeatedSearch(String term, int repeats) {
        for (int unused = 0; unused < repeats; unused++) {
            searchService.search(term);
        }
        goToAnalytics();
    }

    private void repeatedFind(String term, int repeats) {
        Window searchWindow = getMainSession().getActiveWindow();
        Window findWindow = getMainSession().openWindow(config.getFindUrl());

        findWindow.activate();
        Find find = getElementFactory().getFindPage();
        for (int unused = 0; unused < repeats; unused++) {
            find.search(term);
        }
        findWindow.close();
        searchWindow.activate();
    }

    @Test
    public void testExistingPromotion() {
        final StaticPromotion promotion = new StaticPromotion("title", "body", "trigger");

        HSOPromotionService promotionService = getApplication().createPromotionService(getElementFactory());

        if (promotionService.goToPromotions().promotionsList().isEmpty()) {
            promotionService.setUpStaticPromotion(promotion);
            searchService.search(promotion.getTrigger());
        }
        searchService.search(promotion.getTrigger());
        goToAnalytics();

        Term promotionTerm = analytics.promotions().get(promotion.getTrigger());
        verifyPromotionTitle(promotionTerm.getTerm(), promotion);

        promotionTerm.click();
        boolean loadsCorrectPage = false;
        String detailTitle = null;
        try {
            PromotionsDetailPage detailPage = getElementFactory().getPromotionsDetailPage();
            loadsCorrectPage = true;
            detailTitle = detailPage.promotionTitle().getValue();
        } catch (Exception e) {
            /* top promotion was likely deleted */
        }
        verifyThat("successfully loaded promotions detail page", loadsCorrectPage);
        verifyPromotionTitle(detailTitle, promotion);
    }

    @Test
    public void testDeletedPromotion() {
        final StaticPromotion promotion = new StaticPromotion("title", "body", "deleted");

        HSOPromotionService promotionService = getApplication().createPromotionService(getElementFactory());
        promotionService.setUpStaticPromotion(promotion);
        try {
            goToAnalytics();

            Container promotions = analytics.promotions();
            promotions.toggleSortDirection();
            promotions.selectPeriod(Container.Period.DAY);

            Term bottomPromotion = analytics.promotions().get(promotion.getTrigger());
            verifyPromotionTitle(bottomPromotion.getTerm(), promotion);
        } finally {
            promotionService.delete(promotion);
        }

        goToAnalytics();

        Container promotions = analytics.promotions();
        promotions.toggleSortDirection();
        promotions.selectPeriod(Container.Period.DAY);
        Term bottomPromotion = analytics.promotions().get(promotion.getTrigger());
        verifyPromotionTitle(bottomPromotion.getTerm(), promotion);
        bottomPromotion.click();

        boolean loadsCorrectPage = false;
        try {
            getElementFactory().getPromotionsPage();
            loadsCorrectPage = true;
        } catch (Exception e) {
            /* taken to the wrong page */
        }
        verifyThat("successfully redirected to promotions page", loadsCorrectPage);
    }

    private void verifyPromotionTitle(String value, Promotion promotion) {
        String lowercase = (value == null) ? null : value.toLowerCase();
        verifyThat(lowercase, containsString(promotion.getName().toLowerCase()));
        verifyThat(lowercase, containsString(promotion.getTrigger().toLowerCase()));
    }

    @After
    public void tearDown(){

    }

}
