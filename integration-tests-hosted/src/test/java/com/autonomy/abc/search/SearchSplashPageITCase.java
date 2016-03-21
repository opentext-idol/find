package com.autonomy.abc.search;

import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.logging.KnownBug;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.autonomy.abc.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchSplashPageITCase extends HostedTestBase {
    private TopNavBar topNavBar;
    private SideNavBar sideNavBar;
    private SearchPage searchPage;

    public SearchSplashPageITCase(TestConfig config) {
        super(config);
    }

    @Test
    @KnownBug("CSA-2040")
    public void testBigSearch() {
        setNavBars();
        goToSearchVia(PromotionsPage.class);
        verifyBigSearch();

        getWindow().refresh();
        setNavBars();
        verifyBigSearch();

        goToSearchVia(IndexesPage.class);
        verifyBigSearch();

        topNavBar.search("hello");
        searchPage = getElementFactory().getSearchPage();
        verifyRealSearch();
    }

    @Test
    @KnownBug("CCUK-2658")
    public void testWhitespaceSearch() {
        setNavBars();
        goToSearchVia(PromotionsPage.class);

        topNavBar.search("     ");
        verifyBigSearch();

        topNavBar.search("not white space");
        searchPage = getElementFactory().getSearchPage();
        topNavBar.search(" ");
        searchPage.waitForSearchLoadIndicatorToDisappear();

        verifyRealSearch();
        verifyThat(searchPage.getHeadingSearchTerm(), is("not white space"));
    }

    private void goToSearchVia(Class<? extends AppPage> otherPage) {
        getApplication().switchTo(otherPage);
        setNavBars();
		/* SearchSplashPage is-not-a SearchPage */
        sideNavBar.switchPage(NavBarTabId.SEARCH);
        setNavBars();
    }

    private void setNavBars() {
        topNavBar = getElementFactory().getTopNavBar();
        sideNavBar = getElementFactory().getSideNavBar();
    }

    private void verifyRealSearch() {
        verifyThat(searchPage.getSearchResults(), not(empty()));
        verifyThat(topNavBar, not(searchBoxIsDown()));
    }

    private void verifyBigSearch() {
        verifyThat(topNavBar.searchBox(), displayed());
        verifyThat(topNavBar, searchBoxIsDown());
    }

    private Matcher<? super TopNavBar> searchBoxIsDown() {
        return new TypeSafeMatcher<TopNavBar>() {
            @Override
            protected boolean matchesSafely(TopNavBar topNavBar) {
                return topNavBar.findElements(By.className("animated-down-search")).size() > 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("search box in the middle of the page");
            }
        };
    }

}
