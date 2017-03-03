/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public class MissingWidgetITCase extends DashboardITCase {

    public MissingWidgetITCase(final TestConfig config) {
        super(config, 1, "Missing Widget Dashboard");
    }

    @Test
    public void testElementExists() {
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("error message is incorrect or not displayed",
                   "Widget of type \"test\" could not be found. Please check your configuration and try again"
                           .equals(webElement.findElement(By.cssSelector(".widget-content")).getText()));
    }
}
