/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public class ResultsListWidgetITCase extends ClickableDashboardITCase {
    public ResultsListWidgetITCase(final TestConfig config) {
        super(config, 1, "List Dashboard", "List", "ListSearch");
    }

    @Test
    public void testElementExists() {
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("class has not been rendered", webElement.findElement(By.cssSelector(".results-list")) != null);
    }

    @Test
    public void testResultsDisplay() {
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("No visible results", !webElement.findElements(By.cssSelector(".search-result:not(.out-of-view)")).isEmpty());
        assertThat("Should be one hidden result", webElement.findElements(By.cssSelector(".search-result.out-of-view")).size() == 1);
    }

    @Test
    public void testResultsOrientation() {
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("incorrect flex direction, should be column", "column".equals(webElement.findElement(By.cssSelector(".results-list")).getCssValue("flex-direction")));
    }

    @Test
    public void testResize() {
        getDriver().manage().window().setSize(new Dimension(1000, 800));
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("Should be two visible results", webElement.findElements(By.cssSelector(".search-result:not(.out-of-view)")).size() == 2);
        assertThat("Should be four hidden results", webElement.findElements(By.cssSelector(".search-result.out-of-view")).size() == 4);
    }
}
