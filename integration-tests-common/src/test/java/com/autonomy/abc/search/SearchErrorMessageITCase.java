package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.shared.QueryTestHelper;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
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
        new QueryTestHelper<>(searchService).mismatchedBracketQueryText();
    }

    @Test
    @KnownBug({"IOD-8454", "CCUK-3741"})
    public void testSearchQuotationMarks() {
        @RelatedTo("CCUK-3747")
        Serializable error = isHosted() ?
                Errors.Search.INVALID : Errors.Search.QUOTES;
        new QueryTestHelper<>(searchService).mismatchedQuoteQueryText(error);
    }

    @Test
    @KnownBug("CCUK-3741")
    public void testQueriesWithNoTerms() {
        @RelatedTo("CCUK-3747")
        Serializable booleanError = isHosted() ?
                Errors.Search.INVALID : Errors.Search.OPENING_BOOL;
        Serializable emptyError = isHosted() ?
                Errors.Search.INVALID : Errors.Search.NO_TEXT;

        new QueryTestHelper<>(searchService).booleanOperatorQueryText(booleanError);
        new QueryTestHelper<>(searchService).emptyQueryText(emptyError);
    }

    @Test
    public void testQueryAnalysisForBadQueries() {
        for (final String term : QueryTestHelper.NO_TERMS) {
            Query query = new Query(term).withFilter(new LanguageFilter(Language.ENGLISH));
            String error = searchService.search(query).getKeywordError();
            assertThat(error, not(isEmptyOrNullString()));
            assertThat(error, containsString(Errors.Keywords.NO_TERMS));
        }
    }

    @Test
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        new QueryTestHelper<>(searchService).hiddenQueryOperatorText();
    }
}
