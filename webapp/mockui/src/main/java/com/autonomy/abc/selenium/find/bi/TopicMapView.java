/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.autonomy.abc.selenium.find.bi;

import com.hp.autonomy.frontend.selenium.element.Slider;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

//TODO SORT OUT USE OF PARENT VS CHILD vs  TOP VS BASE
public class TopicMapView {
    private final WebDriver driver;
    private final WebElement container;
    private final static By CHILD_CONCEPT_LOCATOR = By.cssSelector(".entity-topic-map > svg > path[stroke-opacity='0.7']");
    private final static By PARENT_CONCEPT_LOCATOR = By.cssSelector(".entity-topic-map > svg > path[stroke-opacity='0.2']");

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
        waitForTopLevelEntities();
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

    //TOP LEVEL/PARENT ENTITIES
    //Complicated because need to wait until any entity exists before then waiting until elements
    //have an opacity of either the top or bottom layer (i.e. the map is done loading).
    private void waitForTopLevelEntities() {
        new WebDriverWait(driver,25)
                .withMessage("entity to exist with opacity 2")
                .until(ExpectedConditions.presenceOfElementLocated(PARENT_CONCEPT_LOCATOR));

        new WebDriverWait(driver,10)
                .withMessage("all entities to have opacity of either 0.2 or 0.7")
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(final WebDriver driver) {
                        return container.findElements(By.cssSelector(".entity-topic-map > svg > path:not([stroke-opacity='0.2']):not([stroke-opacity='0.7'])")).isEmpty();
                    }
                });
        }

    private List<WebElement> topLevelMapEntities() {
        return findElements(PARENT_CONCEPT_LOCATOR);
    }

    private void clickParentEntities() {
        topLevelMapEntities().stream().forEach(this::offCentreClick);
    }

    public List<String> parentEntityNames() {
        final List<String> clusterNames = new ArrayList<>();
        final int numberOfClusters = topLevelMapEntities().size();
        final List<WebElement> mapEntities = mapEntityTextElements();
        final int max = mapEntities.size() - 1;
        for(int i = 0; i < numberOfClusters; i++) {
            clusterNames.add(mapEntities.get(max - i).getText().replace(" ", "").toLowerCase());
        }
        return clusterNames;
    }

    public String clickNthClusterHeading(final int index) {
        int workingIndex = topLevelMapEntities().size() - 1 -index;
        int actualIndex = workingIndex + baseLevelEntities().size();
        final WebElement entity = mapEntityTextElements().get(actualIndex);

        final String text = entity.getText();
        entity.click();
        return text;
    }

    public TopicMapConcept clickNthCluster(final int n) {
        TopicMapConcept concept = new TopicMapConcept(topLevelMapEntities().get(n));
        concept.extractLocations();
        return concept;
    }

    //BASE LEVEL/CHILD ENTITIES
    public void waitForBaseLevelEntities() {
        new WebDriverWait(driver,10)
                .withMessage("base entities to reach the right opacity")
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(CHILD_CONCEPT_LOCATOR));
    }

    public List<WebElement> baseLevelEntities() {
        return findElements(CHILD_CONCEPT_LOCATOR);
    }

    public String clickChildEntityAndAddText(int noOfClusters) {
        waitForMapLoaded();

        final int maxIndex = mapEntities().size() - 1;
        final int i = maxIndex - 1;

        waitForTopLevelEntities();
        clickParentEntities();
        waitForBaseLevelEntities();

        final String concept = mapEntityTextElements().get(i - noOfClusters).getText().replace(" ", "").toLowerCase();
        offCentreClick(mapEntityTextElements().get(i - noOfClusters));
        Waits.loadOrFadeWait();
        return concept;
    }

    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}
