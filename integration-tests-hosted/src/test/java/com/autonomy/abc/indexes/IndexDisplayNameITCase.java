package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.util.PageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexDisplayNameITCase extends HostedTestBase {
    private IndexService indexService;
    private IndexesPage indexesPage;
    private Index testIndex;

    public IndexDisplayNameITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        indexService = getApplication().createIndexService(getElementFactory());
        testIndex = new Index("thisisabittooyobbish5me","D1spl4y Nam3 AbC 123");
        indexesPage = indexService.setUpIndex(testIndex);
        body = getBody();
    }

    @After
    public void tearDown(){
        indexService.deleteAllIndexes();
    }

    @Test
    public void testIndexPageList(){
        verifyThat(indexesPage.getIndexNames(), hasItem(testIndex.getDisplayName()));
    }

    @Test
    public void testSearchFilter(){
        body.getTopNavBar().search("Crickets Throw Their Voices");

        SearchPage searchPage = getElementFactory().getSearchPage();

        for(IndexNodeElement node : searchPage.indexesTree().privateIndexes()){
            verifyThat(node.getName(), anyOf(is(Index.DEFAULT.getDisplayName()), is(testIndex.getDisplayName())));
        }
    }

    @Test
    public void testPieChartLink(){
        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        AnalyticsPage analyticsPage = getElementFactory().getAnalyticsPage();

        analyticsPage.indexSizeChart().click();

        verifyThat(PageUtil.getWrapperContent(getDriver()), not(containsText("does not exist")));
    }

}
