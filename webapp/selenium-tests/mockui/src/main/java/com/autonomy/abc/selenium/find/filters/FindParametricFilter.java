/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.CheckboxBase;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FindParametricFilter extends CheckboxBase {
    private final WebElement element;

    FindParametricFilter(final WebElement element) {
        this.element = element;
    }

    public String getName() {
        return name().getText().trim();
    }

    public WebElement name() {return element.findElement(By.className("parametric-value-text")); }

    @Override
    public boolean isChecked() {
        return !ElementUtil.hasClass("hide", element.findElement(By.className("hp-check")));
    }

    @Override
    public void toggle() {
        element.click();
    }

    public int getResultsCount() {
        return getResultCount(By.className("parametric-value-count-cell"));
    }

    private int getResultCount(final By by) {
        final String spanResultCount = element.findElement(by).getText().split("\\(")[1];
        return Integer.parseInt(spanResultCount.substring(0, spanResultCount.length() - 1));
    }
}
