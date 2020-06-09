/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
