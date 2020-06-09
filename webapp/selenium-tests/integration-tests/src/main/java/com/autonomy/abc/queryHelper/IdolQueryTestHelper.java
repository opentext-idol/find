/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.queryHelper;

import com.autonomy.abc.selenium.error.Errors.Search;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.QueryTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.stringContainingAnyOf;
import static org.hamcrest.Matchers.is;

public class IdolQueryTestHelper extends QueryTestHelper<ListView> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestHelper.class);

    public IdolQueryTestHelper(final QueryService<ListView> queryService) {
        super(queryService);
    }

    public void hiddenQueryOperatorText(final FindElementFactory elementFactory) {
        for (final IdolQueryTermResult result : IdolQueryTermResult.idolResultsFor(getHiddenBooleans(), getService())) {
            if (result.errorWellExists() && result.errorContainer().isDisplayed()) {
                verifyThat("Query auto-corrected so sees the Boolean",
                        result.getErrorMessage(),
                        stringContainingAnyOf(Arrays.asList(Search.GENERAL_BOOLEAN.toString(), Search.GENERAL_BOOLEAN.toString().toLowerCase())));
            } else {
                LOGGER.info("The error message is not displayed.");
            }
            elementFactory.getConceptsPanel().removeAllConcepts();
        }
    }

    public void hiddenQueryOperatorTextNoAutoCorrect(final FindElementFactory elementFactory) {
        for (final IdolQueryTermResult result : IdolQueryTermResult.idolResultsFor(getHiddenBooleans(), getService())) {
            verifyThat("No auto-correction", result.errorWellExists(), is(false));
            elementFactory.getConceptsPanel().removeAllConcepts();
        }
    }

    public void mismatchedQuoteQueryText(final FindElementFactory elementFactory, final Serializable... sensibleErrors) {
        validateResults(elementFactory, MISMATCHED_QUOTES, sensibleErrors);
    }

    public void booleanOperatorQueryText(final FindElementFactory elementFactory, final Serializable... sensibleErrors) {
        validateResults(elementFactory, OPERATORS, sensibleErrors);
    }

    public void emptyQueryText(final FindElementFactory elementFactory, final Serializable... sensibleErrors) {
        validateResults(elementFactory, NO_TERMS, sensibleErrors);
    }

    private void validateResults(final FindElementFactory elementFactory, final Iterable<String> terms, final Serializable... sensibleErrors) {
        for (final IdolQueryTermResult result : IdolQueryTermResult.idolResultsFor(terms, getService())) {
            validateError(result, sensibleErrors);
            elementFactory.getConceptsPanel().removeAllConcepts();
        }
    }
}
