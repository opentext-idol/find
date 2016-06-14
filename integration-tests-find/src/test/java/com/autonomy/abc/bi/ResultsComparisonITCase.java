package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

/**
 * Because comparisons are so slow, these rely on quite specific data
 * to ensure few results. You may need to tweak the searches to get
 * them working on your local machine
 */
public class ResultsComparisonITCase extends IdolFindTestBase {
    private FindService findService;
    private SavedSearchService savedSearchService;

    private ResultsComparisonView resultsComparison;

    public ResultsComparisonITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        savedSearchService = getApplication().savedSearchService();
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

        findService.search(polar);
        savedSearchService.saveCurrentAs("polar", SearchType.QUERY);
        savedSearchService.openNewTab();
        findService.search(opposites);
        savedSearchService.saveCurrentAs("opposites", SearchType.QUERY);
        savedSearchService.compareCurrentWith("polar");

        resultsComparison = getElementFactory().getResultsComparison();
        assertThat(resultsComparison.resultsCommonToBoth(), empty());
        assertThat(resultsComparison.commonToBoth(), containsText("No results found"));
    }

    @Test
    @ActiveBug("FIND-228")
    public void testCompareUnsavedSearches() {
        findService.search("\"not many results\"");
        savedSearchService.openNewTab();
        findService.search("\"to speed up comparison\"");

        ComparisonModal modal = getElementFactory().getFindPage().openCompareModal();
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
}
