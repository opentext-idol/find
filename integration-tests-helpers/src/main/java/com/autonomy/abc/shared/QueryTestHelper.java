package com.autonomy.abc.shared;

import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.stringContainingAnyOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

public class QueryTestHelper<T extends QueryResultsPage>{

    public static final List<String> NO_TERMS = Arrays.asList(
            "a",
            "the",
            "of",
            "\"\"",
            "\"       \"",
            /* According to IDOL team SOUNDEX isn't considered a boolean
               operator without brackets */
            "SOUNDEX"
    );
    private static final List<String> HIDDEN_BOOLEANS = Arrays.asList(
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
    private static final List<String> MISMATCHED_BRACKETS = Arrays.asList(
            "(",
            ")",
            // TODO: these are not "mismatched" brackets in the same sense, but should probably be tested somewhere
//            "()",
//            ") (",
            ")war"
    );
    private static final List<String> MISMATCHED_QUOTES = Arrays.asList(
            "\"",
            "\"word",
            "\" word",
            "\" wo\"rd\""
    );
    private static final List<String> OPERATORS = Arrays.asList(
            "OR",
            "WHEN",
            "SENTENCE",
            "DNEAR"
    );

    private final QueryService<T> service;

    public QueryTestHelper(final QueryService<T> queryService) {
        service = queryService;
    }

    public void hiddenQueryOperatorText() {
        for (final SharedResult result : SharedResult.resultsFor(HIDDEN_BOOLEANS,service)) {
            verifyThat("able to search for " + result.term, result.errorContainer(), anyOf(
                    not(displayed()),
                    containsText(Errors.Search.NO_RESULTS))
            );
        }
    }

    public void mismatchedBracketQueryText() {
        for (final SharedResult result : SharedResult.resultsFor(MISMATCHED_BRACKETS,service)) {
            verifyThat("query term '" + result.term + "' is invalid",
                    result.errorContainer(), displayed());
            verifyThat("query term '" + result.term + "' has sensible error message",
                    result.getErrorMessage(), stringContainingAnyOf(Arrays.asList(
                            Errors.Search.INVALID,
                            Errors.Search.OPERATORS,
                            Errors.Search.BRACKETS,
                            Errors.Search.STOPWORDS,
                            Errors.Search.BRACKETS_BOOLEAN_OPEN,
                            Errors.Search.BRACKETS_BOOLEAN_CLOSE,
                            Errors.Search.GENERIC_HOSTED_ERROR
                    ))
            );
        }
    }

    public void mismatchedQuoteQueryText(final Serializable... sensibleErrors) {
        checkQueries(MISMATCHED_QUOTES, sensibleErrors);
    }

    public void booleanOperatorQueryText(final Serializable... sensibleErrors) {
        checkQueries(OPERATORS, sensibleErrors);
    }

    public void emptyQueryText(final Serializable... sensibleErrors) {
        checkQueries(NO_TERMS, sensibleErrors);
    }

    public static final List<String> getHiddenBooleans(){
        return HIDDEN_BOOLEANS;
    }

    public QueryService<T> getService(){return service;}

    private void checkQueries(final List<String> terms, final Serializable... sensibleErrors) {
        for (final SharedResult result : SharedResult.resultsFor(terms,service)) {
            assertThat("query term '" + result.term + "' produces an error", result.errorContainer(), displayed());
            verifyThat("query term '" + result.term + "' has sensible error message", result.getErrorMessage(), stringContainingAnyOf(sensibleErrors));
        }
    }
    
}
