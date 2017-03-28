/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.FindParametricFilter;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Slider;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
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
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ", Pattern.LITERAL);

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
        verifyThat("Topic map element displayed", results.topicMapVisible());
    }

    @Test
    public void testNumbersForMapInSliders() {
        search("gove");

        slidingIncreasesNumber(results.speedVsAccuracySlider());
    }

    private void slidingIncreasesNumber(final Slider slider) {
        final int firstNumber = sliderToolTipValue(slider);

        slider.dragBy(100);
        slider.hover();
        assertThat("Tooltip reappears after dragging", slider.tooltip().isDisplayed());

        verifyThat("New tooltip value bigger than old", slider.getValue(), greaterThanOrEqualTo(firstNumber));
    }

    @Test
    public void testEveryMapEntityHasText() {
        search("trouble");

        results.speedVsAccuracySlider().dragBy(100);
        Waits.loadOrFadeWait();
        results.waitForMapLoaded();

        results.speedVsAccuracySlider().hover();
        final int numberEntities = results.numberOfMapEntities();

        final List<WebElement> textElements = results.mapEntityTextElements();
        verifyThat("Same number of text elements as map pieces", textElements.size(), is(numberEntities));

        results.waitForMapLoaded();
        for(final WebElement textElement : textElements) {
            verifyThat("Text element not empty", textElement.getText(), not(""));
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
        verifyThat("The correct filter label has appeared", findPage.filterLabelsText(), hasItem(containsString(filterName)));
    }

    @Test
    public void testClickingOnMapEntities() {
        search("m");

        assumeThat("Search has results for this data", results.emptyMessage(), not(displayed()));
        final List<String> clusterNames = results.conceptClusterNames();
        final List<String> addedConcepts = new ArrayList<>();

        addedConcepts.add(results.clickConceptAndAddText(clusterNames.size()));
        results.waitForMapLoaded();

        addedConcepts.add(results.clickConceptAndAddText(results.conceptClusterNames().size()));
        Waits.loadOrFadeWait();

        for(final String concept : addedConcepts) {
            verifyThat("Concept " + concept + " was added to the Concepts Panel", selectedConcepts(), hasItem(concept));
        }
    }

    @Test
    @ResolvedBug("FIND-620")
    public void testToolTipNotLyingAboutNumberDocsUsed() {
        search("thing");

        final Slider slider = results.speedVsAccuracySlider();
        final int originalToolTipValue = sliderToolTipValue(slider);

        final List<String> originalParentEntityNames = results.conceptClusterNames();

        slider.dragBy(30);
        results.waitForMapLoaded();
        final List<String> changedParentNames = results.conceptClusterNames();
        assertThat("Changing the slider has changed the map", changedParentNames,
                   anyOf(not(hasSize(originalParentEntityNames.size())),
                         not(containsItems(originalParentEntityNames))));

        slider.dragBy(-30);
        results.waitForMapLoaded();

        //Selenium Actions.moveByOffset takes int -> cannot move by <1%
        //Getting within 1 doc of the original value is permissible
        assumeThat("Have returned tooltip to original value", sliderToolTipValue(slider),
                   anyOf(greaterThanOrEqualTo(originalToolTipValue - 1),
                         lessThanOrEqualTo(originalToolTipValue + 1)));
        verifyThat("Same parent concepts as when originally loaded", results.conceptClusterNames(), containsItems(originalParentEntityNames));
    }

    private int sliderToolTipValue(final Slider slider) {
        slider.hover();
        new WebDriverWait(getDriver(), 5)
                .until(ExpectedConditions.visibilityOf(slider.tooltip()));
        verifyThat("Tooltip appears on hover", slider.tooltip().isDisplayed());
        return slider.getValue();
    }

    @Test
    @ResolvedBug("FIND-650")
    public void testTopicMapRendersWhenManyNewTabs() {
        final int numberTabs = 8;
        search("grey");

        final SearchTabBar tabBar = getElementFactory().getSearchTabBar();
        for(int i = 0; i < numberTabs; i++) {
            tabBar.newTab();
        }

        tabBar.switchTo(numberTabs / 2);
        results.waitForMapLoaded();

        for(int j = 0; j < numberTabs; j++) {
            tabBar.switchTo(j);
            results = getElementFactory().getTopicMap();
            results.waitForMapLoaded();
            verifyThat("Map has appeared on tab " + (j + 1), results.mapEntities(), hasSize(greaterThan(0)));
        }
    }

    @Test
    @ResolvedBug("FIND-1007")
    // selenium user must have at least one saved search -- add that to setup once routing is fixed
    public void testTopicMapRendersColoursOnRouting() {
        final SearchTabBar searchTabBar = getElementFactory().getSearchTabBar();
        searchTabBar.waitUntilSavedSearchAppears();
        searchTabBar.switchTo(searchTabBar.savedTabTitles().get(0));
        final TopicMapView topicMap = getElementFactory().getTopicMap();
        topicMap.waitForMapLoaded();

        final List<WebElement> initialEntities = topicMap.mapEntities();
        verifyThat("Map has appeared", initialEntities, hasSize(greaterThan(0)));

        verifyEntityGradients(topicMap);

        final SunburstView sunburstView = findPage.goToSunburst();
        sunburstView.waitForSunburst();

        // modify search
        findService.searchAnyView("cat");

        findPage.goToTopicMap();
        topicMap.waitForMapLoaded();

        final List<WebElement> newEntities = topicMap.mapEntities();
        verifyThat("Map has appeared", newEntities, hasSize(greaterThan(0)));

        verifyEntityGradients(topicMap);
    }

    private void verifyEntityGradients(final TopicMapView topicMap) {
        final Set<String> newFills = topicMap.getFills();
        final Set<String> newFiltered = newFills.stream().filter(x -> x.contains("topic-map")).collect(Collectors.toSet());

        verifyThat("Paths are linked to their gradients", topicMap.getGradientIds().containsAll(getIdsFromFillUrls(newFills)));
        verifyThat("URLs point to topic-map",
                   newFiltered,
                   hasSize(newFills.size()));
    }

    private Set<String> getIdsFromFillUrls(final Set<String> urls) {
        return urls
                .stream()
                .map(fillUrl -> fillUrl.substring(fillUrl.indexOf('#') + 1, fillUrl.length() - 2))
                .collect(Collectors.toSet());
    }

    private void search(final String term) {
        findService.searchAnyView(term);
        results.waitForMapLoaded();
    }

    private String stripSpaces(final CharSequence term) {
        return SPACE_PATTERN.matcher(term).replaceAll(Matcher.quoteReplacement(""));
    }

    private String addsQuotes(final String term) {
        return term.replace("\"", "");
    }

    private List<String> selectedConcepts() {
        return conceptsPanel.selectedConceptHeaders().stream()
                .map(this::stripSpaces)
                .map(this::addsQuotes)
                .collect(Collectors.toList());
    }
}
