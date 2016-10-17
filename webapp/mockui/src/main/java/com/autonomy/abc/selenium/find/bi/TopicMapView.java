/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.autonomy.abc.selenium.find.bi;

import com.hp.autonomy.frontend.selenium.element.Slider;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class TopicMapView {
    private final WebDriver driver;
    private final WebElement container;
    private final static By CONCEPT_LOCATOR = By.cssSelector(".entity-topic-map > svg > path[stroke-opacity='0.7']");
    private final static By CONCEPT_CLUSTER_LOCATOR = By.cssSelector(".entity-topic-map > svg > path[stroke-opacity='0.2']");

    public TopicMapView(final WebDriver driver) {
        this.driver = driver;
        this.container = driver.findElement(By.cssSelector(".service-view-container:not(.hide)"));
    }

    //GENERAL PAGE
    public boolean topicMapVisible() {
        return !findElements(By.cssSelector(".entity-topic-map:not(.hide)")).isEmpty();
    }

    public WebElement emptyMessage() {
        return findElement(By.cssSelector(".entity-topic-map-empty"));
    }

    public Slider speedVsAccuracySlider() {
        return new Slider(findElement(By.className("slider")), driver);
    }

    private void offCentreClick(WebElement element) {
        int xOffset = element.getSize().getWidth() / 8;
        int yOffset = element.getSize().getHeight() / 8;
        Actions build = new Actions(driver);
        build.moveToElement(element, xOffset, yOffset).click().build().perform();
    }

    public void waitForMapLoaded() {
        waitForConceptClusters();
    }

    //MAP
    public WebElement map () { return findElement(By.cssSelector(".entity-topic-map.clickable")); }

    public int numberOfMapEntities() {
        return mapEntities().size();
    }

    public List<WebElement> mapEntities() {
        return findElements(By.cssSelector(".entity-topic-map > svg > path"));
    }

    public List<WebElement> mapEntityTextElements() {
        return findElements(By.cssSelector(".entity-topic-map > svg > text"));
    }

    //CONCEPT CLUSTERS/PARENT ENTITIES
    //Complicated because need to wait until any entity exists before then waiting until elements
    //have an opacity of either the top or bottom layer (i.e. the map is done loading).
    private void waitForConceptClusters() {
        new WebDriverWait(driver,25)
                .withMessage("entity to exist with opacity 2")
                .until(ExpectedConditions.presenceOfElementLocated(CONCEPT_CLUSTER_LOCATOR));

        new WebDriverWait(driver,10)
                .withMessage("all entities to have opacity of either 0.2 or 0.7")
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(final WebDriver driver) {
                        return container.findElements(By.cssSelector(".entity-topic-map > svg > path:not([stroke-opacity='0.2']):not([stroke-opacity='0.7'])")).isEmpty();
                    }
                });
        }

    private List<WebElement> conceptClusters() {
        return findElements(CONCEPT_CLUSTER_LOCATOR);
    }

    private void clickConceptClusters() {
        conceptClusters().stream().forEach(this::offCentreClick);
    }

    public List<String> conceptClusterNames() {
        final List<String> clusterNames = new ArrayList<>();
        final int numberOfClusters = conceptClusters().size();
        final List<WebElement> mapEntities = mapEntityTextElements();
        final int max = mapEntities.size() - 1;
        for(int i = 0; i < numberOfClusters; i++) {
            clusterNames.add(mapEntities.get(max - i).getText().replace(" ", "").toLowerCase());
        }
        return clusterNames;
    }

    public String clickNthClusterHeading(final int index) {
        int workingIndex = conceptClusters().size() - 1 -index;
        int actualIndex = workingIndex + concepts().size();
        final WebElement entity = mapEntityTextElements().get(actualIndex);

        final String text = entity.getText();
        entity.click();
        return text;
    }

    public TopicMapConcept nthConceptCluster(final int n) {
        return new TopicMapConcept(conceptClusters().get(n));
    }

    //CHILD CONCEPTS/ENTITITES
    public void waitForConcepts() {
        new WebDriverWait(driver,10)
                .withMessage("bottom layer entities to reach the right opacity")
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(CONCEPT_LOCATOR));
    }

    public List<WebElement> concepts() {
        return findElements(CONCEPT_LOCATOR);
    }

    public String clickConceptAndAddText(int noOfClusters) {
        waitForMapLoaded();

        final int maxIndex = mapEntities().size() - 1;
        final int i = maxIndex - 1;

        waitForConceptClusters();
        clickConceptClusters();
        waitForConcepts();

        final String concept = mapEntityTextElements().get(i - noOfClusters).getText().replace(" ", "").toLowerCase();
        offCentreClick(mapEntityTextElements().get(i - noOfClusters));
        Waits.loadOrFadeWait();
        return concept;
    }

    private List<ImmutablePair> childConcepts(final int clusterIndex) {
        //((lowestX,highestX),(lowestY,highestY))
        final Double[][] boundariesOfChosenCluster =  nthConceptCluster(clusterIndex).getBoundaries();

        final Point mapCoordinates = map().getLocation();
        //L:Concept; Y:Index
        List<ImmutablePair> childConceptsOfChosenCluster = new ArrayList<>();

        int entityIndex = 0;
        for(WebElement concepts : concepts()) {
            final Dimension entitySize = concepts.getSize();
            final Point absolutePosition = concepts.getLocation();

            int centreX = absolutePosition.x - mapCoordinates.x + entitySize.getWidth()/2;
            int centreY = absolutePosition.y - mapCoordinates.y + entitySize.getHeight()/2;
            final Point centre = new Point(centreX, centreY);

            if((boundariesOfChosenCluster[0][0] <= centre.x && centre.x <= boundariesOfChosenCluster[0][1])
                    && boundariesOfChosenCluster[1][0] <= centre.y && centre.y <= boundariesOfChosenCluster[1][1]) {
                childConceptsOfChosenCluster.add(new ImmutablePair(concepts,entityIndex));
            }

            entityIndex++;
        }
        return childConceptsOfChosenCluster;
    }

    private List<String> namesOfChildConcepts(List<ImmutablePair> childConceptsOfChosenCluster){
        List<String> childConcepts = new ArrayList<>();
        for(ImmutablePair path : childConceptsOfChosenCluster) {
            int indexOfText = concepts().size() - 1 - (int) path.getRight();
            childConcepts.add(mapEntityTextElements().get(indexOfText).getText().replaceAll("\\s+",""));
        }

        return childConcepts;
    }

    public List<String> getChildConceptsOfCluster(final int clusterIndex) {
        final List<ImmutablePair> childEntitiesOfChosenCluster = childConcepts(clusterIndex);

        return namesOfChildConcepts(childEntitiesOfChosenCluster);
    }

    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}
