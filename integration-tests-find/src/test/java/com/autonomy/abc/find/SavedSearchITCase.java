package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchTab;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SavedSearchITCase extends IdolFindTestBase {
    private FindService findService;
    private SavedSearchService saveService;

    public SavedSearchITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findService = getApplication().findService();
        saveService = getApplication().savedSearchService();
    }

    @After
    public void tearDown() {
        if (hasSetUp()) {
            saveService.deleteAll();
        }
    }

    @Test
    public void testCanSaveSearch() {
        findService.search("queen");

        saveService.saveCurrentAs("save me", SearchType.QUERY);

        SearchTab currentTab = getElementFactory().getSearchTabBar().currentTab();
        assertThat(currentTab.getTitle(), is("save me"));
        assertThat(currentTab.getTitle(), not(containsString("New Search")));
        assertThat(currentTab, not(modified()));
    }

    @Test
    public void testSnapshotSavedInNewTab() {
        findService.search("crocodile");

        saveService.saveCurrentAs("snap", SearchType.SNAPSHOT);

        List<SearchTab> tabs = getElementFactory().getSearchTabBar().tabs();
        assertThat(tabs, hasSize(2));
        assertThat(tabs.get(0), is(modified()));
        assertThat(tabs.get(0).getType(), is(SearchType.QUERY));
        assertThat(tabs.get(1), not(modified()));
        assertThat(tabs.get(1).getType(), is(SearchType.SNAPSHOT));
    }

    private static Matcher<SearchTab> modified() {
        return new TypeSafeMatcher<SearchTab>() {
            @Override
            protected boolean matchesSafely(SearchTab searchTab) {
                return searchTab.isNew();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a modified tab");
            }
        };
    }
}
