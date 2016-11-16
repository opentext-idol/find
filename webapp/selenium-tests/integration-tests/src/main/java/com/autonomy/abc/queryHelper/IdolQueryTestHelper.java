package com.autonomy.abc.queryHelper;

import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.QueryTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.stringContainingAnyOf;


public class IdolQueryTestHelper<T> extends QueryTestHelper{
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestHelper.class);

    public IdolQueryTestHelper(final QueryService queryService){super(queryService);}

    public void hiddenQueryOperatorText(final FindElementFactory elementFactory) {
        for (IdolQueryTermResult result : IdolQueryTermResult.idolResultsFor(getHiddenBooleans(), getService())) {
            if (result.errorWellExists() && result.errorContainer().isDisplayed()) {
                verifyThat("Query auto-corrected so sees the Boolean",
                        result.getErrorMessage(),
                        stringContainingAnyOf(Arrays.asList(Errors.Search.CLOSING_BOOL, Errors.Search.OPENING_BOOL)));
            }
            else {
                LOGGER.info("The error message is not displayed.");
            }
            elementFactory.getConceptsPanel().removeAllConcepts();
        }

    }
}
