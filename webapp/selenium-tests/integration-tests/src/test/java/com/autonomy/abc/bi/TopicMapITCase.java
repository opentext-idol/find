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

package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.FindParametricFilter;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.RangeInput;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

@Role(UserRole.BIFHI)
public class TopicMapITCase extends IdolFindTestBase {
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"", Pattern.LITERAL);
    private IdolFindPage findPage;
    private TopicMapView results;
    private FindService findService;
    private ConceptsPanel conceptsPanel;

    public TopicMapITCase(final TestConfig config) {
        super(config);
    }

    @Override
    public BIIdolFindElementFactory getElementFactory() {
        return (BIIdolFindElementFactory)super.getElementFactory();
    }

    @Before
    public void setUp() {
        final BIIdolFindElementFactory elementFactory = getElementFactory();
        findPage = elementFactory.getFindPage();
        results = elementFactory.getTopicMap();
        findService = getApplication().findService();
        conceptsPanel = elementFactory.getConceptsPanel();
    }

    @Test
    public void testTopicMapTabShowsTopicMap() {
        findService.searchAnyView("m");
        verifyThat("Topic map element displayed",
                   results.topicMapVisible());
    }

    @Test
    public void testNumbersForMapInSliders() {
        final RangeInput slider = results.speedVsAccuracySlider();
        final int firstNumber = sliderToolTipValue(slider);

        search("dog");

        slider.dragBy(10);
        slider.hover();

        assertThat("Tooltip reappears after dragging",
                   slider.tooltip().isDisplayed());
        verifyThat("New tooltip value bigger than old",
                   sliderToolTipValue(slider),
                   greaterThanOrEqualTo(firstNumber));
    }

    @Test
    public void testEveryMapEntityHasText() {
        search("trouble");

        results.speedVsAccuracySlider().dragBy(10);
        Waits.loadOrFadeWait();
        results.waitForMapLoaded();

        results.speedVsAccuracySlider().hover();
        final int numberEntities = results.numberOfMapEntities();

        final List<String> textElements = results.mapEntityText();
        verifyThat("Same number of text elements as map pieces", textElements.size(), is(numberEntities));

        results.waitForMapLoaded();
        for(final String textElement : textElements) {
            verifyThat("Text element not empty",
                       textElement,
                       not(""));
        }
    }

    @Test
    public void testApplyingFiltersToMap() {
        final String searchTerm = "European Union";
        findService.searchAnyView(searchTerm);

        //checks first parametric filter of first parametric filter type
        final FilterPanel filters = getElementFactory().getFilterPanel();
        final int index = filters.nonZeroParamFieldContainer(0);

        final FindParametricFilter filter = filters.checkboxForParametricValue(index, 0);
        final String filterName = filter.getName();
        filter.check();

        results.waitForMapLoaded();
        verifyThat("The correct filter label has appeared",
                   findPage.filterLabelsText(),
                   hasItem(containsString(filterName)));
    }

    @Test
    public void testClickingOnMapEntities() {
        search("m");

        assumeThat("Search has results for this data", results.emptyMessage(), not(displayed()));
        final List<String> clusterNames = results.conceptClusterNames();
        final Collection<String> addedConcepts = new ArrayList<>();

        addedConcepts.add(results.clickConceptAndAddText(clusterNames.size()));
        results.waitForMapLoaded();

        addedConcepts.add(results.clickConceptAndAddText(results.conceptClusterNames().size()));
        Waits.loadOrFadeWait();

        final List<String> selectedConcepts = selectedConcepts();
        for(final String concept : addedConcepts) {
            verifyThat("Concept " + concept + " was added to the Concepts Panel",
                       selectedConcepts,
                       hasItem(concept));
        }
    }

    @Test
    public void testDraggingSliderLoadsNewResults() {
        search("thing");
        final RangeInput slider = results.speedVsAccuracySlider();
        final List<String> originalParentEntityNames = results.conceptClusterNames();

        slider.dragBy(50);
        results.waitForMapLoaded();
        assertThat("Changing the slider has changed the map",
                   results.conceptClusterNames(),
                   not(allOf(hasSize(originalParentEntityNames.size()),
                             containsItems(originalParentEntityNames))));
    }

    private int sliderToolTipValue(final RangeInput slider) {
        slider.hover();
        new WebDriverWait(getDriver(), 5)
                .until(ExpectedConditions.visibilityOf(slider.tooltip()));
        verifyThat("Tooltip appears on hover", slider.tooltip().isDisplayed());
        return slider.getTooltipValue();
    }

    @Test
    @ResolvedBug("FIND-650")
    public void testTopicMapRendersWhenManyNewTabs() {
        search("grey");

        final SearchTabBar tabBar = getElementFactory().getSearchTabBar();
        final int numberTabs = 8;
        for(int i = 0; i < numberTabs; i++) {
            tabBar.newTab();
        }

        tabBar.switchTo(numberTabs / 2);
        results.waitForMapLoaded();

        for(int j = 0; j < numberTabs; j++) {
            tabBar.switchTo(j);
            results = getElementFactory().getTopicMap();
            results.waitForMapLoaded();
            verifyThat("Map has appeared on tab " + (j + 1),
                       results.mapEntities(),
                       hasSize(greaterThan(0)));
        }
    }

    private void search(final String term) {
        findService.searchAnyView(term);
        results.waitForMapLoaded();
    }

    private String removeQuotes(final CharSequence term) {
        return QUOTE_PATTERN.matcher(term).replaceAll("");
    }

    private List<String> selectedConcepts() {
        return conceptsPanel.selectedConceptHeaders().stream()
                .map(this::removeQuotes)
                .collect(Collectors.toList());
    }
}
