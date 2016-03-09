package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.query.QueryTestHelper;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.net.MalformedURLException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class SearchErrorMessageITCase extends ABCTestBase {
    private SearchService searchService;

    public SearchErrorMessageITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() throws MalformedURLException {
        searchService = getApplication().searchService();
    }

    @Test
    @KnownBug("CCUK-3741")
    public void testSearchParentheses() {
        Serializable errorMessage;
        if (getConfig().getType() == ApplicationType.HOSTED) {
            errorMessage = Errors.Search.HOD;
        } else {
            errorMessage = Errors.Search.GENERAL;
        }
        new QueryTestHelper<>(searchService).mismatchedBracketQueryText(errorMessage);
    }

    @Test
    @KnownBug({"IOD-8454", "CCUK-3741"})
    public void testSearchQuotationMarks() {
        new QueryTestHelper<>(searchService).mismatchedQuoteQueryText(Errors.Search.QUOTES);
    }


    @Test
    @RelatedTo("CCUK-3747")
    public void testQueriesWithNoTerms() {
        Serializable booleanError;
        Serializable emptyError;
        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            booleanError = Errors.Search.INVALID;
            emptyError = Errors.Search.INVALID;
        } else {
            booleanError = Errors.Search.OPENING_BOOL;
            emptyError = Errors.Search.NO_TEXT;
        }

        new QueryTestHelper<>(searchService).booleanOperatorQueryText(booleanError);
        new QueryTestHelper<>(searchService).emptyQueryText(emptyError);
    }

    @Test
    public void testQueryAnalysisForBadQueries() {
        for (final String term : QueryTestHelper.NO_TERMS) {
            String error = searchService.search(term).getKeywordError();
            assertThat(error, not(isEmptyOrNullString()));
            assertThat(error, containsString(Errors.Keywords.NO_TERMS));
        }
    }

    @Test
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        new QueryTestHelper<>(searchService).hiddenQueryOperatorText();
    }
}
