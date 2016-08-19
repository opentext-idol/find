package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ToolTips {
    private List<WebElement> toolTips = new ArrayList<>();


    private ToolTips(WebDriver driver){
        this.toolTips = driver.findElements(By.cssSelector(".tooltip"));
    }
    
    public static List<WebElement> toolTips(WebDriver driver){
        return new ToolTips(driver).toolTips;
    }
}
