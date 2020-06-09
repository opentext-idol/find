/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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
package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.NavBarSettings;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.find.results.RelatedConceptsPanel;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsTextIgnoringCase;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTextThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

@RelatedTo("CSA-2091")
@Role(UserRole.FIND)
public class RelatedConceptsITCase extends FindTestBase {
    private FindService findService;
    private ConceptsPanel conceptsPanel;
    private NavBarSettings navBar;

    public RelatedConceptsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        final FindElementFactory elementFactory = getElementFactory();
        findService = getApplication().findService();

        findService.search("Danye West");

        conceptsPanel = elementFactory.getConceptsPanel();
        navBar = elementFactory.getTopNavBar();
    }

    @Test
    public void testRelatedConceptsHasResults() {
        for(final WebElement concept : relatedConceptsPanel()) {
            assertThat(concept, hasTextThat(not(isEmptyOrNullString())));
            assertThat(concept, not(containsTextIgnoringCase("loading")));
        }
    }

    @Test
    @RelatedTo("CCUK-3598")
    public void testRelatedConceptsNavigateOnClick() {
        final String query = "Red";
        searchAndWait(findService, query);
        final WebElement topRelatedConcept = relatedConceptsPanel().concept(0);
        final String concept = topRelatedConcept.getText();

        topRelatedConcept.click();
        assertThat(conceptsPanel.selectedConceptHeaders(), hasItem(equalToIgnoringCase('"' + concept + '"')));
        assertThat(getElementFactory().getSearchBox().getValue(), is(query));
    }

    @Test
    @ResolvedBug({"CCUK-3498", "CSA-2066", "FIND-666"})
    public void testRelatedConceptsHover() {
        searchAndWait(findService, "Find");
        final String popover = relatedConceptsPanel().hoverOverRelatedConcept(0).getText();
        verifyThat(popover, not(isEmptyOrNullString()));
        verifyThat(popover, not(containsString("QueryText-Placeholder")));
        verifyThat(popover, not(containsString(Errors.Search.RELATED_CONCEPTS)));
    }

    @Test
    @ResolvedBug("FIND-666")
    public void testMultipleAdditionalConcepts() {
        searchAndWait(findService, "bongo");

        final Collection<String> relatedConcepts = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            final List<WebElement> newRelatedConcepts = relatedConceptsPanel().relatedConcepts();
            if(!newRelatedConcepts.isEmpty()) {
                final String newConcept = clickFirstNewConcept(relatedConcepts, newRelatedConcepts);
                verifyThat(conceptsPanel.selectedConceptHeaders(), hasItem(equalToIgnoringCase('"' + newConcept + '"')));
            }
        }

        verifyThat(getElementFactory().getSearchBox().getValue(), is("bongo"));

        final List<String> selectedConceptHeaders = conceptsPanel.selectedConceptHeaders().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        verifyThat(selectedConceptHeaders, hasSize(relatedConcepts.size()));
        verifyThat(selectedConceptHeaders, containsItems(relatedConcepts.stream().map(s -> '"' + s + '"').collect(Collectors.toList())));
    }

    @Test
    @Category(CoreFeature.class)
    @ResolvedBug("FIND-666")
    @ActiveBug("FIND-665 - Still relevant on hosted as of 09/12/16, WON'T BE CAUGHT BY TEST") //TODO write new test
    public void testAddRemoveConcepts() {
        final String queryTerm = "general";
        searchAndWait(findService, queryTerm);

        final Collection<String> concepts = new ArrayList<>();
        final String firstConcept = clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());
        final String secondConcept = clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());

        verifyThat(conceptsPanel.selectedConcepts(), hasSize(2));

        conceptsPanel.removableConceptForHeader(secondConcept).removeAndWait();

        final List<String> moreConcepts = conceptsPanel.selectedConceptHeaders();

        verifyThat(moreConcepts, hasSize(1));
        verifyThat(moreConcepts, not(hasItem(equalToIgnoringCase('"' + secondConcept + '"'))));
        verifyThat(moreConcepts, hasItem(equalToIgnoringCase('"' + firstConcept + '"')));
        verifyThat(getElementFactory().getSearchBox().getValue(), is(queryTerm));
    }

    @Test
    @ResolvedBug({"CCUK-3566", "FIND-109"})
    @ActiveBug("FIND-495")
    public void testTermNotInRelatedConcepts() {
        final String query = "world cup";
        searchAndWait(findService, query);
        final RelatedConceptsPanel panel = relatedConceptsPanel();

        verifyThat(panel.getRelatedConcepts(), not(hasItem(equalToIgnoringCase('"' + query + '"'))));

        final Collection<String> addedConcepts = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            clickFirstNewConcept(addedConcepts, relatedConceptsPanel().relatedConcepts());
            verifyThat(panel.getRelatedConcepts(), not(hasItem(equalToIgnoringCase('"' + query + '"'))));
        }
    }

    @Test
    @ResolvedBug({"CCUK-3566", "FIND-666"})
    @ActiveBug("FIND-854")
    public void testAdditionalConceptsNotAlsoRelated() {
        searchAndWait(findService, "matt");
        final Collection<String> addedConcepts = new ArrayList<>();

        final int limit = 5;
        int i = 0;
        while (i < limit && !relatedConceptsPanel().noConceptsPresent()) {
            clickFirstNewConcept(addedConcepts, relatedConceptsPanel().relatedConcepts());
            final List<String> relatedConcepts = relatedConceptsPanel().getRelatedConcepts();

            for (final String addedConcept : addedConcepts) {
                verifyThat(relatedConcepts, not(hasItem(equalToIgnoringCase('"' + addedConcept + '"'))));
            }
            i++;
        }
    }

    @Test
    @RelatedTo({"FIND-243", "FIND-110"})
    @ResolvedBug("FIND-666")
    @Ignore("Test will never currently pass due to lack of routing/push-state")
    public void testRefreshAddedConcepts() {
        searchAndWait(findService, "fresh");

        final Collection<String> concepts = new ArrayList<>();
        clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());
        getElementFactory().getListView().waitForResultsToLoad();
        clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());
        getDriver().navigate().refresh();
        navBar = getElementFactory().getTopNavBar();

        verifyThat(getElementFactory().getSearchBox().getValue(), is("fresh"));
        verifyThat(conceptsPanel.selectedConceptHeaders(), containsItems(concepts));
    }

    @Test
    @ResolvedBug({"FIND-308","FIND-666"})
    public void testRelatedConceptsHoverNoExtraScrollBar() {
        searchAndWait(findService, "orange");
        // Bug not observed if few related concepts
        if(relatedConceptsPanel().relatedConceptsClusters().size() >= 2) {
            final List<WebElement> clusterMembers = relatedConceptsPanel().membersOfCluster(1);
            final int lastConcept = clusterMembers.size() - 1;
            relatedConceptsPanel().hoverOverRelatedConcept(clusterMembers.get(lastConcept));
            verifyThat("No vertical scroll bar", !getElementFactory().getFindPage().verticalScrollBarPresent());
        } else {
            LOGGER.warn("There were too few concept clusters to carry out this test - bug would not occur");
        }
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void testResultsCountGoesDownAfterAddingConcept() {
        final int numberOfRepeats = 2;
        final LinkedList<Integer> resultCountList = new LinkedList<>();
        final Collection<String> concepts = new ArrayList<>();

        final ListView results = searchAndWait(findService, "sanctimonious");

        final int resultsCountNoConcept = results.getTotalResultsNum();
        assumeThat("Initial query returned no results", resultsCountNoConcept, greaterThan(0));
        resultCountList.add(resultsCountNoConcept);

        for(int i = 0; i < numberOfRepeats; ++i) {
            clickFirstNewConcept(concepts, conceptsPanel().relatedConcepts());
            results.waitForResultsToLoad();
            resultCountList.add(results.getTotalResultsNum());
        }

        for(int i = 0; i < resultCountList.size() - 1; ++i) {
            LOGGER.info("Search no. " + (i + 1) + " yielded " + resultCountList.get(i) + " results.");
            assertThat("Adding a concept does not increase the result count",
                       resultCountList.get(i),
                       greaterThanOrEqualTo(resultCountList.get(i + 1)));
        }
    }

    private RelatedConceptsPanel conceptsPanel() {
        return getElementFactory().getRelatedConceptsPanel();
    }

    private RelatedConceptsPanel relatedConceptsPanel() {
        return getElementFactory().getRelatedConceptsPanel();
    }

    private String clickFirstNewConcept(final Collection<String> existingConcepts,
                                        final Iterable<WebElement> relatedConcepts) {
        for(final WebElement concept : relatedConcepts) {
            final String conceptText = concept.getText();
            if(!existingConcepts.contains(conceptText)) {
                LOGGER.info("Clicking concept " + conceptText);
                concept.click();

                existingConcepts.add(conceptText.toLowerCase());

                return conceptText;
            }
        }

        throw new NoSuchElementException("no new related concepts");
    }

    private ListView searchAndWait(final FindService findService, final String query) {
        final ListView results = findService.search(query);
        results.waitForResultsToLoad();
        return results;
    }
}
