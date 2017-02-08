/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.queryHelper;

import com.autonomy.abc.selenium.error.Errors.Search;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.QueryTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.stringContainingAnyOf;

public class IdolQueryTestHelper<T> extends QueryTestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestHelper.class);

    public IdolQueryTestHelper(final QueryService queryService) {super(queryService);}

    public void hiddenQueryOperatorText(final FindElementFactory elementFactory) {
        for(final IdolQueryTermResult result : IdolQueryTermResult.idolResultsFor(getHiddenBooleans(), getService())) {
            if(result.errorWellExists() && result.errorContainer().isDisplayed()) {
                verifyThat("Query auto-corrected so sees the Boolean",
                           result.getErrorMessage(),
                           stringContainingAnyOf(Arrays.asList(Search.GENERAL_BOOLEAN)));
            } else {
                LOGGER.info("The error message is not displayed.");
            }
            elementFactory.getConceptsPanel().removeAllConcepts();
        }
    }
}
