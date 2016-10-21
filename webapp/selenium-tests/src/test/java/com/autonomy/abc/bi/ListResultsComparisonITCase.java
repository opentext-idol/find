package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.comparison.AppearsIn;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.find.results.SimilarDocumentsView;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Because comparisons are so slow, these rely on quite specific data
 * to ensure few results. You may need to tweak the searches to get
 * them working on your local machine
 */
@Role(UserRole.BIFHI)
public class ListResultsComparisonITCase extends IdolFindTestBase {
    private FindService findService;
    private SavedSearchService savedSearchService;
    private BIIdolFindElementFactory elementFactory;

    private ResultsComparisonView resultsComparison;
    private IdolFindPage findPage;

    public ListResultsComparisonITCase(final TestConfig config) {
        super(config);
    }

    @Override
    public BIIdolFindElementFactory getElementFactory() {
        return (BIIdolFindElementFactory) super.getElementFactory();
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        savedSearchService = getApplication().savedSearchService();
        elementFactory = getElementFactory();
        findService.search("long set-up");

        savedSearchService.waitForSomeTabsAndDelete();

        elementFactory.getConceptsPanel().removeAllConcepts();
        findPage = getElementFactory().getFindPage();
        findPage.goToListView();
        elementFactory.getResultsPage().waitForResultsToLoad();
    }

    @After
    public void tearDown() {
        getDriver().get(getConfig().getAppUrl(getApplication()));
        elementFactory.getFindPage().waitUntilDatabasesLoaded();
        savedSearchService.waitForSomeTabsAndDelete();
    }

    @SuppressWarnings("Duplicates")
    @Test
    @ResolvedBug("FIND-232")
    public void testComparisonButtonActivates() {
        findService.search("\"Unicorns\"");
        assertThat(findPage.compareButton(), hasClass("disabled"));
        savedSearchService.openNewTab();
        findService.search("\"Pegasus\"");
        Waits.loadOrFadeWait();
        assertThat(findPage.compareButton(), not(hasClass("disabled")));
        elementFactory.getSearchTabBar().tabFromIndex(0);
        assertThat(findPage.compareButton(), not(hasClass("disabled")));
    }

    @Test
    //TODO needs to find 2 search terms that never share results but each have results
    public void testNoOverlap() {
        findPage.waitForParametricValuesToLoad();

        final FilterPanel filters = elementFactory.getFilterPanel();
        filters.indexesTreeContainer().expand();

        final Query polar = new Query("\"polar bear\"").withFilter(new IndexFilter(filters.getIndex(0)));
        final Query opposites = new Query("\"opposable thumbs\"").withFilter(new IndexFilter(filters.getIndex(1)));

        searchAndSave(polar, "polar");
        savedSearchService.openNewTab();

        findPage.waitUntilDatabasesLoaded();

        elementFactory.getFilterPanel().indexesTreeContainer().expand();
        searchAndSave(opposites, "opposites");
        savedSearchService.compareCurrentWith("polar");

        resultsComparison = elementFactory.getResultsComparison();
        Waits.loadOrFadeWait();
        resultsComparison.goToListView();
        final ResultsView resultsView = resultsComparison.resultsView(AppearsIn.BOTH);

        assertThat(resultsView.getResults(), empty());
        assertThat(resultsView, containsText("No results found"));

        findPage.goBackToSearch();
    }

    @Test
    @ActiveBug("FIND-240")
    public void testSubSearch() {
        final Query outerQuery = new Query("documents");
        final Query innerQuery = new Query("\"secret documents\"");
        searchAndSave(outerQuery, "outer");
        final int outerCount = getTotalResults();
        savedSearchService.openNewTab();
        searchAndSave(innerQuery, "inner");
        Waits.loadOrFadeWait();
        ResultsView resultsView = elementFactory.getResultsPage();
        resultsView.goToListView();

        final int innerCount = getTotalResults();

        savedSearchService.compareCurrentWith("outer");
        resultsComparison = elementFactory.getResultsComparison();
        resultsComparison.goToListView();

        verifyThat(resultsComparison.getResults(AppearsIn.THIS_ONLY), empty());
        verifyThat(resultsComparison.getResults(AppearsIn.OTHER_ONLY), not(empty()));

        verifyThat(resultsComparison.getResultsCountFor(AppearsIn.THIS_ONLY), is(0));
        verifyThat(resultsComparison.getResultsCountFor(AppearsIn.BOTH), is(innerCount));
        verifyThat(resultsComparison.getResultsCountFor(AppearsIn.OTHER_ONLY), is(outerCount - innerCount));

        findPage.goBackToSearch();
    }

    private int getTotalResults() {
        return elementFactory.getResultsPage().getResultsCount();
    }

    @Test
    @ResolvedBug("FIND-228")
    public void testCompareUnsavedSearches() {
        findService.search("\"not many results\"");
        savedSearchService.openNewTab();
        findService.search("\"to speed up comparison\"");

        final ComparisonModal modal = findPage.openCompareModal();
        modal.select("New Search");
        modal.compareButton().click();

        Exception thrown = null;
        try {
            // server appears to cancel comparison request after 90s
            modal.waitForComparisonToLoad(100);
        } catch (final TimeoutException e) {
            thrown = e;
        }
        assertThat(thrown, nullValue());

        findPage.goBackToSearch();
    }

