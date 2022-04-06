/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
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
