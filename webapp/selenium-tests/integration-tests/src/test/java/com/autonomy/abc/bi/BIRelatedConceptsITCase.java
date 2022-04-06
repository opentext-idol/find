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

package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel.EditPopover;
import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.find.results.ListView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasAttribute;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

@Role(UserRole.BIFHI)
public class BIRelatedConceptsITCase extends IdolFindTestBase {
    private BIIdolFindElementFactory elementFactory;
    private FindService findService;
    private IdolFindPage findPage;
    private ConceptsPanel conceptsPanel;

    public BIRelatedConceptsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        elementFactory = (BIIdolFindElementFactory) getElementFactory();
        findService = getApplication().findService();
        findPage = elementFactory.getFindPage();
        findPage.goToListView();
        conceptsPanel = elementFactory.getConceptsPanel();
    }

    private EditPopover openEditPopOverForConcept(final int index, final String correctValue) {
        final EditPopover popOver = conceptsPanel.editConcept(index);

        assertThat("Edit box has opened", popOver, displayed());
        verifyThat("Popover contains value", popOver.containsValue(correctValue));

        return popOver;
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void testResultsCountGoesDownAfterAddingConcept() {

        final ListView results = searchAndWait("loathing");

        final int resultsCountNoConcept = results.getTotalResultsNum();
        assumeThat("Initial query returned no results", resultsCountNoConcept, greaterThan(0));
        final AbstractSequentialList<Integer> resultCountList = new LinkedList<>();
        resultCountList.add(resultsCountNoConcept);

        final int numberOfRepeats = 2;
        for (int i = 0; i < numberOfRepeats; ++i) {
            final TopicMapView topicMap = goToTopicMap();
            topicMap.clickNthClusterHeading(0);
            Waits.loadOrFadeWait();

            findPage.goToListView();
            results.waitForResultsToLoad();

            resultCountList.add(results.getTotalResultsNum());
        }

        for (int i = 0; i < resultCountList.size() - 1; ++i) {
            LOGGER.info("Search no. " + (i + 1) + " yielded " + resultCountList.get(i) + " results.");
            assertThat("Adding a concept does not increase the result count",
                    resultCountList.get(i),
                    greaterThanOrEqualTo(resultCountList.get(i + 1)));
        }
    }

    @Test
    public void editConceptToWhitespaceNotAllowed() {
        final String concept = "cheese";
        searchAndWait(concept);
        final EditPopover popover = openEditPopOverForConcept(0, concept);
        popover.setValue("");
        verifyThat("Not possible to save concept as empty space", popover.saveButton(), hasAttribute("disabled"));
        popover.setValue("\n     ");
        verifyThat("Not possible to save concept as whitespace", popover.saveButton(), hasAttribute("disabled"));
        popover.cancelEdit();
    }

    @Test
    public void testEditingASingleConcept() {
        final String originalConcept = "balloon";

        final ListView results = searchAndWait(originalConcept);
        final String firstResult = results.getResult(1).getTitleString();

        final TopicMapView topicMap = goToTopicMap();
        final List<String> parentNames = topicMap.conceptClusterNames();

        EditPopover popOver = openEditPopOverForConcept(0, originalConcept);

        popOver.setValue("blaaaaaaaaaaaaah");
        popOver.cancelEdit();
        verifyThat("Have not edited the concept", conceptsPanel.selectedConcepts().get(0), containsText(originalConcept));

        popOver = conceptsPanel.editConcept(0);
        final String editedConcept = "shiny";
        popOver.setValue(editedConcept);
        popOver.saveEdit();
        verifyThat("Edit popover has closed", conceptsPanel.popOverGone());

        final WebElement addedConcept = conceptsPanel.selectedConcepts().get(0);
        verifyThat("Old concept not there", addedConcept, not(containsText(originalConcept)));
        verifyThat("Concept edited to new value", addedConcept, containsText(editedConcept));

        findPage.goToListView();
        final String editedFirstResult = elementFactory.getListView().getResult(1).getTitleString();
        verifyThat("First result is different", editedFirstResult, not(firstResult));

        goToTopicMap();
        verifyThat("Topic map entities have changed", topicMap.conceptClusterNames(), not(parentNames));
    }

    @Test
    //Assumes that "nefarioustrout" returns no results
    public void testQuotesInConcept() {
        final String termA = "trout";

        searchAndWait(termA);
        final ListView results = elementFactory.getListView();
        int numResults = results.getTotalResultsNum();
        conceptsPanel.removeAllConcepts();

        final String termB = "nefarious";
        searchAndWait(termB);
        numResults = numResults + results.getTotalResultsNum();
        assertThat("There are some results when search terms are 'OR'ed", numResults, greaterThan(0));
        conceptsPanel.removeAllConcepts();

        final String originalConcept = "silly";
        searchAndWait(originalConcept);

        EditPopover popOver = openEditPopOverForConcept(0, originalConcept);

        popOver.setValueAndSave(Arrays.asList('"' + termB, termA + '"'));

        results.waitForResultsToLoad();
        verifyThat("Converts the line break to a space and looks for an exact match", results.getTotalResultsNum(), is(0));

        popOver = conceptsPanel.editConcept(0);
        verifyThat("Line breaks replaced with spaces in edit box", !popOver.containsValue("\n"));
    }

    @Test
    public void testEditingConceptCluster() {
        searchAndWait("something");
        final TopicMapView topicMap = goToTopicMap();

        final int clusterIndex = 0;
        final List<String> childConcepts = topicMap.getChildConceptsOfCluster(clusterIndex);
        LOGGER.info("Child concepts: " + childConcepts);

        final String conceptCluster = topicMap.clickNthClusterHeading(clusterIndex);
        topicMap.waitForConcepts();

        final EditPopover popOver = openEditPopOverForConcept(1, conceptCluster);

        for (final String child : childConcepts) {
            verifyThat("Pop-over contains child: " + child, popOver.containsValue(child));
        }

        final List<String> newConcepts = Arrays.asList("my fab", "concepts", "so fabulous");
        popOver.setValueAndSave(newConcepts);

        DriverUtil.hover(getDriver(), conceptsPanel.selectedConcepts().get(1));
        final String text = conceptsPanel.toolTipText(1);
        for (final String concept : newConcepts) {
            verifyThat("Tool tip has added concept: " + concept, text, containsString(concept));
        }
    }

    @Test
    @ResolvedBug("FIND-686")
    public void testInlinePreviewClosesOnEdit() {
        findPage.goToListView();
        final String originalSearch = "face";
        final ListView results = findService.search(originalSearch);
        results.waitForResultsToLoad();

        final InlinePreview inlinePreview = results.getResult(1).openDocumentPreview();
        final EditPopover popOver = openEditPopOverForConcept(0, originalSearch);

        popOver.setValue("blaaaaaaaaaaaaahljsfiejsfeisjtl");
        popOver.saveEdit();

        verifyThat("Document preview not still there", inlinePreview, not(displayed()));
    }

    private TopicMapView goToTopicMap() {
        final TopicMapView map = findPage.goToTopicMap();
        map.waitForMapLoaded();
        return map;
    }

    private ListView searchAndWait(final String query) {
        final ListView results = findService.search(query);
        results.waitForResultsToLoad();
        return results;
    }
}
