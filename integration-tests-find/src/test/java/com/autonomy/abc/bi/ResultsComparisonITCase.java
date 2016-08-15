package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;

//HAS THE RESULTS COMPARISON TESTS FOR NON-LIST VIEWS
public class ResultsComparisonITCase extends IdolFindTestBase{

    private FindService findService;
    private SavedSearchService savedSearchService;
    private IdolFindElementFactory elementFactory;

    private ResultsComparisonView resultsComparison;
    private IdolFindPage findPage;

    public ResultsComparisonITCase(final TestConfig config) {
        super(config);
    }
}
