/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.autonomy.abc.selenium.find.FindPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Assume;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public abstract class ClickableDashboardITCase extends DashboardITCase {
    private final String resultsViewType;
    private final String savedSearchName;

    ClickableDashboardITCase(final TestConfig config, final int numberOfWidgets, final String dashboardName, final String resultsViewType, final String savedSearchName) {
        super(config, numberOfWidgets, dashboardName);
        this.resultsViewType = resultsViewType;
        this.savedSearchName = savedSearchName;
    }

    @Test
    public void testClickThrough() {
        Assume.assumeNotNull(resultsViewType, savedSearchName);
        getDriver().findElement(By.cssSelector(".widget-content")).click();
        final FindPage findPage = getElementFactory().getFindPage();
        findPage.waitForLoad();
        final String savedSearchName = getDriver().findElement(By.cssSelector(".search-tabs-list .search-tab.active .search-tab-anchor .search-tab-title")).getText();
        final String resultsViewType = getDriver().findElement(By.cssSelector("div:not(.hide) > .bi-query-service-view .middle-container li.active .result-view-type span")).getText();
        assertThat("Saved search name should be " + this.savedSearchName + " but was " + savedSearchName, savedSearchName.equals(this.savedSearchName));
        assertThat("Results view should be " + this.resultsViewType + " but was " + resultsViewType, resultsViewType.equals(this.resultsViewType));
    }
}