    @Test
    @ResolvedBug("FIND-232")
    public void testAllSavedSearchesAreComparable() {
        searchAndSave(new Query("\"alice in wonderland\""), "alice", SearchType.QUERY);
        savedSearchService.openNewTab();
        searchAndSave(new Query("\"bob's burgers\""), "bob", SearchType.SNAPSHOT);
        searchAndSave(new Query("\"charlie's angels\""), "charlie", SearchType.QUERY);

        final Collection<String> bobOptions = getModalOptionsForTab("bob");
        verifyThat(bobOptions, hasItems("alice", "charlie"));
        verifyThat(bobOptions, not(hasItem("bob")));

        final Collection<String> aliceOptions = getModalOptionsForTab("alice");
        verifyThat(aliceOptions, hasItems("bob", "charlie"));
        verifyThat(aliceOptions, not(hasItem("alice")));
    }

    private List<String> getModalOptionsForTab(final String tabName) {
        elementFactory.getSearchTabBar().switchTo(tabName);
        assertThat(findPage.compareButton(), not(disabled()));
        final ComparisonModal modal = findPage.openCompareModal();
        verifyThat(modal.getSelected(), is(tabName));
        final List<String> options = modal.getItems();
        modal.close();
        return options;
    }

    @Test
    public void testSimilarDocumentsNavigation() {
        final Index expectedIndex = new Index("Wikipedia");
        final String comparedTabName = "nope";
        final String expectedTabName = "expected";

        findPage.waitForParametricValuesToLoad();
        searchAndSave(new Query("face"), comparedTabName);
        savedSearchService.openNewTab();

        elementFactory.getFilterPanel().indexesTreeContainer().expand();
        searchAndSave(new Query("bus").withFilter(new IndexFilter(expectedIndex)), expectedTabName);

        elementFactory.getTopicMap().waitForMapLoaded();
        ResultsView resultsView = elementFactory.getResultsPage();
        resultsView.goToListView();
        final String firstTitle = resultsView.getResult(1).getTitleString();

        savedSearchService.compareCurrentWith(comparedTabName);
        ResultsComparisonView resultsComparison = elementFactory.getResultsComparison();
        resultsComparison.goToListView();

        resultsComparison.resultsView(AppearsIn.THIS_ONLY)
                .getResult(1)
                .similarDocuments()
                .click();

        final SimilarDocumentsView similarDocs = elementFactory.getSimilarDocumentsView();
        assertThat(similarDocs.getTitle(), containsString(firstTitle));

        similarDocs.backButton().click();
        final FilterPanel filters = elementFactory.getFilterPanel();

        assertThat(elementFactory.getResultsPage().getResult(1).getTitleString(), is(firstTitle));
        assertThat(filters.indexesTree().getSelected(), is(Collections.singletonList(expectedIndex)));
        assertThat(elementFactory.getSearchTabBar().getCurrentTabTitle(), is(expectedTabName));
    }

    @Test
    @ResolvedBug("FIND-239")
    public void testComparingWhenZeroResults() {
        searchAndSave(new Query("lsijfielsjfiesjflisejlijlij"), "contagion");
        assumeThat("1 search has 0 results", findPage.totalResultsNum(), is(0));

        savedSearchService.openNewTab();
        searchAndSave(new Query("virus"), "ill");

        savedSearchService.compareCurrentWith("contagion");
        verifyThat("Has compared the searches", findPage.resultsComparisonVisible());

        findPage.goBackToSearch();
    }

    @Test
    @ActiveBug("FIND-634")
    public void testEditingSavedSearchThenComparing() {
        final String otherSearchName = "Not Changin'";
        searchAndSave(new Query("car"), otherSearchName);
        savedSearchService.openNewTab();

        final String originalSearch = "face";

        searchAndSave(new Query(originalSearch), "Gonna Change");
        findPage.goToListView();

        final String originalFirstResult = getElementFactory()
                .getResultsPage()
                .getResult(1)
                .title()
                .getText();

        elementFactory.getConceptsPanel().removeAllConcepts();
        findService.search("stuff AND (NOT " + originalSearch + ")");

        verifyThat("Tab is marked as modified", elementFactory.getSearchTabBar().currentTab().isNew());
        savedSearchService.compareCurrentWith(otherSearchName);
        resultsComparison = elementFactory.getResultsComparison();
        resultsComparison.goToListView();

        verifyThat(resultsComparison.getResults(AppearsIn.THIS_ONLY).get(0).getTitleString(), not(originalFirstResult));
    }



    private void searchAndSave(final Query query, final String saveAs) {
        searchAndSave(query, saveAs, SearchType.QUERY);
    }

    private void searchAndSave(final Query query, final String saveAs, final SearchType saveType) {
        findService.search(query);
        new WebDriverWait(getDriver(), 30L).withMessage("Buttons should become active").until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".service-view-container:not(.hide) .save-button:not(.disabled)")));
        savedSearchService.saveCurrentAs(saveAs, saveType);
    }
}
