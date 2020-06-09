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

package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.Container;
import com.google.common.base.Function;
import com.hp.autonomy.frontend.selenium.element.RangeInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TopicMapView {
    private static final int CONCEPT_CLUSTER_TIMEOUT = 25;
    private static final By CONCEPT_LOCATOR = By.cssSelector(".entity-topic-map > svg > path[stroke-opacity='0.7']");
    private static final By CONCEPT_CLUSTER_LOCATOR = By.cssSelector(".entity-topic-map > svg > path[stroke-opacity='0.2']");

    private final WebDriver driver;
    private final WebElement container;

    public TopicMapView(final WebDriver driver) {
        this.driver = driver;
        container = ElementUtil.ancestor(Container.currentTabContents(driver).findElement(By.className("entity-topic-map")), 2);
    }

    public TopicMapView(final WebElement element, final WebDriver driver) {
        this.driver = driver;
        container = element;
    }

    //GENERAL PAGE
    public boolean topicMapVisible() {
        return !findElements(By.cssSelector(".entity-topic-map:not(.hide)")).isEmpty();
    }

    public boolean topicMapPresent() {
        return !findElements(By.cssSelector(".entity-topic-map.clickable:not(.hide)")).isEmpty();
    }

    public WebElement emptyMessage() {
        return findElement(By.cssSelector(".entity-topic-map-empty p"));
    }

    public RangeInput speedVsAccuracySlider() {
        return new RangeInput(findElement(By.className("range-input-slider")), driver, 10);
    }

    private void offCentreClick(final WebElement element) {
        final int xOffset = element.getSize().getWidth() / 8;
        final int yOffset = element.getSize().getHeight() / 8;
        final Actions build = new Actions(driver);
        build.moveToElement(element, xOffset, yOffset).click().build().perform();
    }

    public void waitForMapLoaded() {
        waitForConceptClusters();
    }

    //MAP
    public WebElement map() {
        return findElement(By.cssSelector(".entity-topic-map.clickable"));
    }

    public int numberOfMapEntities() {
        return mapEntities().size();
    }

    public List<WebElement> mapEntities() {
        return findElements(By.cssSelector(".entity-topic-map > svg > path"));
    }

    private List<WebElement> mapEntityTextElements() {
        return findElements(By.cssSelector(".entity-topic-map > svg > text"));
    }

    public List<String> mapEntityText() {
        return mapEntityTextElements().stream()
            .map(this::getElementText)
            .collect(Collectors.toList());
    }

    private String getElementText(final SearchContext webElement) {
        return webElement.findElements(By.tagName("tspan")).stream()
            .map(WebElement::getText)
            .collect(Collectors.joining(" "));
    }

    //CONCEPT CLUSTERS/PARENT ENTITIES
    //Complicated because need to wait until any entity exists before then waiting until elements
    //have an opacity of either the top or bottom layer (i.e. the map is done loading).
    private void waitForConceptClusters() {
        new WebDriverWait(driver, CONCEPT_CLUSTER_TIMEOUT)
            .withMessage("entity to exist with opacity 2")
            .until(ExpectedConditions.presenceOfElementLocated(CONCEPT_CLUSTER_LOCATOR));

        new WebDriverWait(driver, 10)
            .withMessage("all entities to have opacity of either 0.2 or 0.7")
            .until((Function<? super WebDriver, Boolean>)x ->
                container.findElements(
                    By.cssSelector(".entity-topic-map > svg > path:not([stroke-opacity='0.2']):not([stroke-opacity='0.7'])")
                ).isEmpty());
    }

    private List<WebElement> conceptClusters() {
        return findElements(CONCEPT_CLUSTER_LOCATOR);
    }

    //TODO: In IE this does not always reveal the lower layer
    private void clickConceptClusters() {
        //TODO: this won't work for any topic map with medium or small sized polygons (or long topic titles)
        conceptClusters().forEach(this::offCentreClick);
    }

    public List<String> conceptClusterNames() {
        final int numberOfClusters = conceptClusters().size();
        final List<String> entityText = mapEntityText();
        final int max = entityText.size() - 1;

        final List<String> clusterNames = new ArrayList<>(numberOfClusters);
        for(int i = 0; i < numberOfClusters; i++) {
            clusterNames.add(entityText.get(max - i));
        }

        return clusterNames;
    }

    public String clickNthClusterHeading(final int index) {
        waitForMapLoaded();

        final int workingIndex = conceptClusters().size() - 1 - index;
        final int actualIndex = workingIndex + concepts().size();
        final WebElement textElement = mapEntityTextElements().get(actualIndex);

        final String text = getElementText(textElement);
        textElement.click();
        return text;
    }

    private TopicMapConcept nthConceptCluster(final int n) {
        return new TopicMapConcept(conceptClusters().get(n));
    }

    public void waitForConcepts() {
        new WebDriverWait(driver, 10)
            .withMessage("bottom layer entities to reach the right opacity")
            .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(CONCEPT_LOCATOR));
    }

    public List<WebElement> concepts() {
        return findElements(CONCEPT_LOCATOR);
    }

    public String clickConceptAndAddText(final int noOfClusters) {
        waitForMapLoaded();

        final int maxIndex = mapEntities().size() - 1;

        waitForConceptClusters();
        clickConceptClusters();
        waitForConcepts();

        final int i = maxIndex - 1;
        final WebElement textElement = mapEntityTextElements().get(i - noOfClusters);
        final String concept = getElementText(textElement);
        offCentreClick(textElement);
        Waits.loadOrFadeWait();
        return concept;
    }

    private List<ImmutablePair<WebElement, Integer>> childConcepts(final int clusterIndex) {
        //((lowestX,highestX),(lowestY,highestY))
        final Double[][] boundariesOfChosenCluster = nthConceptCluster(clusterIndex).getBoundaries();

        final Point mapCoordinates = map().getLocation();
        //L:Concept; Y:Index
        final List<ImmutablePair<WebElement, Integer>> childConceptsOfChosenCluster = new ArrayList<>();

        int entityIndex = 0;
        for(final WebElement concepts : concepts()) {
            final Dimension entitySize = concepts.getSize();
            final Point absolutePosition = concepts.getLocation();

            final int centreX = absolutePosition.x - mapCoordinates.x + entitySize.getWidth() / 2;
            final int centreY = absolutePosition.y - mapCoordinates.y + entitySize.getHeight() / 2;
            final Point centre = new Point(centreX, centreY);

            if(boundariesOfChosenCluster[0][0] <= centre.x && centre.x <= boundariesOfChosenCluster[0][1]
                && boundariesOfChosenCluster[1][0] <= centre.y && centre.y <= boundariesOfChosenCluster[1][1]) {
                childConceptsOfChosenCluster.add(new ImmutablePair<>(concepts, entityIndex));
            }

            entityIndex++;
        }
        return childConceptsOfChosenCluster;
    }

    private List<String> namesOfChildConcepts(final Collection<ImmutablePair<WebElement, Integer>> childConceptsOfChosenCluster) {
        final int numberOfConcepts = concepts().size();
        final List<String> textElements = mapEntityText();

        return childConceptsOfChosenCluster.stream()
            .map(path -> textElements.get(numberOfConcepts - 1 - path.getRight()))
            .collect(Collectors.toList());
    }

    public List<String> getChildConceptsOfCluster(final int clusterIndex) {
        final List<ImmutablePair<WebElement, Integer>> childEntitiesOfChosenCluster = childConcepts(clusterIndex);

        return namesOfChildConcepts(childEntitiesOfChosenCluster);
    }

    public Set<String> getGradientIds() {
        return findElements(By.tagName("linearGradient"))
            .stream()
            .map(tag -> tag.getAttribute("id"))
            .collect(Collectors.toSet());
    }

    public Set<String> getFills() {
        return mapEntities().stream()
            .map(path -> path.getAttribute("fill"))
            .collect(Collectors.toSet());
    }

    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}
