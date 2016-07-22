package com.autonomy.abc.selenium.find.numericWidgets;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class NumericWidget {
    private final WebElement container;
    private final WebDriver driver;

    NumericWidget(WebElement container, WebDriver driver){
        this.driver = driver;
        this.container = driver.findElement(By.className("svg-chart"));
    }


}
