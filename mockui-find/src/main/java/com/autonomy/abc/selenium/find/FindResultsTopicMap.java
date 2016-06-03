package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class FindResultsTopicMap extends FindResultsPage{

    public FindResultsTopicMap(WebDriver driver) {
         super(driver);
    }
    public void goToTopicMap(){
         findElement(By.cssSelector("[data-tab-id='topic-map']")).click();
         new WebDriverWait(getDriver(),15).until(ExpectedConditions.visibilityOf(findElement(By.cssSelector(".entity-topic-map"))));
    }

    public boolean topicMapVisible(){
         return findElements(By.cssSelector(".entity-topic-map")).size()>0;
    }

    private WebElement sliderBlock(){return findElement(By.cssSelector(".slider-block div:nth-child(2)"));}

    public WebElement relevanceVsClusteringSlider(){return sliderBlock().findElement(By.cssSelector("[id*='relevance-slider']"));}

    public WebElement numberTopicsSlider(){return sliderBlock().findElement(By.cssSelector("[id*='count-slider']"));}

    //sliders
    public void hoverOnSlider(WebElement slider){
         DriverUtil.hover(getDriver(),sliderHandle(slider));
    }

    public int numberinMap(WebElement slider){
         return Integer.parseInt(slider.findElement(By.className("tooltip-inner")).getText());
    }

    public WebElement sliderToolTip(WebElement slider){
         return slider.findElement(By.className("tooltip-main"));
    }

    public WebElement sliderHandle(WebElement slider){
         return slider.findElement(By.className("min-slider-handle"));
    }

    //probably shouldn't be in here -> should be somewhere like DriverUtil
    public void dragSliderByI(WebElement slider,int i){
         int width = slider.getSize().getWidth();
         Actions builder = new Actions(getDriver());
         Action dragSlider = builder.clickAndHold(sliderHandle(slider)).moveByOffset((width/100)*i,0).release().build();
         dragSlider.perform();
    }

    public void dragSlider(WebElement slider){
         dragSliderByI(slider,100);
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
 }

