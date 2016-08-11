package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.openqa.selenium.By.cssSelector;

public class MapView {
    private final WebDriver driver;
    private final WebElement container;
    private final By mapLocator = By.xpath("//*[starts-with(@class,'location')]");

    public MapView(final WebDriver driver) {
        this.driver = driver;
        this.container = driver.findElement(By.cssSelector(".service-view-container:not(.hide)"));
    }

    public WebElement map() {
        return container.findElement(mapLocator);
    }

    public boolean mapPresent() {
        return container.findElements(mapLocator).size()>0;
    }

    public boolean isLoading() {
        return container.findElements(cssSelector(".map-loading-spinner > .loading-spinner:not(.hide)")).size()>0;
    }

    public void waitForMarkers() {
        new WebDriverWait(driver,40).withMessage("Map never stopped loading")
                .until(ExpectedConditions.presenceOfElementLocated(cssSelector(".service-view-container:not(.hide) .map-loading-spinner > .loading-spinner.hide")));
    }

    public List<WebElement> markers() {
        return container.findElements(By.cssSelector(".leaflet-marker-pane .awesome-marker"));
    }

    public List<WebElement> markerClusters() {
        return container.findElements(By.cssSelector(".marker-cluster"));
    }

    public WebElement popover() {
        return container.findElement(By.cssSelector(".leaflet-popup"));
    }

    public void clickMarker(WebElement marker) {
        Waits.loadOrFadeWait();
        marker.click();
        new WebDriverWait(driver,5).until(ExpectedConditions.presenceOfElementLocated(cssSelector(".leaflet-popup")));
    }

    public boolean noResults() {
        final String message = "'There are no results with the location field selected'";
        return container.findElements(By.xpath(".//p[contains(text(),"+message+")]")).size()>0;
    }

    public int numberResults() {
        String number = Container.currentTabContents(driver).findElement(By.cssSelector(".map-results-count > strong:nth-child(3)")).getText();
        return Integer.parseInt(number);
    }

    private int numberInCluster(WebElement cluster) {
        return Integer.parseInt(cluster.findElement(By.tagName("span")).getText());
    }

    public int countLocationsForComparer() {
      return countLocationsFor("green","first");
    }

    public int countLocationsForComparee() { return countLocationsFor("red","second"); }

    private int countLocationsFor(final String colour, final String position) {
        int total = 0;
        total+=container.findElements(By.cssSelector(".leaflet-marker-pane .awesome-marker-icon-"+colour+"")).size();
        List<WebElement> markerClusters = container.findElements(By.cssSelector(".leaflet-marker-pane ."+position+"-location-cluster"));
        for(WebElement clusterMarker : markerClusters) {
            total+=numberInCluster(clusterMarker);
        }
        return total;
    }
}
