/*
 * Copyright 2016-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.DatePickerFilter;
import com.autonomy.abc.selenium.query.StringDateFilter.Filterable;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.hp.autonomy.frontend.selenium.util.CssUtil.cssifyIndex;

public class FilterPanel {
    private final WebElement panel;
    private final WebDriver driver;
    private final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory;

    public FilterPanel(final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory, final WebDriver driver) {
        this.indexesTreeFactory = indexesTreeFactory;
        this.driver = driver;
        panel = Container.LEFT.findUsing(driver).findElement(By.cssSelector(".left-side-filters-view-section"));
    }

    //INDEX/DATABASE RELATED
    public IndexesTree indexesTree() {
        return indexesTreeFactory.create(new FindIndexCategoryNode(panel.findElement(By.cssSelector(".databases-list [data-category-id='all']")), driver));
    }

    public Index getIndex(final int i) {
        indexesTreeContainer().expand();
        return indexesTree().allIndexes().getIndex(i);
    }

    public void waitForIndexes() {
        new WebDriverWait(driver, 10).until(webDriver ->
                ElementUtil.hasClass("hide", panel.findElement(By.cssSelector(".no-active-databases"))));
    }

    public IndexesTreeContainer indexesTreeContainer() {
        final WebElement container = panel.findElement(By.xpath(".//div[contains(div/@class, 'collapsible-header') and (contains(div/h4/span/text(), 'Indexes') or contains(div/h4/span/text(), 'Databases'))]"));
        return new IndexesTreeContainer(container, driver);
    }

    public DateFilterContainer dateFilterContainer() {
        final WebElement container = panel.findElement(By.xpath(".//div[contains(div/@class, 'collapsible-header') and contains(div/h4/span/text(), 'Dates')]"));
        return new DateFilterContainer(container, driver);
    }

    public List<ParametricFieldContainer> parametricFieldContainers() {
        final List<ParametricFieldContainer> containers = new ArrayList<>();
        for(final WebElement container : getParametricFilters()) {
            containers.add(new ParametricFieldContainer(container, driver));
        }
        return containers;
    }

    private Iterable<WebElement> getParametricFilters() {
        return panel.findElements(By.cssSelector("[data-field-display-name][data-field]"));
    }

    public ParametricFieldContainer parametricContainerOfFilter(final String filter) {
        final WebElement field = panel.findElement(By.cssSelector(".parametric-value-element[data-value='" + filter + "']"));
        return new ParametricFieldContainer(ElementUtil.ancestor(field, 5), driver);
    }

    public ParametricFieldContainer parametricContainer(final String filterCategory) {
        final WebElement category = panel.findElement(By.cssSelector("[data-field-display-name='" + filterCategory + "']"));
        return new ParametricFieldContainer(category, driver);
    }

    public boolean parametricContainerIsPresent(final String filterCategory) {
        return !panel.findElements(By.cssSelector("[data-field-display-name='" + filterCategory + "']")).isEmpty();
    }

    public ParametricFieldContainer parametricField(final int i) {
        return parametricFieldContainers().get(i);
    }

    public int nonZeroParamFieldContainer(final int n) {
        return nthParametricThatSatisfiedCondition(n, x -> 0 != x);
    }

    public int nthParametricThatSatisfiedCondition(final int n, final Predicate<Integer> op) {
        int index = 0;
        int nonZeroCount = 0;
        for(final WebElement container : getParametricFilters()) {
            final ParametricFieldContainer candidate = new ParametricFieldContainer(container, driver);
            if(op.test(candidate.getFilterCount())) {
                if(nonZeroCount >= n) {
                    return index;
                } else {
                    nonZeroCount++;
                }
            }
            index++;
        }
        return -1;
    }

    public String formattedNameOfNonZeroField(final int n) {
        return WordUtils.capitalize(parametricField(nonZeroParamFieldContainer(n))
                                            .filterCategoryName()
                                            .toLowerCase());
    }

    //DATE SPECIFIC
    public void toggleFilter(final DateOption filter) {
        dateFilterContainer().toggleFilter(filter);
    }

    public DatePickerFilter.Filterable datePickerFilterable() {
        return dateFilterContainer();
    }

    public Filterable stringDateFilterable() {
        return dateFilterContainer();
    }

    //CHECKBOXES
    public List<FindParametricFilter> checkBoxesForParametricFieldContainer(final int i) {
        final int index = nonZeroParamFieldContainer(i);
        final ParametricFieldContainer container = parametricField(index);
        container.expand();
        return container.getFilters();
    }

    public FindParametricFilter checkboxForParametricValue(final String fieldName, final String fieldValue) {
        final ParametricFieldContainer container = new ParametricFieldContainer(panel.findElement(By.cssSelector("[data-field-display-name='" + fieldName + "']")), driver);
        container.expand();
        return new FindParametricFilter(container.getContainer().findElement(By.cssSelector("[data-value='" + fieldValue.toUpperCase() + "']")));
    }

    public FindParametricFilter checkboxForParametricValue(final int fieldIndex, final int valueIndex) {
        final ParametricFieldContainer container = new ParametricFieldContainer(panel.findElements(By.cssSelector("[data-field]")).get(fieldIndex), driver);
        container.expand();
        return new FindParametricFilter(container.getContainer().findElement(By.cssSelector("[data-value]:nth-of-type(" + cssifyIndex(valueIndex) + ')')));
    }

    //EXPANDING AND COLLAPSING
    public List<FilterContainer> allFilterContainers() {
        final List<FilterContainer> nodes = new ArrayList<>();
        nodes.add(indexesTreeContainer());
        nodes.add(dateFilterContainer());
        nodes.addAll(parametricFieldContainers());
        return nodes;
    }

    public void collapseAll() {
        allFilterContainers().forEach(Collapsible::collapse);
    }

    //OTHER
    public String getErrorMessage() {
        return panel.findElement(By.cssSelector("p:not(.hide)")).getText();
    }

    public void waitForParametricFields() {
        Container.LEFT.waitForLoad(driver);
    }

    protected WebElement getPanel() {
        return panel;
    }

    public List<WebElement> toolTips() {
        return panel.findElements(By.cssSelector("[aria-describedby^='tooltip']"));
    }

    public boolean containerContainsFilter(final String target, final int index) {
        final int tooManyFiltersToBother = 600;

        final ParametricFieldContainer container = parametricField(index);
        if(container.getFilterCount() > tooManyFiltersToBother) {
            return true;
        }
        container.expand();
        container.seeAll();

        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(driver);

        final List<WebElement> filters = filterModal.activePaneFilterList();

        for(final WebElement filter : filters) {
            final String name = filter.findElement(By.cssSelector(".field-value")).getText();
            if(name.contains(target)) {
                filterModal.cancel();
                return true;
            }
        }
        filterModal.cancel();
        return false;
    }
}
