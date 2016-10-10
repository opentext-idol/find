/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@Role(UserRole.BIFHI)
public class BIRelatedConceptsITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;
    private TopicMapView topicMap;

    public BIRelatedConceptsITCase(final TestConfig config) {
        super(config);
    }

    @Override
    public BIIdolFindElementFactory getElementFactory() {
        return (BIIdolFindElementFactory)super.getElementFactory();
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        findPage = getElementFactory().getFindPage();
        topicMap = getElementFactory().getTopicMap();
        findPage.goToListView();
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void testResultsCountGoesDownAfterAddingConcept() {
        final int numberOfRepeats = 2;
        final LinkedList<Integer> resultCountList = new LinkedList<>();

        final ResultsView results = searchAndWait(findService, "loathing");

        final int resultsCountNoConcept = results.getResultsCount();
        assumeThat("Initial query returned no results", resultsCountNoConcept, greaterThan(0));
        resultCountList.add(resultsCountNoConcept);

        for(int i = 0; i < numberOfRepeats; ++i) {
            findPage.goToTopicMap();
            topicMap.waitForMapLoaded();

            topicMap.clickChildEntityAndAddText(topicMap.parentEntityNames().size());
            Waits.loadOrFadeWait();

            findPage.goToListView();
            results.waitForResultsToLoad();

            resultCountList.add(results.getResultsCount());
        }

        for(int i = 0; i < resultCountList.size() - 1; ++i) {
            LOGGER.info("Search no. " + (i + 1) + " yielded " + resultCountList.get(i) + " results.");
            assertThat("Adding a concept does not increase the result count",
                       resultCountList.get(i),
                       greaterThanOrEqualTo(resultCountList.get(i + 1)));
        }
    }

    private ResultsView searchAndWait(final FindService findService, final String query) {
        final ResultsView results = findService.search(query);
        results.waitForResultsToLoad();
        return results;
    }
}
