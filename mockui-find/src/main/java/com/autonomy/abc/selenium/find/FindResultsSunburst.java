package com.autonomy.abc.selenium.find;


import com.autonomy.abc.selenium.find.filters.ParametricFilterNode;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

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
        LOGGER.info("segments size"+findElements(By.xpath("//path[not(contains(@fill,'#f0f0f0')) and not(contains(@fill,'#ffffff'))]")).size());
        //return findElements(By.cssSelector("path:not([fill='#f0f0f0']), path:not([fill='#ffffff'])"));
        return findElements(By.xpath("//svg/path/@fill[.!='#f0f0f0' and .!='#ffffff']"));
    }

    //decrement by 1 to discount the centre of sunburst
    public int numberOfSunburstSegments(){
        return findSunburstSegments().size() - 1;
    }

    public WebElement getSunburstCentre(){return findElement(By.cssSelector("svg > path[fill='#ffffff']"));}

    public String getSunburstCentreName(){
        return findElement(By.className("sunburst-sector-name")).getText();}

    public String getSunburstCentreNumber(){return findElement(By.cssSelector(".sunburst-sector-name div")).getText();}

    public WebElement getIthSunburstSegment(int i){
        List<WebElement> actualSegments = findElements(By.cssSelector("path:not([fill='#f0f0f0']):not([fill='#ffffff'])]"));
        return actualSegments.get(i);
    }

    public String hoverOnSegmentGetCentre(int i){
        //first SunburstSegment is centre so inc by 1
        DriverUtil.hover(getDriver(),getIthSunburstSegment(i));
        return getSunburstCentreName();
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
