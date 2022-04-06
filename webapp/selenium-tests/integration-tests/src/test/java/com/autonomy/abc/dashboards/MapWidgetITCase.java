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

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.openqa.selenium.By.cssSelector;

public class MapWidgetITCase extends ClickableDashboardITCase {
    public MapWidgetITCase(final TestConfig config) {
        super(config, 1, "Map Dashboard", "Map", "MapSearch");
    }

    @Test
    public void testMarkersLoad() {
        new WebDriverWait(getDriver(), 60).withMessage("Markers never loaded")
                .until(ExpectedConditions.presenceOfElementLocated(cssSelector(".leaflet-marker-pane .awesome-marker")));
    }

    @Test
    public void testMapLoads() {
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("leaflet has not initialised the map container", webElement.findElement(By.className("leaflet-container")) != null);
        assertThat("no map tiles have loaded", !webElement.findElements(cssSelector(".leaflet-tile")).isEmpty());
    }
}
