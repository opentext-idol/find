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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertTrue;

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
        final List<WebElement> results = webElement.findElements(By.cssSelector(".search-result"));

        assertThat("There should be at least one result", results, not(empty()));

        checkOutOfViewClass(webElement);
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
        checkOutOfViewClass(webElement);
    }

    private void checkOutOfViewClass(final SearchContext widget) {
        final List<WebElement> results = widget.findElements(By.cssSelector(".search-result"));

        results.forEach(result -> {
            final boolean isDisplayed = result.isDisplayed();
            final boolean hasClass = result.getAttribute("class").contains("out-of-view");

            assertTrue(
                    "All invisible results (and only invisible results) must have the out-of-view class",
                    isDisplayed ^ hasClass
            );
        });
    }
}
