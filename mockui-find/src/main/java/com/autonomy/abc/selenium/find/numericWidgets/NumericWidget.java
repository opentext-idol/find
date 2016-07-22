package com.autonomy.abc.selenium.find.numericWidgets;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class NumericWidget extends AppElement {

    final private WebElement container;
    public NumericWidget(WebDriver driver, WebElement outerContainer){
        //unless MainNumericWidget is also some kind of AppElement then have to pass its container
        super(outerContainer.findElement(By.cssSelector("svg.chart")),driver);
        this.container = outerContainer.findElement(By.cssSelector("svg.chart"));
    }

    public WebElement getContainer(){
        return container;
    }

    public WebElement selectionRec(){
        return findElement(By.cssSelector("rect.selection"));
    }

    public boolean selectionRectangleExists(){
        return findElements(By.cssSelector("rect.selection")).size()>0;
    }

    public List<WebElement> barsWithResults(){
        List<WebElement> bars = new ArrayList<>();
        for(WebElement bar:findElements(By.cssSelector("g > g > rect:not([height='1'])"))){
            if(bar.isDisplayed()){
                bars.add(bar);
            }
        }
        return bars;
    }

}
