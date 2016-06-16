package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.application.IdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.autonomy.abc.selenium.find.save.*;
import com.autonomy.abc.selenium.query.ParametricFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.autonomy.abc.matchers.ErrorMatchers.isError;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.checked;
import static org.hamcrest.Matchers.*;

public class SavedSearchITCase extends IdolFindTestBase {
    private SearchTabBar searchTabBar;

    private FindService findService;
    private SavedSearchService saveService;

    public SavedSearchITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findService = getApplication().findService();
        saveService = getApplication().savedSearchService();

        findService.search("*");
        searchTabBar = getElementFactory().getSearchTabBar();
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

        final SearchTab currentTab = searchTabBar.currentTab();
        assertThat(currentTab.getTitle(), is("save me"));
        assertThat(currentTab.getTitle(), not(containsString("New Search")));
        assertThat(currentTab, not(modified()));
    }

    @Test
    public void testSnapshotSavedInNewTab() {
        findService.search("crocodile");

        saveService.saveCurrentAs("snap", SearchType.SNAPSHOT);

        final List<SearchTab> tabs = searchTabBar.tabs();
        assertThat(tabs, hasSize(2));
        assertThat(tabs.get(0), is(modified()));
        assertThat(tabs.get(0).getType(), is(SearchType.QUERY));
        assertThat(tabs.get(1), not(modified()));
        assertThat(tabs.get(1).getType(), is(SearchType.SNAPSHOT));
    }

    @Test
    public void testOpenSnapshotAsQuery() {
        findService.search("open");
        saveService.saveCurrentAs("sesame", SearchType.SNAPSHOT);
        findService.search("no longer open");
        searchTabBar.tab("sesame").activate();

        getElementFactory().getSearchOptionsBar().openSnapshotAsQuery();

        assertThat(searchTabBar.currentTab().getTitle(), is("New Search"));
        assertThat(searchTabBar.currentTab().getType(), is(SearchType.QUERY));
        assertThat(searchTabBar.tab("sesame").getType(), is(SearchType.SNAPSHOT));
        assertThat(getElementFactory().getTopNavBar().getSearchBoxTerm(), is("open"));
    }

    @Test
    public void testDuplicateNamesPrevented() {
        saveService.saveCurrentAs("duplicate", SearchType.QUERY);
        saveService.openNewTab();
        final SearchOptionsBar options = getElementFactory().getSearchOptionsBar();

        options.saveAsButton().click();
        options.searchTitleInput().setValue("duplicate");
        options.saveConfirmButton().click();
        assertThat(options.getSaveErrorMessage(), isError(Errors.Find.DUPLICATE_SEARCH));

        options.searchTypeButton(SearchType.SNAPSHOT).click();
        options.saveConfirmButton().click();
        assertThat(options.getSaveErrorMessage(), isError(Errors.Find.DUPLICATE_SEARCH));
    }

    @Test
    public void testSavedSearchVisibleInNewSession() {
        findService.search(new Query("live forever").withFilter(new ParametricFilter("OVERALL VIBE", "POSITIVE")));
        saveService.saveCurrentAs("oasis", SearchType.QUERY);

        final IdolFind other = new IdolFind();
        launchInNewSession(other);
        other.loginService().login(getConfig().getDefaultUser());
        other.findService().search("blur");

        final IdolFindElementFactory factory = other.elementFactory();
        factory.getSearchTabBar().tab("oasis").activate();
        factory.getFilterPanel().waitForParametricFields();
        assertThat(factory.getTopNavBar().getSearchBoxTerm(), is("live forever"));
        assertThat(factory.getFilterPanel().checkboxForParametricValue("OVERALL VIBE", "POSITIVE"), checked());
    }

    private static Matcher<SearchTab> modified() {
        return new TypeSafeMatcher<SearchTab>() {
            @Override
            protected boolean matchesSafely(final SearchTab searchTab) {
                return searchTab.isNew();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a modified tab");
            }
        };
    }
}
