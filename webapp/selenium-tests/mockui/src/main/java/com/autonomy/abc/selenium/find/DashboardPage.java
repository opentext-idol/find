/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class DashboardPage extends AppElement {
    DashboardPage(final WebDriver driver) {
        super(new WebDriverWait(driver, 30)
                      .withMessage("loading Dashboard page")
                      .until(ExpectedConditions.visibilityOfElementLocated(
                              By.className("dashboard"))), driver);
    }

    public boolean isFullscreen() {
        return getDriver().findElement(cssSelector(".widgets")).getAttribute("class").contains("fullscreen");
    }

    public List<WebElement> getWidgets() {
        return getDriver().findElements(cssSelector(".widget"));
    }

    public void enterFullscreen() {
        openDropdown();
        new WebDriverWait(getDriver(), 3).until(ExpectedConditions.visibilityOfElementLocated(cssSelector(".hp-expand")));
        getMaximizeButton().click();
    }

    public WebElement getDropdownToggle() {
        return getDriver().findElement(cssSelector(".btn.inline-dropdown"));
    }

    private void openDropdown() {
        getDropdownToggle().click();
    }

    private WebElement getMaximizeButton() {
        return getDriver().findElement(cssSelector(".hp-expand"));
    }

    public void waitForSunburstWidgetToRender() {
        new WebDriverWait(getDriver(), 60).withMessage("Sunburst never loaded")
                .until(presenceOfElementLocated(
                        cssSelector(".sunburst-visualizer-container path")));
    }

    public static class Factory implements ParametrizedFactory<WebDriver, DashboardPage> {
        @Override
        public DashboardPage create(final WebDriver context) {
            return new DashboardPage(context);
        }
    }
}
