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

public class StaticContentWidgetITCase extends DashboardITCase {
    public StaticContentWidgetITCase(final TestConfig config) {
        super(config, 1, "Static Content Dashboard");
    }

    @Test
    public void testElementExists() {
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("class has not been rendered", webElement.findElement(By.cssSelector(".test")) != null);
    }
}
