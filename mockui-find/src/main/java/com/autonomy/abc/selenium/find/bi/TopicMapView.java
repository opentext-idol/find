package com.autonomy.abc.selenium.find.bi;

import com.hp.autonomy.frontend.selenium.element.Slider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class TopicMapView {
    private final WebDriver driver;
    private final WebElement container;

    public TopicMapView(WebDriver driver) {
        this.driver = driver;
        this.container = driver.findElement(By.className("service-view-container"));
    }
    public void goToTopicMap(){
         findElement(By.cssSelector("[data-tab-id='topic-map']")).click();
         new WebDriverWait(getDriver(),15).until(ExpectedConditions.visibilityOf(findElement(By.cssSelector(".entity-topic-map"))));
    }

    public boolean topicMapVisible(){
         return findElements(By.cssSelector(".entity-topic-map")).size()>0;
    }

    private WebElement sliderBlock(){return findElement(By.cssSelector(".slider-block div:nth-child(2)"));}

    public Slider relevanceVsClusteringSlider(){
        return new Slider(sliderBlock().findElement(By.cssSelector("[id*='relevance-slider']")), driver);
    }

    public Slider numberTopicsSlider(){
        return new Slider(sliderBlock().findElement(By.cssSelector("[id*='count-slider']")), driver);
    }

    //map
    public int numberOfMapEntities(){
         return mapEntities().size();
    }

    public List<WebElement> mapEntities(){
         return findElements(By.cssSelector(".entity-topic-map > svg > path"));
    }

    public List<WebElement> mapEntityTextElements(){
         return findElements(By.cssSelector(".entity-topic-map >svg > text"));
    }

    //adds text from input fraction of map and clicks each
    public List<String> clickEntitiesAndAddText(int fraction){
         List<String> addedConcepts = new ArrayList<>();
         int max = mapEntities().size() - 1;
         for(int i=0; i<mapEntities().size()/fraction;i++){
             waitForReload();
             addedConcepts.add(mapEntityTextElements().get(i).getText().replace(" ","").toLowerCase());
             mapEntities().get(max-i).click();
             waitForReload();
         }
         return addedConcepts;}

    public void waitForMapLoaded(){
         new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfAllElements(mapEntityTextElements()));
    }

    public void waitForReload(){
         new WebDriverWait(getDriver(),50).until(ExpectedConditions.invisibilityOfElementLocated(By.className("view-server-loading-indicator")));
     }

    private WebDriver getDriver() {
        return driver;
    }

    private WebElement findElement(By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(By locator) {
        return container.findElements(locator);
    }
 }

