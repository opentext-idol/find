package com.autonomy.abc.query;

import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import org.openqa.selenium.WebElement;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static com.autonomy.abc.matchers.StringMatchers.stringContainingAnyOf;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

public class QueryTestHelper<T extends QueryResultsPage> {
    private final static List<String> HIDDEN_BOOLEANS = Arrays.asList(
            "NOTed",
            "ANDREW",
            "ORder",
            "WHENCE",
            "SENTENCED",
            "PARAGRAPHING",
            "NEARLY",
            "SENTENCE1D",
            "PARAGRAPHING",
            "PARAGRAPH2inG",
            "SOUNDEXCLUSIVE",
            "XORING",
            "EORE",
            "DNEARLY",
            "WNEARING",
            "YNEARD",
            "AFTERWARDS",
            "BEFOREHAND",
            "NOTWHENERED"
    );
    private final static List<String> MISMATCHED_BRACKETS = Arrays.asList(
            "(",
            ")",
            "()",
            ") (",
            ")war"
    );
    private final static List<String> MISMATCHED_QUOTES = Arrays.asList(
            "\"",
            "\"word",
            "\" word",
            "\" wo\"rd\""
    );

    private final QueryService<T> service;

    public QueryTestHelper(QueryService<T> queryService) {
        service = queryService;
    }

    public void hiddenQueryOperatorText() {
        for (Result result : resultsFor(HIDDEN_BOOLEANS)) {
            assertThat("able to search for " + result.term, result.errorContainer(), not(displayed()));
        }
    }

    public void mismatchedBracketQueryText(final Serializable expectedError) {
        for (Result result : resultsFor(MISMATCHED_BRACKETS)) {
            assertThat("query term '" + result.term + "' is invalid",
                    result.getErrorMessage(), containsString(expectedError));
            assertThat("query term '" + result.term + "' has sensible error message",
                    result.getErrorMessage(), stringContainingAnyOf(Arrays.asList(
                            Errors.Search.INVALID,
                            Errors.Search.OPERATORS,
                            Errors.Search.STOPWORDS
                    ))
            );
        }
    }

    public void mismatchedQuoteQueryText(final Serializable expectedError) {
        // TODO: cover "", "\"\"" and " " in whitespace test
        for (Result result : resultsFor(MISMATCHED_QUOTES)) {
            assertThat("query term '" + result.term + "' produces an error", result.errorContainer(), displayed());
            verifyThat("query term '" + result.term + "' has sensible error message", result.getErrorMessage(), containsString(expectedError));
        }
    }

    private Iterable<Result> resultsFor(final Iterable<String> queries) {
        return new Iterable<Result>() {
            @Override
            public Iterator<Result> iterator() {
                final Iterator<String> queryIterator = queries.iterator();
                return new Iterator<Result>() {
                    @Override
                    public boolean hasNext() {
                        return queryIterator.hasNext();
                    }

                    @Override
                    public Result next() {
                        return resultFor(queryIterator.next());
                    }
                };
            }
        };
    }

    private Result resultFor(String queryTerm) {
        T page = service.search(queryTerm);
        return new Result(queryTerm, page);
    }
    
    private class Result {
        final String term;
        final T page;
        private String text;

        Result(String term, T page) {
            this.term = term;
            this.page = page;
        }

        String getErrorMessage() {
            if (text == null) {
                text = errorContainer().getText();
            }
            return text;
        }

        WebElement errorContainer() {
            return page.errorContainer();
        }
    }
}
