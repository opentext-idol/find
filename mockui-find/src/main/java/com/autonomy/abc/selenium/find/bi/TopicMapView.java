package com.autonomy.abc.selenium.find.bi;

import com.hp.autonomy.frontend.selenium.element.Slider;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class TopicMapView {
    private final WebDriver driver;
    private final WebElement container;

    public TopicMapView(final WebDriver driver) {
        this.driver = driver;
        this.container = driver.findElement(By.className("service-view-container"));
    }

    public boolean topicMapVisible() {
        return !findElements(By.cssSelector(".entity-topic-map:not(.hide)")).isEmpty();
    }

    public WebElement emptyMessage() {
        return findElement(By.cssSelector(".entity-topic-map-empty"));
    }

    public Slider speedVsAccuracySlider() {
        return new Slider(findElement(By.className("slider")), driver);
    }

    //map
    public int numberOfMapEntities() {
        return mapEntities().size();
    }

    public List<WebElement> mapEntities() {
        return findElements(By.cssSelector(".entity-topic-map > svg > path"));
    }

    public List<WebElement> topLevelMapEntities() {
        return findElements(By.cssSelector(".entity-topic-map > svg > path[stroke-opacity='0.2']"));
    }

    public List<WebElement> mapEntityTextElements() {
        return findElements(By.cssSelector(".entity-topic-map > svg > text"));
    }

    public void clickParentEntities() {
        final List<WebElement> webElements = topLevelMapEntities();
        for (WebElement cluster: webElements){
            offCentreClick(cluster);
        }
    }

    public void offCentreClick(WebElement element) {
        int xOffset = element.getSize().getWidth()/8;
        int yOffset = element.getSize().getHeight()/8;
        Actions build = new Actions(driver);
        build.moveToElement(element, xOffset, yOffset).click().build().perform();
    }

    public List<String> returnParentEntityNames() {
        final List<String> clusterNames = new ArrayList<>();
        final int numberOfClusters = topLevelMapEntities().size();
        final List<WebElement> mapEntities = mapEntityTextElements();
        final int max = mapEntities.size() - 1;
        for (int i = 0; i < numberOfClusters; i++) {
            clusterNames.add(mapEntities.get(max - i).getText().replace(" ", "").toLowerCase());
        }
        return clusterNames;
    }

    public String clickChildEntityAndAddText(int noOfClusters) {
        waitForMapLoaded();
        final int maxIndex = mapEntities().size() - 1;
        final int i = maxIndex - 1;

        Waits.loadOrFadeWait();
        clickParentEntities();
        Waits.loadOrFadeWait();

        final String concept = mapEntityTextElements().get(i - noOfClusters).getText().replace(" ", "").toLowerCase();
        offCentreClick(mapEntityTextElements().get(i - noOfClusters));
        Waits.loadOrFadeWait();
        return concept;
    }

    public void waitForMapLoaded() {
        new WebDriverWait(driver, 30).until(new mapLoaded());
    }

    private static class mapLoaded implements ExpectedCondition<Boolean> {
        @Override
        public Boolean apply(final WebDriver driver) {
            return driver.findElements(By.cssSelector(".entity-topic-map > svg > text")).size()>0;
        }
    }

    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}

