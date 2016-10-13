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
import com.autonomy.abc.selenium.find.bi.TopicMapConcept;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

@Role(UserRole.BIFHI)
public class BIRelatedConceptsITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;
    private TopicMapView topicMap;
    private BIIdolFindElementFactory elementFactory;

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

    @Test
    public void testEditingASingleConcept() {
        final String originalConcept = "balloon";
        final String editedConcept = "shiny";

        ResultsView results = searchAndWait(originalConcept);
        final String firstResult = results.getResult(1).getTitleString();

        goToTopicMap();
        final List<String> parentNames = topicMap.parentEntityNames();

        final ConceptsPanel conceptsPanel = getElementFactory().getConceptsPanel();

        ConceptsPanel.EditPopover popOver = conceptsPanel.editConcept(0);
        assertThat("Edit box has opened", popOver, displayed());
        verifyThat("Popover contains value", popOver.containsValue(originalConcept));

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

        verifyThat("Topic map entities have changed", topicMap.parentEntityNames(), not(parentNames));
    }
    
    @Test
    //Assumes that "nefarioustrout" returns no results
    public void testQuotesInConcept() {
        final String termA = "trout";
        final String termB = "nefarious";
        final ConceptsPanel conceptsPanel = getElementFactory().getConceptsPanel();
        conceptsPanel.removeAllConcepts();

        searchAndWait(termA);
        int numResults = findPage.totalResultsNum();
        conceptsPanel.removeAllConcepts();

        searchAndWait(termB);
        numResults = numResults + findPage.totalResultsNum();
        assertThat("There are some results when search terms are 'OR'ed", numResults, greaterThan(0));
        conceptsPanel.removeAllConcepts();

        final String originalConcept = "silly";
        searchAndWait(originalConcept);

        ConceptsPanel.EditPopover popOver = conceptsPanel.editConcept(0);

        assertThat("Edit box has opened", popOver, displayed());
        verifyThat("Popover contains value", popOver.containsValue(originalConcept));

        popOver.setValue(Arrays.asList("\""+termB, termA+"\""));
        popOver.saveEdit();

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

        TopicMapConcept clickedCluster = topicMap.clickNthCluster(clusterIndex);
        Double[][] boundariesOfChosenCluster = clickedCluster.getBoundaries();

        Point wholeMapTopLeft = topicMap.map().getLocation();

        List<MutablePair> desiredBaseEntities = new ArrayList<>();
        int index = 0;
        List<WebElement> totalBaseEntities = topicMap.baseLevelEntities();

        for(WebElement el: totalBaseEntities) {
            Dimension entitySize = el.getSize();

            Point absolutePosOfEntity = el.getLocation();

            int relativeX = absolutePosOfEntity.x - wholeMapTopLeft.x;
            int relativeY = absolutePosOfEntity.y - wholeMapTopLeft.y;
            int centreX = relativeX + entitySize.getWidth()/2;
            int centreY = relativeY + entitySize.getHeight()/2;

            if((boundariesOfChosenCluster[0][0] <= centreX && centreX <= boundariesOfChosenCluster[0][1])
                    && boundariesOfChosenCluster[1][0] <=centreY && centreY <= boundariesOfChosenCluster[1][1]) {
                desiredBaseEntities.add(new MutablePair(el,index));
            }

            index++;
        }

        List<String> childConcepts = new ArrayList<>();
        for(MutablePair path : desiredBaseEntities) {
            int indexOfText = totalBaseEntities.size() - 1 - (int) path.getRight();
            childConcepts.add(topicMap.mapEntityTextElements().get(indexOfText).getText());
        }

        LOGGER.info("Child concepts = " + childConcepts);
        final String conceptCluster = topicMap.clickNthClusterHeading(clusterIndex);

        topicMap.waitForBaseLevelEntities();
        final ConceptsPanel conceptsPanel = getElementFactory().getConceptsPanel();

        ConceptsPanel.EditPopover popOver = conceptsPanel.editConcept(1);

        assertThat("Edit box has opened", popOver, displayed());

        verifyThat("Pop-over contains value", popOver.containsValue(conceptCluster));
        for (String child : childConcepts) {
            verifyThat("Pop-over contains child: " + child, popOver.containsValue(child));
        }

        final List<String> newConcepts = Arrays.asList("my fab","concepts","so fabulous");
        popOver.setValue(newConcepts);
        popOver.saveEdit();

        DriverUtil.hover(getDriver(),conceptsPanel.selectedConcepts().get(1));
        final String text = conceptsPanel.toolTipText(1);

        for(String concept : newConcepts) {
            verifyThat("Tool tip has added concept: " + concept, text, containsString(concept));
        }
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
