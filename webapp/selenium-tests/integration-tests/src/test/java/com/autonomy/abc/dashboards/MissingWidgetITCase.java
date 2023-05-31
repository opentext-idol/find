/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
