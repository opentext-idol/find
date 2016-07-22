package com.autonomy.abc.selenium.find.numericWidgets;

import com.hp.autonomy.frontend.selenium.element.DatePicker;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;


public class MainNumericWidget {
    private final WebElement container;
    private final WebDriver driver;

    private enum LimitType{
        max,
        min;
    }

    public MainNumericWidget(final WebDriver driver) {
        this.driver = driver;
        this.container = driver.findElement(By.className("middle-container-time-bar"));
    }

    public void closeWidget(){
        container.findElement(By.cssSelector(".hp-close.time-bar-container-icon")).click();
    }


    public void reset(){
        container.findElement(By.className("numeric-parametric-reset")).click();
    }

    //COULD BE 1
    public void noMin(){
        container.findElement(By.className("numeric-parametric-no-min")).click();
    }

    public void noMax(){
        container.findElement(By.className("numeric-parametric-no-max")).click();
    }

    public WebElement errorMessage(){
        return container.findElement(By.className("numeric-parametric-error-text"));
    }

    public String header(){
        return container.findElement(By.className("time-bar-header")).getText();
    }

    //ACTUAL GRAPH PART
    public WebElement graph(){
        return container.findElement(By.cssSelector("svg.chart"));
    }

    public WebElement selectionRec(){
        return graph().findElement(By.cssSelector("rect.selection"));
    }

    public boolean selectionRectangleExists(){
        return graph().findElements(By.cssSelector("rect.selection")).size()>0;
    }

    public List<WebElement> barsWithResults(){
        List<WebElement> bars = new ArrayList<>();
        for(WebElement bar:graph().findElements(By.cssSelector("g > g > rect:not([height='1'])"))){
            if(bar.isDisplayed()){
                bars.add(bar);
            }
        }
        return bars;
    }

    public void selectHalfTheBars(){
        List<WebElement> bars = barsWithResults();
        int index = bars.size()/2;

        WebElement bar = bars.get(index);
        clickAndDrag(100,0,bar);
    }

    public void waitUntilWidgetLoaded(){
        new WebDriverWait(driver,10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("numeric-parametric-loading-indicator")));
    }
    //Drag and drop not element -> needs to go in DriverUtils in QA infrastructure!!!
    public void clickAndDrag(int x_dest,int y_dest, WebElement startingElement){
        final Actions action = new Actions(driver);
        action.moveToElement(startingElement);
        action.clickAndHold().build().perform();
        action.moveByOffset(x_dest,y_dest).build().perform();
        action.release().build().perform();
    }

    //ALSO NEEDS TO GO IN DRIVERUTIL
    public void zoomIn(){
        Actions action = new Actions(driver);
        action.sendKeys(Keys.CONTROL).sendKeys(Keys.ADD).perform();

        graph().sendKeys(Keys.chord("Keys.CONTROL,Keys.ADD"));
    }

    //could cheat by just double clicking
    public void simulateZoomingIn(){
        final int clickNumber = 20;
        Actions action = new Actions(driver);

        for(int i=0;i<clickNumber;i++){
            action.doubleClick(graph());
        }

    }

    public void zoomOut(){
        (new Actions(driver)).sendKeys(Keys.CONTROL).sendKeys(Keys.SUBTRACT).perform();
    }




    //not all the numeric widgets have calendars e.g. place elevation
    //How differentiate between the 2 in tests?!?!

    //If Date
    //MinorMax should be an enum
    private WebElement dateValueInputBox(LimitType limit){
        return container.findElement(By.cssSelector(".input-group[data-date-attribute='"+limit.toString()+"-date']"));
    }
    public WebElement minDateValue(){
        return dateValueInputBox(LimitType.min);
    }
    public WebElement maxDateValue(){
        return dateValueInputBox(LimitType.max);
    }

    private String  numericValueInputBox(LimitType limit){
        return container.findElement(By.className("numeric-parametric-"+limit.toString()+"-input")).getAttribute("value");
    }
    public String minNumValue(){
        return numericValueInputBox(LimitType.min);}

    public String maxNumValue(){
        return numericValueInputBox(LimitType.max);
    }



    //need to open the calendar
    public DatePicker openCalendar(WebElement dateInput){
        //dateInput.findElement(By.className("hp-calendar")).click();
        return new DatePicker(dateInput,driver);
    }


}
