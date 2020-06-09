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

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public class StaticImageWidgetITCase extends DashboardITCase {
    public StaticImageWidgetITCase(final TestConfig config) {
        super(config, 1, "Static Image Dashboard");
    }

    @Test
    public void testImageElementExists() { // not sure if this test is any use
        final WebElement webElement = page.getWidgets().get(0);
        final WebElement staticImageElement = webElement.findElement(By.cssSelector(".static-image"));
        assertThat("class has not been rendered", staticImageElement != null);
    }

    @Test
    public void testImageElementSrc() {
        final WebElement webElement = page.getWidgets().get(0);
        final WebElement staticImageElement = webElement.findElement(By.cssSelector(".static-image"));
        final String src = staticImageElement.getCssValue("background-image");
        assertThat("src is incorrect", "url(\"http://placehold.it/800x300\")".equals(src));
    }
}
