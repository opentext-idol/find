package com.autonomy.abc.analytics;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.analytics.Container;
import com.autonomy.abc.selenium.page.analytics.ContainerItem;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsTextIgnoringCase;
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
        searchService = getApplication().searchService();

        goToAnalytics();
    }

    private void goToAnalytics() {
        analytics = getApplication().switchTo(AnalyticsPage.class);
    }

    @Test
    public void testSortDirection() {
        for (Container container : analytics.containers()) {
            verifySorted(container.getItems(), ContainerItem.COUNT_DESCENDING);
            container.toggleSortDirection();
            verifySorted(container.getItems(), ContainerItem.COUNT_ASCENDING);
            container.toggleSortDirection();
            verifySorted(container.getItems(), ContainerItem.COUNT_DESCENDING);
        }
    }

    private <T> void verifySorted(List<T> toCheck, Comparator<T> sorter) {
        List<T> sorted = new ArrayList<>(toCheck);
        Collections.sort(sorted, sorter);
        verifyThat("sorted " + sorter, toCheck, is(sorted));
    }

    @Test
    public void testTimePeriods() {
        for (Container container : analytics.containers()) {
            container.selectPeriod(Container.Period.DAY);
            verifyThat(container.getSelectedPeriod(), is(Container.Period.DAY));
            List<ContainerItem> dayItems = container.getItems();

            container.selectPeriod(Container.Period.WEEK);
            verifyThat(container.getSelectedPeriod(), is(Container.Period.WEEK));
            List<ContainerItem> weekItems = container.getItems();
            verifyFirstListBigger(weekItems, dayItems, ContainerItem.COUNT_ASCENDING);

            container.selectPeriod(Container.Period.MONTH);
            verifyThat(container.getSelectedPeriod(), is(Container.Period.MONTH));
            List<ContainerItem> monthItems = container.getItems();
            verifyFirstListBigger(monthItems, weekItems, ContainerItem.COUNT_ASCENDING);
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
    @KnownBug("CSA-2054")
    public void testPopularTerms() {
        final String mostPopularSearchTerm = "cat";
        final String mostPopularFindTerm = "dog";
        repeatedSearch(mostPopularSearchTerm, 5);
        repeatedFind(mostPopularFindTerm, 4);

        verifyThat(analytics.getPopularSearch(0), equalToIgnoringCase(mostPopularSearchTerm));
        verifyThat(analytics.getPopularSearch(1), equalToIgnoringCase(mostPopularFindTerm));
    }

    @Test
    @KnownBug("CSA-2054")
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

        try {
            findWindow.activate();
            FindPage findPage = new HSODFind(findWindow).elementFactory().getFindPage();
            for (int unused = 0; unused < repeats; unused++) {
                findPage.search(term);
                findPage.search("");
                Waits.loadOrFadeWait();
            }
        } finally {
            findWindow.close();
            searchWindow.activate();
        }
    }

    @Test
    public void testExistingPromotion() {
        final StaticPromotion promotion = new StaticPromotion("title", "body", "trigger");

        HSOPromotionService promotionService = getApplication().promotionService();

        try {
            promotionService.goToPromotions().getPromotionLinkWithTitleContaining(promotion.getTrigger());
        } catch (NoSuchElementException e) {
            promotionService.setUpStaticPromotion(promotion);
            searchService.search(promotion.getTrigger());
        }
        searchService.search(promotion.getTrigger());
        goToAnalytics();

        ContainerItem promotionItem = analytics.promotions().get(promotion.getTrigger());
        verifyPromotionTitle(promotionItem.getTerm(), promotion);

        promotionItem.click();
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

        HSOPromotionService promotionService = getApplication().promotionService();
        promotionService.setUpStaticPromotion(promotion);
        try {
            goToAnalytics();

            Container promotions = analytics.promotions();
            promotions.toggleSortDirection();
            promotions.selectPeriod(Container.Period.DAY);

            ContainerItem bottomPromotion = analytics.promotions().get(promotion.getTrigger());
            verifyPromotionTitle(bottomPromotion.getTerm(), promotion);
        } finally {
            promotionService.delete(promotion);
        }

        goToAnalytics();

        Container promotions = analytics.promotions();
        promotions.toggleSortDirection();
        promotions.selectPeriod(Container.Period.DAY);
        ContainerItem bottomPromotion = analytics.promotions().get(promotion.getTrigger());
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

    @Test
    @KnownBug("CSA-1752")
    public void testNonZeroSearchTerm() {
        verifyThat(analytics.getMostPopularNonZeroSearchTerm(), not(isEmptyOrNullString()));
    }

    @Test
    public void testNoErrorsOnPageLoad(){
        verifyThat(analytics, not(containsTextIgnoringCase("error")));
    }
    
    @After
    public void tearDown(){

    }

}
