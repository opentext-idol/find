package com.autonomy.abc.query;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.selenium.util.Waits;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static com.autonomy.abc.matchers.StringMatchers.stringContainingAnyOf;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.fail;

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
        for (final String hiddenBooleansProximity : HIDDEN_BOOLEANS) {
            T page = service.search(hiddenBooleansProximity);
            Waits.loadOrFadeWait();
            assertThat("able to search for " + hiddenBooleansProximity, page.getText(), errorMatcher);
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

        for (String searchTerm : queryTerms) {
            String text = service.search(searchTerm).getText();
            assertThat("query term " + searchTerm + " is invalid",
                    text, containsString(invalidator));
            assertThat("query term " + searchTerm + " has sensible error message",
                    text, stringContainingAnyOf(Arrays.asList(
                            Errors.Search.INVALID,
                            Errors.Search.OPERATORS,
                            Errors.Search.STOPWORDS
                    ))
            );
        }
    }
}
