package com.autonomy.abc.selenium.find.comparison;

import com.autonomy.abc.selenium.find.bi.MapView;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ListView;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class ResultsComparisonView {
    private final WebElement wholeContainer;
    private final WebDriver driver;

    private ResultsComparisonView(final WebElement serviceViewContainer, final WebDriver driver) {
        wholeContainer = serviceViewContainer;
        this.driver = driver;
        waitForLoad(driver);
    }

    public ResultsComparisonView(final WebDriver driver) {
        this(driver.findElement(By.cssSelector(".service-view-container:not(.hide)")), driver);
    }

    //LIST VIEW
    public List<FindResult> getResults(final AppearsIn appearsIn) {
        return resultsView(appearsIn).getResults();
    }

    public int getResultsCountFor(final AppearsIn appearsIn) {
        return resultsView(appearsIn).getTotalResultsNum();
    }

    public ListView resultsView(final AppearsIn appearsIn) {
        return new ListView(wholeContainer.findElement(appearsIn.getResultsListLocator()), driver);
    }

    //TOPIC MAP VIEW
    public TopicMapView topicMapView(final AppearsInTopicMap panel) {
        goToTopicMapView(panel);
        return new TopicMapView(driver);
    }

    private void goToTopicMapView(final AppearsInTopicMap panel) {
        wholeContainer.findElement(panel.mapLocator()).click();
    }

    public TopicMapView topicMap() {
        final WebElement map = ElementUtil.ancestor(wholeContainer.findElement(By.cssSelector(".entity-topic-map")), 2);
        return new TopicMapView(map, driver);
    }

    //CHANGE VIEW
    public void goToListView() {
        wholeContainer.findElement(By.cssSelector("[data-tab-id='list']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.visibilityOf(wholeContainer.findElement(By.cssSelector(".results-list-container"))));
    }

    public MapView goToMapView() {
        wholeContainer.findElement(By.cssSelector("[data-tab-id='map']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.visibilityOf(wholeContainer.findElement(By.cssSelector(".location-comparison-map"))));
        return new MapView(driver, true);
    }

    private static void waitForLoad(final WebDriver driver) {
        new WebDriverWait(driver, 20)
                .withMessage("waiting for comparison view")
                .until(visibilityOfElementLocated(By.cssSelector(".service-view-container:not(.hide) .comparison-view")));
    }
}
