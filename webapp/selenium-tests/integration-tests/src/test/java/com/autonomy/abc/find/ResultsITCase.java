/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
import com.autonomy.abc.selenium.find.CSVExportModal;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTagName;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

public class ResultsITCase extends FindTestBase {
    private FindPage findPage;
    private FindService findService;

    public ResultsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        findPage.goToListView();
    }

    @Test
    @ResolvedBug("CSA-1665")
    public void testSearchTermInResults() {
        final String searchTerm = "tiger";

        final ListView results = findService.search(searchTerm);

        for(final WebElement searchElement : results.resultsContainingString(searchTerm)) {
            if(searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText().toLowerCase(), containsString(searchTerm));
            }
            verifyThat(searchElement, not(hasTagName("a")));
        }
    }

    @Test
    @ResolvedBug("CSA-2082")
    public void testAutoScroll() {
        final ListView results = findService.search("nightmare");

        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.getResults(), hasSize(allOf(greaterThanOrEqualTo(30), lessThanOrEqualTo(60))));

        findPage.scrollToBottom();
        verifyThat(results.getResults(), hasSize(allOf(greaterThanOrEqualTo(60), lessThanOrEqualTo(90))));

        final List<String> references = new ArrayList<>();

        for(final FindResult result : results.getResults()) {
            references.add(result.getReference());
        }

        final Collection<String> referencesSet = new HashSet<>(references);

        /* References apparently may not be unique, but they're definitely ~more unique
                than titles within our data set  */
        verifyThat("No duplicate references", references, hasSize(referencesSet.size()));
    }

    @Test
    @ResolvedBug("CCUK-3647")
    public void testNoMoreResultsFoundAtEnd() {
        final ListView results = findService.search(new Query("Cheese AND Onion AND Carrot AND Coriander"));
        results.waitForResultsToLoad();

        verifyThat(results.getTotalResultsNum(), lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.resultsDiv(), containsText("No more results found"));
    }

    @Test
    @ResolvedBug("FIND-93")
    public void testNoResults() {
        final ListView results = findService.search("thissearchwillalmostcertainlyreturnnoresults");

        new WebDriverWait(getDriver(), 60L).withMessage("No results message should appear")
            .until(ExpectedConditions.textToBePresentInElement(results.resultsDiv(), "No results found"));

        findPage.scrollToBottom();

        final int occurrences = StringUtils.countMatches(results.resultsDiv().getText(), "results found");
        verifyThat("Only one message showing at the bottom of search results", occurrences, is(1));
    }

    @Test
    @ResolvedBug("FIND-350")
    @Role(UserRole.FIND)
    public void testDecliningAutoCorrectNotPermanent() {
        search("blarf");

        findPage.originalQuery().click();
        findPage.waitForParametricValuesToLoad();

        search("eevrywhere");
        verifyThat("Says it corrected query", findPage.originalQuery(), displayed());

        final ListView results = findPage.goToListView();
        verifyThat("There are results in list view", results.getTotalResultsNum(), greaterThan(0));
    }

    @Test
    @ResolvedBug("FIND-694")
    @Role(UserRole.FIND)
    public void testAutoCorrectedQueriesHaveRelatedConceptsAndParametrics() {
        final String termAutoCorrected = "everything";
        search(termAutoCorrected);

        LOGGER.info("Need to verify that " + termAutoCorrected + " has results, related concepts and parametrics");

        assumeThat(termAutoCorrected + " has some results", getElementFactory().getListView().getTotalResultsNum(), greaterThan(0));

        final int indexOfCategoryWFilters = getElementFactory().getFilterPanel().nonZeroParamFieldContainer(0);
        assertThat(termAutoCorrected + " has some parametric fields", indexOfCategoryWFilters, not(-1));
        assertThat(termAutoCorrected + " has related concepts", !getElementFactory().getRelatedConceptsPanel().noConceptsPresent());

        final String term = "eevrything";
        search(term);
        assertThat("Has autocorrected", findPage.hasAutoCorrected());
        assertThat("Has autocorrected " + term + " to " + termAutoCorrected,
                   findPage.getCorrectedQuery().toLowerCase(),
                   is("( " + termAutoCorrected + " )"));

        findPage.waitForParametricValuesToLoad();
        verifyThat("Still has parametric fields", getElementFactory().getFilterPanel().parametricField(indexOfCategoryWFilters).getFilterCount(), not(0));
        verifyThat("Still has related concepts", !getElementFactory().getRelatedConceptsPanel().noConceptsPresent());
    }

    @Test
    @ResolvedBug("FIND-719")
    @Role(UserRole.FIND)
    public void testNoResultsMessageHiddenAfterAutoCorrect() {
        final String term = "eevrything";

        search(term);
        final ListView results = findPage.goToListView();
        findPage.ensureTermNotAutoCorrected();
        results.waitForResultsToLoad();
        assertThat("Searching for " + term + " returns no results.", results.getTotalResultsNum(), is(0));

        //Search for a random thing to allow re-search of the term
        search("cat");

        search(term);
        results.waitForResultsToLoad();
        findPage.waitForParametricValuesToLoad();
        assumeThat(term + " has been auto-corrected to " + findPage.getCorrectedQuery() + " and this returns some results",
                   results.getTotalResultsNum(),
                   greaterThan(0));

        assertThat("\"No more results\" message not present.", !findPage.resultsMessagePresent());
    }

    @Test
    @Role(UserRole.FIND)
    public void testRefreshWithSlash() {
        final String query = "foo/bar";
        search(query);

        getDriver().navigate().refresh();

        findPage = getElementFactory().getFindPage();
        findPage.waitForLoad();

        // This could fail because %2F can be blocked by Tomcat
        assertThat(getElementFactory().getSearchBox().getValue(), is(query));
    }

    @Test
    @ResolvedBug("FIND-508")
    @Role(UserRole.BIFHI)
    public void testCanSelectParametricsThenExport() {
        final FilterPanel filters = getElementFactory().getFilterPanel();
        findPage.waitForParametricValuesToLoad();

        final int goodFilter = filters.nonZeroParamFieldContainer(0);
        filters.parametricField(goodFilter).getFilters().get(0).check();
        findPage.waitForParametricValuesToLoad();

        //TODO: part of the bad structure -> will be fixed w/ refactor of Roles vs App.
        ((BIIdolFindElementFactory)getElementFactory()).getSearchOptionsBar().exportResultsToCSV();

        final CSVExportModal modal = CSVExportModal.make(getDriver());
        assertThat("Modal has some contents", modal.fieldsToExport(), hasSize(greaterThan(0)));

        modal.close();
    }

    @Test
    @ResolvedBug("FIND-563")
    public void testQueryHighlightingForNonLatin() {
        //TODO investigate hod not enjoying searching for some of these terms -> also add data to all deployments
        search("*");

        final ConceptsPanel conceptsPanel = getElementFactory().getConceptsPanel();

        //Japanese: Human; Hebrew: Home; Thai: make; Russian: Russia; Arabic: white; Chinese: China
        final List<String> nonLatinQueries = Arrays.asList("人", "אדום", "ทำ", "Россия", "بيض", "中国");
        final String weightOfHighlightedTerm = "900";

        boolean foundResults = false;

        for(final String query : nonLatinQueries) {
            if(!foundResults) {
                search(query);
                findPage.ensureTermNotAutoCorrected();
                findPage.waitForParametricValuesToLoad();

                final ListView results = getElementFactory().getListView();
                if(results.getTotalResultsNum() > 0) {
                    foundResults = true;
                    final WebElement incidenceOfTerm = results.resultsContainingString(query).get(0);
                    assertThat("Term \"" + query + "\" is highlighted (bold).",
                               incidenceOfTerm.getCssValue("font-weight"),
                               is(weightOfHighlightedTerm));
                }

                conceptsPanel.removeAllConcepts();
            }
        }
        assertThat("Found some results for the non-Latin queries", foundResults);
    }

    @Test
    @Role(UserRole.BIFHI)
    @ActiveBug("FIND-703")
    public void testBIUserCannotRouteToSplashPage() {
        final String splashURL = getAppUrl() + "public/search/splash";
        getDriver().get(splashURL);
        Waits.loadOrFadeWait();

        findPage = getElementFactory().getFindPage();
        assertThat("Splash page logo not visible", findPage.footerLogo(), not(displayed()));
        assertThat("Has redirected away from Splash page", getDriver().getCurrentUrl(), not(splashURL));
    }

    private void search(final String term) {
        findService.search(term);
        findPage.waitForParametricValuesToLoad();
    }
}
