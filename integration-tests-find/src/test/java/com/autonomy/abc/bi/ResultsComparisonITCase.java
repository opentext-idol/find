package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.comparison.AppearsIn;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import java.util.Collection;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;

/**
 * Because comparisons are so slow, these rely on quite specific data
 * to ensure few results. You may need to tweak the searches to get
 * them working on your local machine
 */
public class ResultsComparisonITCase extends IdolFindTestBase {
    private FindService findService;
    private SavedSearchService savedSearchService;

    private ResultsComparisonView resultsComparison;
    private IdolFindPage findPage;

    public ResultsComparisonITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        savedSearchService = getApplication().savedSearchService();

        findPage = getElementFactory().getFindPage();
    }

    @After
    public void tearDown() {
        if (hasSetUp()) {
            findService.search("back to results");
            savedSearchService.deleteAll();
        }
    }

    @Test
    public void testNoOverlap() {
        final Query polar = new Query("\"polar bear\"").withFilter(new IndexFilter("Wikipedia"));
        final Query opposites = new Query("\"opposable thumbs\"").withFilter(new IndexFilter("Wookiepedia"));

        searchAndSave(polar, "polar");
        savedSearchService.openNewTab();
        searchAndSave(opposites, "opposites");
        savedSearchService.compareCurrentWith("polar");

        resultsComparison = getElementFactory().getResultsComparison();
        final ResultsView resultsView = resultsComparison.resultsView(AppearsIn.BOTH);
        assertThat(resultsView.getResults(), empty());
        assertThat(resultsView, containsText("No results found"));
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
        final int innerCount = getTotalResults();

        savedSearchService.compareCurrentWith("outer");
        resultsComparison = getElementFactory().getResultsComparison();

        verifyThat(resultsComparison.getResults(AppearsIn.THIS_ONLY), empty());
        verifyThat(resultsComparison.getResults(AppearsIn.OTHER_ONLY), not(empty()));

        verifyThat(resultsComparison.getResultsCountFor(AppearsIn.THIS_ONLY), is(0));
        verifyThat(resultsComparison.getResultsCountFor(AppearsIn.BOTH), is(innerCount));
        verifyThat(resultsComparison.getResultsCountFor(AppearsIn.OTHER_ONLY), is(outerCount - innerCount));
    }

    private int getTotalResults() {
        return getElementFactory().getResultsPage().getResultsCount();
    }

    @Test
    @ActiveBug("FIND-228")
    public void testCompareUnsavedSearches() {
        findService.search("\"not many results\"");
        savedSearchService.openNewTab();
        findService.search("\"to speed up comparison\"");

        ComparisonModal modal = findPage.openCompareModal();
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
    }

    @Test
    @ActiveBug("FIND-232")
    public void testAllSavedSearchesAreComparable() {
        searchAndSave(new Query("\"alice in wonderland\""), "alice", SearchType.QUERY);
        savedSearchService.openNewTab();
        searchAndSave(new Query("\"bob's burgers\""), "bob", SearchType.SNAPSHOT);
        searchAndSave(new Query("\"charlie's angels\""), "charlie", SearchType.QUERY);

        switchToTab("bob");
        final Collection<String> bobOptions = getOptionsFromModal();
        verifyThat(bobOptions, hasItems("alice", "charlie"));
        verifyThat(bobOptions, not(hasItem("bob")));

        switchToTab("alice");
        final Collection<String> aliceOptions = getOptionsFromModal();
        verifyThat(aliceOptions, hasItems("bob", "charlie"));
        verifyThat(aliceOptions, not(hasItem("alice")));
    }

    private List<String> getOptionsFromModal() {
        assertThat(findPage.compareButton(), not(disabled()));
        ComparisonModal modal = findPage.openCompareModal();
        List<String> options = modal.getItems();
        modal.close();
        return options;
    }

    private void searchAndSave(Query query, String saveAs) {
        searchAndSave(query, saveAs, SearchType.QUERY);
    }

    private void searchAndSave(Query query, String saveAs, SearchType saveType) {
        findService.search(query);
        savedSearchService.saveCurrentAs(saveAs, saveType);
    }

    private void switchToTab(String tabName) {
        getElementFactory().getSearchTabBar().switchTo(tabName);
    }
}
