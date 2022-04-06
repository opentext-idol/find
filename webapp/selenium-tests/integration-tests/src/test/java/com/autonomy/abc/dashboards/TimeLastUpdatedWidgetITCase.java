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
        super(config, 2, "Time Last Refreshed Dashboard");
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
