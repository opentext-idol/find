/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OnPremNavBarSettings extends NavBarSettings {
    public OnPremNavBarSettings(final WebDriver driver) { super(driver);}

    public void goToSettings() {
        openSettings();
        header().findElement(By.cssSelector("li[data-pagename='settings'] a")).click();
    }

    public void goToDashboard(final String dashboardName) {
        openSideBar();
        header().findElement(By.cssSelector("ul.side-menu li[data-pagename='dashboards']")).click();
        header().findElement(By.cssSelector("ul.side-menu li[data-pagename='dashboards/" + dashboardName + "']")).click();
    }
}
