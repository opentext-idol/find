/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

package com.autonomy.abc.selenium.find.numericWidgets;

import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NumericWidgetService {
    private final BIIdolFindElementFactory elementFactory;
    private final BIIdolFind find;
    private final IdolFindPage findPage;

    public NumericWidgetService(final BIIdolFind find) {
        elementFactory = find.elementFactory();
        this.find = find;
        findPage = elementFactory.getFindPage();
    }

    public MainNumericWidget waitForReload() {
        elementFactory.getFilterPanel().waitForParametricFields();
        final MainNumericWidget mainGraph = findPage.mainGraph();
        mainGraph.waitUntilWidgetLoaded();
        return mainGraph;
    }

    public String selectFilterGraph(final GraphFilterContainer container, final WebDriver driver) {
        container.expand();
        final String graphTitle = container.filterCategoryName();
        new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                return !container.isCollapsed();
            }
        });
        container.graph().click();
        return graphTitle;
    }

    public MainNumericWidget searchAndSelectNthGraph(final int n, final String searchTerm, final WebDriver driver) {
        final IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getNthGraph(n), driver);

        return findPage.mainGraph();
    }

    public MainNumericWidget searchAndSelectFirstNumericGraph(final String searchTerm, final WebDriver driver) {
        final IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getFirstNumericGraph(), driver);

        return findPage.mainGraph();
    }

    public MainNumericWidget searchAndSelectFirstDateGraph(final String searchTerm, final WebDriver driver) {
        final IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getFirstDateGraph(), driver);
        return findPage.mainGraph();
    }

    private IdolFilterPanel searchAndReturnFilterPanel(final String searchTerm) {
        find.findService().searchAnyView(searchTerm);
        final IdolFilterPanel filterPanel = elementFactory.getFilterPanel();
        filterPanel.waitForParametricFields();
        return filterPanel;
    }
}
