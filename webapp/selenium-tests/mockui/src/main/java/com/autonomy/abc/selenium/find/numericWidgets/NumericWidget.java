/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.numericWidgets;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class NumericWidget extends AppElement {

    private final WebElement container;

    public NumericWidget(final WebDriver driver, final WebElement outerContainer) {
        super(new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfNestedElementLocatedBy(outerContainer, By.cssSelector("svg.chart"))), driver);
        container = outerContainer.findElement(By.cssSelector("svg.chart"));
    }

    public WebElement getContainer() {
        return container;
    }

    public WebElement selectionRec() {
        return findElement(By.cssSelector("rect.selection"));
    }

    public int selectionRectangleWidth() {
        return (int)Double.parseDouble(selectionRec().getAttribute("Width"));
    }

    public boolean selectionRectangleExists() {
        return findElements(By.cssSelector("rect.selection")).size() > 0;
    }

    public List<WebElement> barsWithResults() {
        final List<WebElement> bars = new ArrayList<>();
        for(final WebElement bar : findElements(By.cssSelector("g > g > rect:not([height='1'])"))) {
            if(bar.isDisplayed()) {
                bars.add(bar);
            }
        }
        return bars;
    }
}
