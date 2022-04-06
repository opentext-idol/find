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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParametricFieldContainer extends ListFilterContainer implements Iterable<FindParametricFilter> {
    private static final Pattern COUNT_START_PATTERN = Pattern.compile(" \\(");
    private static final Pattern PARAMETRIC_COUNT_PATTERN = Pattern.compile("^\\((?:\\d+ / )?(?<count>\\d+)\\)$");

    ParametricFieldContainer(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
    }

    @Override
    public String filterCategoryName() {
        expand();
        return COUNT_START_PATTERN.split(filterCategory().getText())[0];
    }

    public int getFilterCount() {
        final String countInfo = filterCategory().findElement(By.className("parametric-value-counts")).getText();
        final Matcher matcher = PARAMETRIC_COUNT_PATTERN.matcher(countInfo);
        assert matcher.find();
        return Integer.parseInt(matcher.group("count"));
    }

    public List<FindParametricFilter> getFilters() {
        expand();
        final List<WebElement> filters = getContainer().findElements(By.cssSelector(".parametric-value-element:not(.hide)"));
        return filters.stream().map(FindParametricFilter::new).collect(Collectors.toList());
    }

    public List<WebElement> filters() {
        expand();
        return getContainer().findElements(By.cssSelector(".parametric-value-element:not(.hide)"));
    }

    @Override
    public List<String> getFilterNames() {
        final boolean startedCollapsed = isCollapsed();

        // text cannot be read if it is not visible
        if (startedCollapsed) {
            expand();
        }

        final List<String> filterNames = getFilters().stream().map(FindParametricFilter::getName).collect(Collectors.toList());

        // restore collapsed state
        if (startedCollapsed) {
            collapse();
        }

        return filterNames;
    }

    @Override
    public Iterator<FindParametricFilter> iterator() {
        return getFilters().iterator();
    }

    //TODO: expanding reloads the parametric filter container so if have to expand them it will be stale
    public void seeAll() {
        expand();
        getContainer().findElement(By.cssSelector(".show-all")).click();
    }
}
