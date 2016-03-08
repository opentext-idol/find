package com.autonomy.abc.query;

import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import org.hamcrest.Matcher;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static com.autonomy.abc.matchers.StringMatchers.stringContainingAnyOf;

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

    private final QueryService<T> service;

    public QueryTestHelper(QueryService<T> queryService) {
        service = queryService;
    }

    public void hiddenQueryOperatorText(final Matcher<? super String> errorMatcher) {
        for (Result result : resultsFor(HIDDEN_BOOLEANS)) {
            assertThat("able to search for " + result.term, result.getText(), errorMatcher);

        }
    }

    public void mismatchedBracketQueryText(final Serializable invalidator) {
        List<String> queryTerms = Arrays.asList(
                "(",
                ")",
                "()",
                ") (",
                ")war"
        );

        for (Result result : resultsFor(queryTerms)) {
            assertThat("query term " + result.term + " is invalid",
                    result.getText(), containsString(invalidator));
            assertThat("query term " + result.term + " has sensible error message",
                    result.getText(), stringContainingAnyOf(Arrays.asList(
                            Errors.Search.INVALID,
                            Errors.Search.OPERATORS,
                            Errors.Search.STOPWORDS
                    ))
            );
        }
    }

    public void mismatchedQuoteQueryText(final Serializable invalidator) {
        // TODO: cover "", "\"\"" and " " in whitespace test
        List<String> queryTerms = Arrays.asList(
                "\"",
                "\"word",
                "\" word",
                "\" wo\"rd\""
        );
        for (Result result : resultsFor(queryTerms)) {
            assertThat(result.getText(), containsString(Errors.Search.QUOTES));
            assertThat("query term " + result.term + " has sensible error message", result.getText(), containsString(invalidator));
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
                        final String queryTerm = queryIterator.next();
                        T page = service.search(queryTerm);
                        return new Result(queryTerm, page);
                    }
                };
            }
        };
    }
    
    private class Result {
        final String term;
        final T page;
        private String text;

        Result(String term, T page) {
            this.term = term;
            this.page = page;
        }

        String getText() {
            if (text == null) {
                text = page.getText();
            }
            return text;
        }
    }
}
