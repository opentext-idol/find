package com.autonomy.abc.selenium.find;


import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
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
        return findElements(By.cssSelector("svg > path"));
    }

    public int numberOfSunburstSegments(){
        return findSunburstSegments().size() - 1;
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
