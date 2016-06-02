package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.find.FindIndexCategoryNode;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterPanel {
    private final WebElement panel;
    private final WebDriver driver;
    private final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory;

    public FilterPanel(ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory, WebDriver driver) {
        this.indexesTreeFactory = indexesTreeFactory;
        this.driver = driver;
        this.panel = Container.LEFT.findUsing(driver);
    }

    public IndexesTree indexesTree() {
        return indexesTreeFactory.create(new FindIndexCategoryNode(panel.findElement(By.cssSelector(".databases-list [data-category-id='all']")), getDriver()));
    }

    public Index getIndex(int i) {
        return indexesTree().allIndexes().getIndex(i);
    }

    /**
     * waits until the list of indexes has been retrieved
     * from HOD if necessary
     */
    public void waitForIndexes() {
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("not-loading")));
    }

    public boolean parametricEmptyExists() {
        return !panel.findElements(By.className("parametric-empty")).isEmpty();
    }

    //should check not already selected
    public void clickFirstIndex(){
        panel.findElement(By.cssSelector(".child-categories li:first-child")).click();
    }

    public void seeMoreOfCategory(WebElement element){element.findElement(By.className("toggle-more")).click();}

    private WebDriver getDriver() {
        return driver;
    }

    private ParametricFilterTree parametricFilterTree() {
        return new ParametricFilterTree(panel, getParametricFilters(), getDriver());
    }

    private DateFilterTree dateFilterTree() {
        return new DateFilterTree(ElementUtil.ancestor(getDateFilter(), 2), getDriver());
    }

    private DatabaseFilterTree databaseFilterTree() {
        return new DatabaseFilterTree(ElementUtil.ancestor(getDatabaseFilter(), 2), getDriver());
    }

    private WebElement getDatabaseFilter() {
        return panel.findElement(By.xpath(".//h4[contains(text(),'Databases')]"));
    }

    private WebElement getDateFilter() {
        return panel.findElement(By.xpath(".//h4[contains(text(),'Dates')]"));
    }

    private List<WebElement> getParametricFilters() {
        List<WebElement> ancestors = new ArrayList<>();
        for (WebElement element : panel.findElements(By.className("parametric-fields-table"))) {
            ancestors.add(ElementUtil.ancestor(element, 3));
        }
        return ancestors;
    }

    public void filterResults(String term) {
        FormInput input = new FormInput(panel.findElement(By.cssSelector("[placeholder='Search filters...']")), getDriver());
        input.clear();
        input.setAndSubmit(term);
    }

    public void clearFilter() {
        FormInput input = new FormInput(panel.findElement(By.cssSelector("[placeholder='Search filters...']")), getDriver());
        input.clear();
        waitForIndexes();
    }

    public boolean parametricFilterExists(String filter) {
        return panel.findElements(By.cssSelector(".parametric-value-element[data-value='" + filter + "']")).size() > 0;
    }

    //TODO: make this use the filter trees
    private WebElement findFilter(String name) {
        return panel.findElement(By.xpath(".//*[contains(text(),'" + name + "')]"));
    }

    public boolean filterVisible(String filter) {
        return findFilter(filter).isDisplayed();
    }

    public boolean noneMatchingMessageVisible() {
        return panel.findElement(By.xpath(".//p[contains(text(),'No filters matched')]")).isDisplayed();
    }

    public List<WebElement> getCurrentFilters() {
        List<WebElement> currentFilters = new ArrayList<>();
        currentFilters.addAll(databaseFilterTree().getAllFiltersInTree());
        currentFilters.addAll(dateFilterTree().getAllFiltersInTree());
        currentFilters.addAll(parametricFilterTree().getAllFiltersInTree());
        return currentFilters;
    }

    private List<String> getVisibleFilterTypes() {
        List<WebElement> elements = new ArrayList<>();

        elements.addAll(databaseFilterTree().getFilterTypes());
        elements.addAll(dateFilterTree().getFilterTypes());
        elements.addAll(parametricFilterTree().getFilterTypes());

        return ElementUtil.getTexts(elements);
    }

    public List<String> findFilterString(String targetFilter, List<WebElement> allFilters) {
        waitForIndexes();
        Set<String> matchingFilters = new HashSet<>();

        for (WebElement filter : allFilters) {
            if (StringUtils.containsIgnoreCase(filter.getText(), targetFilter)) {
                matchingFilters.add(filter.getText());

                if (getVisibleFilterTypes().contains(filter.getText())) {
                    matchingFilters.addAll(new FilterNode(ElementUtil.ancestor(filter, 2), getDriver()).getChildNames());
                }
                //is child
                else {
                    matchingFilters.add(new FilterNode(filter, getDriver()).getParentName());
                }
            }
        }
        return new ArrayList<>(matchingFilters);
    }

    //toggling see more
    public void showFilters() {
        for (WebElement element : panel.findElements(By.className("toggle-more-text"))) {
            if (!element.getText().equals("See Less")) {
                element.click();
            }
        }
    }

    public void expandFiltersFully() {
        waitForIndexes();
        expandAll();
        showFilters();
    }

    private void expandAll() {
        databaseFilterTree().expandAll();
        dateFilterTree().expandAll();
        parametricFilterTree().expandAll();
    }

    public void collapseAll() {
        databaseFilterTree().collapseAll();
        dateFilterTree().collapseAll();
        parametricFilterTree().collapseAll();
    }

}
