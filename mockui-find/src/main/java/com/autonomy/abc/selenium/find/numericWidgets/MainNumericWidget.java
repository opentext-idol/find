package com.autonomy.abc.selenium.find.numericWidgets;

import com.hp.autonomy.frontend.selenium.element.DatePicker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.ButtonReleaseAction;
import org.openqa.selenium.interactions.ClickAndHoldAction;
import org.openqa.selenium.interactions.MoveToOffsetAction;
import org.openqa.selenium.internal.Locatable;

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
        container.findElement(By.className(".hp-close.time-bar-container-icon")).click();
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


    public String header(){
        return container.findElement(By.className("time-bar-header")).getText();
    }

    //how the hell am I supposed to do the purple square
    //ACTUAL GRAPH PART
    public WebElement graph(){
        return container.findElement(By.cssSelector("svg.chart"));
    }


    //Drag and drop not element -> needs to go in DriverUtils in QA infrastructure!!!
    public void clickAndDrag( int x_dest,int y_dest){
        //move mouse to coordinates

        //blargh
        final Actions action = new Actions(driver);
        //can have clickAndHold w/ and w/o Element parameter
        action.clickAndHold().build().perform();
        //action.moveToOffsetAction(this.mouse, (Locatable)null, xOffset, yOffset));
        action.moveByOffset(x_dest,y_dest).build().perform();
        action.

        action.addAction(new ButtonReleaseAction(this.mouse, (Locatable)null));
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

    private WebElement numericValueInputBox(LimitType limit){
        return container.findElement(By.className("numeric-parametric-"+limit.toString()+"-input"));
    }
    public WebElement minNumValue(){
        return numericValueInputBox(LimitType.min);}

    public WebElement maxNumValue(){
        return numericValueInputBox(LimitType.max);
    }



    //need to open the calendar
    public DatePicker openCalendar(WebElement dateInput){
        //dateInput.findElement(By.className("hp-calendar")).click();
        return new DatePicker(dateInput,driver);
    }


}
