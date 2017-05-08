/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.save;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchTab {
    private final WebElement tab;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    SearchTab(final WebElement tab) {
        this.tab = tab;
    }

    public String getTitle() {
        return tab.findElement(By.className("search-tab-title")).getText();
    }

    public boolean isNew() {
        return tab.findElement(By.cssSelector(".search-tab-title .hp-new")).isDisplayed();
    }

    void activate() {
        tab.findElement(By.tagName("a")).click();
    }

    public SearchType getType() {
        return SearchType.valueOf(tab.findElement(By.cssSelector(".hp-icon:not(.hide):not(.hp-new)")).getAttribute("data-original-title"));
    }

    public WebElement getTab() {
        return tab;
    }
}
