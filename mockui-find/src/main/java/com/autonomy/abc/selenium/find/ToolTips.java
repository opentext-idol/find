package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ToolTips extends AppElement{

    public ToolTips(final WebDriver driver){
        super(driver.findElement(By.tagName("body")),driver);
    }

    public List<WebElement> toolTips(){
        return findElements(By.cssSelector(".tooltip"));
    }
}
