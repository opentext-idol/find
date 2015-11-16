package com.autonomy.abc.analytics;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.analytics.Term;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.core.Is.is;

public class AnalyticsITCase extends HostedTestBase {
    private AnalyticsPage analytics;
    private final static Logger LOGGER = LoggerFactory.getLogger(AnalyticsITCase.class);

    public AnalyticsITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        analytics = getElementFactory().getAnalyticsPage();
        body = getBody();
    }

    @Test
    public void testPopularTermsSearchOptimizer(){
        Term mostPopular = analytics.getMostPopularSearchTerm();

        int mostPopularSearchCount = mostPopular.getSearchCount();

        LOGGER.info(String.valueOf(mostPopularSearchCount));

        body.getTopNavBar().search(mostPopular.getTerm());

        getElementFactory().getSearchPage();
        body = getBody();

        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        analytics = getElementFactory().getAnalyticsPage();

        mostPopular = analytics.getMostPopularSearchTerm();

        LOGGER.info(String.valueOf(mostPopular.getSearchCount()));

        verifyThat(mostPopular.getSearchCount(), is(mostPopularSearchCount + 1));

        mostPopularSearchCount = mostPopular.getSearchCount();

        analytics.getMostPopularSearchTerm().getElement().click();

        getElementFactory().getSearchPage();

        body = getBody();

        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        analytics = getElementFactory().getAnalyticsPage();

        mostPopular = analytics.getMostPopularSearchTerm();

        LOGGER.info(String.valueOf(mostPopular.getSearchCount()));

        verifyThat(mostPopular.getSearchCount(), is(mostPopularSearchCount + 1));
    }

    @Test
    public void testPopularTermsFind(){
        Term mostPopular = analytics.getMostPopularSearchTerm();
        int mostPopularSearchCount = mostPopular.getSearchCount();

        List<String> browserHandles = analytics.createAndListWindowHandles();

        getDriver().switchTo().window(browserHandles.get(1));
        getDriver().get(config.getFindUrl());
        getDriver().manage().window().maximize();
        FindPage find = getElementFactory().getFindPage();

        find.search(mostPopular.getTerm());

        getDriver().switchTo().window(browserHandles.get(0));

        getDriver().navigate().refresh();

        body = getBody();
        analytics = getElementFactory().getAnalyticsPage();

        assertThat(analytics.getMostPopularSearchTerm().getSearchCount(),is(mostPopularSearchCount + 1));
    }

    @Test
    public void testPopularPromotionsClickthrough(){
        WebElement mostPopular = analytics.getMostPopularPromotion();

        String promotionsName = mostPopular.getText();

        mostPopular.click();

        PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
    }

    @After
    public void tearDown(){

    }

}
