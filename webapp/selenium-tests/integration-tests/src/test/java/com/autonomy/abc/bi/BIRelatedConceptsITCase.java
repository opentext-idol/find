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
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

@Role(UserRole.BIFHI)
public class BIRelatedConceptsITCase extends IdolFindTestBase {
    private BIIdolFindElementFactory elementFactory;
    private FindService findService;
    private IdolFindPage findPage;
    private TopicMapView topicMap;
    private ConceptsPanel conceptsPanel;

    public BIRelatedConceptsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        elementFactory = (BIIdolFindElementFactory)super.getElementFactory();
        findService = getApplication().findService();
        findPage = elementFactory.getFindPage();
        topicMap = elementFactory.getTopicMap();
        findPage.goToListView();
        conceptsPanel = elementFactory.getConceptsPanel();
    }

    private ConceptsPanel.EditPopover openEditPopOverForConcept(final int index, final String correctValue) {
        ConceptsPanel.EditPopover popOver = conceptsPanel.editConcept(index);

        assertThat("Edit box has opened", popOver, displayed());
        verifyThat("Popover contains value", popOver.containsValue(correctValue));

        return popOver;
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void testResultsCountGoesDownAfterAddingConcept() {
        final int numberOfRepeats = 2;
        final LinkedList<Integer> resultCountList = new LinkedList<>();

        final ResultsView results = searchAndWait("loathing");

        final int resultsCountNoConcept = results.getResultsCount();
        assumeThat("Initial query returned no results", resultsCountNoConcept, greaterThan(0));
        resultCountList.add(resultsCountNoConcept);

        for(int i = 0; i < numberOfRepeats; ++i) {
            goToTopicMap();
            topicMap.clickConceptAndAddText(topicMap.conceptClusterNames().size());
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

    @Test
    public void testEditingASingleConcept() {
        final String originalConcept = "balloon";
        final String editedConcept = "shiny";

        ResultsView results = searchAndWait(originalConcept);
        final String firstResult = results.getResult(1).getTitleString();

        goToTopicMap();
        final List<String> parentNames = topicMap.conceptClusterNames();

        ConceptsPanel.EditPopover popOver = openEditPopOverForConcept(0,originalConcept);

        popOver.setValue("blaaaaaaaaaaaaah");
        popOver.cancelEdit();
        verifyThat("Have not edited the concept", conceptsPanel.selectedConcepts().get(0), containsText(originalConcept));

        popOver = conceptsPanel.editConcept(0);
        popOver.setValue(editedConcept);
        popOver.saveEdit();
        verifyThat("Edit popover has closed", conceptsPanel.popOverGone());

        final WebElement addedConcept = conceptsPanel.selectedConcepts().get(0);
        verifyThat("Old concept not there", addedConcept, not(containsText(originalConcept)));
        verifyThat("Concept edited to new value", addedConcept, containsText(editedConcept));

        findPage.goToListView();
        final String editedFirstResult = elementFactory.getResultsPage().getResult(1).getTitleString();
        verifyThat("First result is different", editedFirstResult, not(firstResult));

        goToTopicMap();
        verifyThat("Topic map entities have changed", topicMap.conceptClusterNames(), not(parentNames));
    }
    
    @Test
    //Assumes that "nefarioustrout" returns no results
    public void testQuotesInConcept() {
        final String termA = "trout";
        final String termB = "nefarious";

        searchAndWait(termA);
        int numResults = findPage.totalResultsNum();
        conceptsPanel.removeAllConcepts();

        searchAndWait(termB);
        numResults = numResults + findPage.totalResultsNum();
        assertThat("There are some results when search terms are 'OR'ed", numResults, greaterThan(0));
        conceptsPanel.removeAllConcepts();

        final String originalConcept = "silly";
        searchAndWait(originalConcept);

        ConceptsPanel.EditPopover popOver = openEditPopOverForConcept(0, originalConcept);

        popOver.setValueAndSave(Arrays.asList("\""+termB, termA+"\""));

        getElementFactory().getResultsPage().waitForResultsToLoad();
        verifyThat("Converts the line break to a space and looks for an exact match", findPage.totalResultsNum(), is(0));

        popOver = conceptsPanel.editConcept(0);
        verifyThat("Line breaks replaced with spaces in edit box",!popOver.containsValue("\n"));
    }

    @Test
    public void testEditingConceptCluster() {
        searchAndWait("something");
        goToTopicMap();

        final int clusterIndex = 0;
        final List<String> childConcepts = topicMap.getChildConceptsOfCluster(clusterIndex);
        LOGGER.info("Child concepts: " + childConcepts);

        final String conceptCluster = topicMap.clickNthClusterHeading(clusterIndex);
        topicMap.waitForConcepts();

        ConceptsPanel.EditPopover popOver = openEditPopOverForConcept(1, conceptCluster);

        for (String child : childConcepts) {
            verifyThat("Pop-over contains child: " + child, popOver.containsValue(child));
        }

        final List<String> newConcepts = Arrays.asList("my fab","concepts","so fabulous");
        popOver.setValueAndSave(newConcepts);

        DriverUtil.hover(getDriver(),conceptsPanel.selectedConcepts().get(1));
        final String text = conceptsPanel.toolTipText(1);
        for(String concept : newConcepts) {
            verifyThat("Tool tip has added concept: " + concept, text, containsString(concept));
        }
    }

    @Test
    @ResolvedBug("FIND-686")
    public void testInlinePreviewClosesOnEdit() {
        final String originalSearch = "face";
        findPage.goToListView();
        final ResultsView results = findService.search(originalSearch);
        results.waitForResultsToLoad();

        InlinePreview inlinePreview = results.getResult(1).openDocumentPreview();
        ConceptsPanel.EditPopover popOver = openEditPopOverForConcept(0, originalSearch);

        popOver.setValue("blaaaaaaaaaaaaahljsfiejsfeisjtl");
        popOver.saveEdit();

        verifyThat("Document preview not still there", inlinePreview, not(displayed()));
    }

    private void goToTopicMap() {
        findPage.goToTopicMap();
        topicMap.waitForMapLoaded();
    }

    private ResultsView searchAndWait(final String query) {
        final ResultsView results = findService.search(query);
        results.waitForResultsToLoad();
        return results;
    }
}
