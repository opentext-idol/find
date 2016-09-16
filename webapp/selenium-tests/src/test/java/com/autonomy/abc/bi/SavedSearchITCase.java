package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.autonomy.abc.selenium.find.numericWidgets.NumericWidgetService;
import com.autonomy.abc.selenium.find.save.*;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.autonomy.abc.matchers.ErrorMatchers.isError;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.checked;
import static org.hamcrest.Matchers.*;

@Role(UserRole.BIFHI)
public class SavedSearchITCase extends IdolFindTestBase {
    private SearchTabBar searchTabBar;
    private FindService findService;
    private SavedSearchService saveService;

    public SavedSearchITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        saveService = getApplication().savedSearchService();

        findService.search("*");
        getElementFactory().getFindPage().goToListView();
        searchTabBar = getElementFactory().getSearchTabBar();
    }

    @After
    public void tearDown() {
        saveService.deleteAll();
    }

    @Test
    @ResolvedBug("FIND-467")
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
        searchTabBar.switchTo("sesame");

        getElementFactory().getSearchOptionsBar().openSnapshotAsQuery();

        assertThat(searchTabBar.currentTab().getTitle(), is("New Search"));
        assertThat(searchTabBar.currentTab().getType(), is(SearchType.QUERY));
        assertThat(searchTabBar.tab("sesame").getType(), is(SearchType.SNAPSHOT));
        assertThat(getElementFactory().getTopNavBar().getSearchBoxTerm(), is("open"));
    }

    @Test
    public void testDuplicateNamesPrevented() {
        findService.search("useless");
        saveService.saveCurrentAs("duplicate", SearchType.QUERY);
        saveService.openNewTab();
        getElementFactory().getResultsPage().waitForResultsToLoad();

        checkSavingDuplicateThrowsError("duplicate",SearchType.QUERY);
        checkSavingDuplicateThrowsError("duplicate",SearchType.SNAPSHOT);
    }

    private void checkSavingDuplicateThrowsError(final String searchName, final SearchType type){
        Waits.loadOrFadeWait();
        final SearchOptionsBar options = saveService.nameSavedSearch(searchName,type);
        options.saveConfirmButton().click();
        assertThat(options.getSaveErrorMessage(), isError(Errors.Find.DUPLICATE_SEARCH));
        options.cancelSave();
    }

    @Test
    public void testSavedSearchVisibleInNewSession() {
        findService.search(new Query("live forever"));
        FilterPanel filterPanel = getElementFactory().getFilterPanel();
        filterPanel.waitForParametricFields();

        int index = filterPanel.nonZeroParaFieldContainer(0);
        filterPanel.parametricField(index).expand();
        filterPanel.checkboxForParametricValue(index,0).check();

        saveService.saveCurrentAs("oasis", SearchType.QUERY);

        final BIIdolFind other = new BIIdolFind();
        launchInNewSession(other);
        other.loginService().login(getConfig().getDefaultUser());
        other.findService().search("blur");

        final IdolFindElementFactory factory = other.elementFactory();
        factory.getSearchTabBar().switchTo("oasis");
        factory.getFilterPanel().waitForParametricFields();
        assertThat(factory.getTopNavBar().getSearchBoxTerm(), is("live forever"));
        assertThat(factory.getFilterPanel().checkboxForParametricValue(index, 0), checked());
    }

    @Test
    @ResolvedBug("FIND-278")
    public void testCannotChangeParametricValuesInSnapshot() {
        findService.search("terrible");
        final String searchName = "horrible";

        saveService.saveCurrentAs(searchName, SearchType.SNAPSHOT);
        searchTabBar.switchTo(searchName);

        IdolFindPage findPage = getElementFactory().getFindPage();
        findPage.goToSunburst();
        Waits.loadOrFadeWait();

        SavedSearchPanel panel = new SavedSearchPanel(getDriver());
        int originalCount = panel.resultCount();

        SunburstView results = getElementFactory().getSunburst();

        results.waitForSunburst();
        results.getIthSunburstSegment(1).click();
        results.waitForSunburst();

        verifyThat("Has not added filter",findPage.filterLabels(),hasSize(0));
        verifyThat("Same number of results",panel.resultCount(),is(originalCount));
    }

    @Test
    @ResolvedBug("FIND-284")
    public void testRenamingSnapshot() {
        final String originalName = "originalName";
        final String newName = "newName";

        findService.search("broken");

        saveService.saveCurrentAs(originalName, SearchType.SNAPSHOT);
        searchTabBar.switchTo(originalName);

        saveService.renameCurrentAs(newName);

        saveService.openNewTab();
        searchTabBar.switchTo(newName);
        verifyThat("Saved search has content",getElementFactory().getTopicMap().topicMapVisible());
    }

    @Test
    @ResolvedBug("FIND-269")
    public void testSearchesWithNumericFilters() {
        NumericWidgetService widgetService = getApplication().numericWidgetService();

        MainNumericWidget mainGraph = widgetService.searchAndSelectNthGraph(1,"saint");
        mainGraph.clickAndDrag(100, mainGraph.graph());

        saveService.saveCurrentAs("saaaaved", SearchType.QUERY);

        assertThat(searchTabBar.currentTab(),not(modified()));
    }

    @Test
    @ResolvedBug("FIND-167")
    public void testCannotSaveSearchWithWhitespaceAsName() {
        findService.search("yolo");
        SearchOptionsBar searchOptions = saveService.nameSavedSearch("   ", SearchType.QUERY);

        assertThat("Save button is disabled",!searchOptions.saveConfirmButton().isEnabled());
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
