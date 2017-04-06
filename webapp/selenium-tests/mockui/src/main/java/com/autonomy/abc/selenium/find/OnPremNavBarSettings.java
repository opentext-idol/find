/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void goToDashboard(final String dashboardName) {
        openSideBar();
        header().findElement(By.cssSelector("ul.side-menu li[data-pagename='dashboards']")).click();
        final By cssSelector = By.cssSelector("ul.side-menu li[data-pagename='dashboards/" + COMPILE.matcher(dashboardName).replaceAll(Matcher.quoteReplacement("%20")) + "']");
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(cssSelector));
        header().findElement(cssSelector).click();
    }
}
