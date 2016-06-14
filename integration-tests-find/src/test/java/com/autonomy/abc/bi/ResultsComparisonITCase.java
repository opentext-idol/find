package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.empty;

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
        assertThat(resultsComparison.commonToBoth(), empty());
    }
}
