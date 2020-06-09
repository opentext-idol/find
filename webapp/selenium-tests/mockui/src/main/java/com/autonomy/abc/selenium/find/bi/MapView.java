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

package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.junit.Assert.fail;
import static org.openqa.selenium.By.cssSelector;

public class MapView {
    private static final int MAP_LOAD_TIMEOUT = 60;
    private static final int WAIT_FOR_MAP_FIT_TO_BOUNDS = 5000;

    private final WebDriver driver;
    private final WebElement container;
    private final By mapLocator = By.xpath("//*[starts-with(@class,'location')]");

    public MapView(final WebDriver driver) {
        this(driver, false);
    }

    public MapView(final WebDriver driver, final boolean comparison) {
        this.driver = driver;
        container = comparison ? driver.findElement(By.className("location-comparison-map"))
                : ElementUtil.ancestor(Container.currentTabContents(driver).findElement(By.className("location-results-map")), 1);
    }

    public WebElement map() {
        return container.findElement(mapLocator);
    }

    public boolean mapPresent() {
        return !container.findElements(mapLocator).isEmpty();
    }

    public boolean isLoading() {
        return !container.findElements(cssSelector(".map-loading-spinner > .loading-spinner:not(.hide)")).isEmpty();
    }

    public void waitForMarkers() {
        new WebDriverWait(driver, MAP_LOAD_TIMEOUT).withMessage("Map never stopped loading")
                .until(ExpectedConditions.presenceOfElementLocated(cssSelector(".service-view-container:not(.hide) .map-loading-spinner > .loading-spinner.hide")));
        try {
            Thread.sleep(WAIT_FOR_MAP_FIT_TO_BOUNDS);
        } catch (final InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public List<WebElement> markers() {
        return container.findElements(cssSelector(".leaflet-marker-pane .awesome-marker"));
    }

    public List<WebElement> markerClusters() {
        return container.findElements(cssSelector(".marker-cluster"));
    }

    public WebElement popover() {
        return container.findElement(cssSelector(".leaflet-popup"));
    }

    public void clickMarker(final WebElement marker) {
        Waits.loadOrFadeWait();
        marker.click();
        new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(cssSelector(".leaflet-popup")));
    }

    public boolean noResults() {
        final String message = "'There are no results with the location field selected'";
        return !container.findElements(By.xpath(".//p[contains(text()," + message + ")]")).isEmpty();
    }

    public int numberOfDisplayedDocuments() {
        final String number = Container.currentTabContents(driver).findElement(cssSelector(".map-results-count > strong:nth-child(2)")).getText();
        return Integer.parseInt(number);
    }

    private int numberInCluster(final SearchContext cluster) {
        return Integer.parseInt(cluster.findElement(By.tagName("span")).getText());
    }

    public int countLocations() {
        int total = 0;
        total += container.findElements(cssSelector(".leaflet-marker-pane .awesome-marker")).size();
        final List<WebElement> markerClusters = container.findElements(cssSelector(".leaflet-marker-pane .marker-cluster"));
        for (final WebElement clusterMarker : markerClusters) {
            total += numberInCluster(clusterMarker);
        }
        return total;
    }

    public int countLocationsForComparer() {
        return countLocationsFor("green", "first");
    }

    public int countLocationsForComparee() {
        return countLocationsFor("red", "second");
    }

    public int countCommonLocations() {
        return countLocationsFor("orange", "both");
    }

    private int countLocationsFor(final String colour, final String position) {
        int total = 0;
        total += container.findElements(cssSelector(".leaflet-marker-pane .awesome-marker-icon-" + colour)).size();
        final List<WebElement> markerClusters = container.findElements(cssSelector(".leaflet-marker-pane ." + position + "-location-cluster"));
        for (final WebElement clusterMarker : markerClusters) {
            total += numberInCluster(clusterMarker);
        }
        return total;
    }
}
