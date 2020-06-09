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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OnPremNavBarSettings extends NavBarSettings {
    private static final Pattern COMPILE = Pattern.compile(" ", Pattern.LITERAL);
    private final WebDriver driver;

    public OnPremNavBarSettings(final WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public void goToSettings() {
        openSettings();
        header().findElement(By.cssSelector("li[data-pagename='settings'] a")).click();
    }

    public List<String> getAvailableDashboards() {
        openSideBar();
        final WebElement dashboardsHeader = header().findElement(By.cssSelector("ul.side-menu li[data-pagename='dashboards']"));
        dashboardsHeader.click();
        return dashboardsHeader.findElements(By.cssSelector(".nav-second-level > li > a")).stream()
                .map(WebElement::getText).collect(Collectors.toList());
    }

    public void goToDashboard(final String dashboardName) {
        openSideBar();
        header().findElement(By.cssSelector("ul.side-menu li[data-pagename='dashboards']")).click();
        final By cssSelector = By.cssSelector("ul.side-menu li[data-pagename='dashboards/" + COMPILE.matcher(dashboardName).replaceAll(Matcher.quoteReplacement("%20")) + "']");
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(cssSelector));
        header().findElement(cssSelector).click();
    }
}
