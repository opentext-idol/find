package com.autonomy.abc.selenium.find;


import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;


public class FindResultsSunburst extends FindResultsPage{

    public FindResultsSunburst(WebDriver driver) {
        super(driver);
    }

    //Navigation
    public void goToSunburst(){
       findElement(By.cssSelector("[data-tab-id='sunburst']")).click();
        new WebDriverWait(getDriver(),15).until(ExpectedConditions.visibilityOf(findElement(By.cssSelector(".sunburst"))));
    }

    //Display
    public boolean mainResultsContainerHidden(){
        return !(findElement(By.className("main-results-content-container")).isDisplayed());
    }

    public boolean sunburstVisible(){
        return findElement(By.cssSelector(".sunburst svg")).isDisplayed();
    }


    public List<WebElement> findSunburstSegments(){
        return findElements(By.cssSelector("path:not([fill='#f0f0f0']):not([fill='#ffffff'])"));
    }

    public int numberOfSunburstSegments(){
        return findSunburstSegments().size();
    }

    public void waitForSunburst(){
        new WebDriverWait(getDriver(),15).until(ExpectedConditions.invisibilityOfElementLocated(By.className("view-server-loading-indicator")));
        }

    public WebElement sunburstCentre(){return findElement(By.cssSelector("svg > path[fill='#ffffff']"));}

    public String getSunburstCentreName(){
        return findElement(By.className("sunburst-sector-name")).getText();}

    public WebElement getIthSunburstSegment(int i){
        List<WebElement> actualSegments = findSunburstSegments();
        return actualSegments.get(i);
    }

    public void hoverOverTooFewToDisplaySegment(){
        DriverUtil.hover(getDriver(),findElement(By.cssSelector("svg > path[fill='#f0f0f0']")));
    }

    public String hoverOnSegmentGetCentre(int i){
        DriverUtil.hover(getDriver(),getIthSunburstSegment(i));
        return getSunburstCentreName();
    }

    public void hoveringRight(WebElement element){
        Actions builder = new Actions(getDriver());
        Dimension dimensions = element.getSize();
        builder.moveToElement(element, (dimensions.getWidth()/4)*3, dimensions.getHeight()/2);
        Action hover = builder.build();
        hover.perform();
    }
    //Parametric Filtering
    public String nthParametricFilterName(int i){
        return nthParametricFilter(i).getText();
    }

    public WebElement nthParametricFilter(int i){
        return findElement(By.cssSelector(".parametric-selections span:nth-child("+i+")"));
    }

    public ChosenDrop parametricSelectionDropdown(int i){
        return new ChosenDrop(nthParametricFilter(i),getDriver());
    }

    public List<String> getParametricDropdownItems(int i){
        ChosenDrop dropdown = parametricSelectionDropdown(i);
        return ElementUtil.getTexts(dropdown.getItems());
    }




}
