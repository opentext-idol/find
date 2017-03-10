/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public class TimeLastUpdatedWidgetITCase extends DashboardITCase {
    public TimeLastUpdatedWidgetITCase(final TestConfig config) {
        super(config, 9, "Time Last Refreshed Dashboard");
    }

    @Test
    public void testElementsExist() {
        assertThat("last refreshed time has not been rendered", getDriver().findElement(By.cssSelector(".last-refresh")) != null);
        assertThat("next refresh time has not been rendered", getDriver().findElement(By.cssSelector(".next-refresh")) != null);
        assertThat("update progress has not been rendered", getDriver().findElement(By.cssSelector(".update-progress")) != null);
        assertThat("spinner has not been rendered", getDriver().findElement(By.cssSelector(".updating")) != null);
    }

    @Test
    public void testRefreshElementsShown() {
        final WebElement updateProgress = getDriver().findElement(By.cssSelector(".update-progress"));
        final WebElement updateSpinner = getDriver().findElement(By.cssSelector(".updating"));
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfAllElements(Arrays.asList(updateProgress, updateSpinner)));
    }
}
