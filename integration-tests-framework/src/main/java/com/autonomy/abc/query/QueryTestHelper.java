package com.autonomy.abc.query;

import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.selenium.util.Waits;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;

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
            assertThat(page.getText(), errorMatcher);
        }
    }
}
