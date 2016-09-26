/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.Container;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;

public class AppliedFiltersPanel {
    private final WebElement panel;
    private final WebDriver driver;

    public AppliedFiltersPanel(final WebDriver driver) {
        this.driver = driver;
        this.panel = Container.LEFT.findUsing(driver).findElement(By.cssSelector(".left-side-applied-filters-view-container"));
    }

    public ArrayList<WebElement> getAppliedFilters() {
        return Lists.newArrayList(panel.findElements(By.cssSelector(".filters-labels .filter-label")).iterator());
    }

    public WebElement getHeader() {
        return panel.findElement(By.cssSelector(".header-left-side-applied-filters-view"));
    }

    public WebElement getPanel() {
        return panel;
    }
}
