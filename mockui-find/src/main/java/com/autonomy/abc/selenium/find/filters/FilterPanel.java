package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.find.FindIndexCategoryNode;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

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

    //should check not already selected
    public void clickFirstIndex(){
        panel.findElement(By.cssSelector(".child-categories li:first-child")).click();
    }

    private WebDriver getDriver() {
        return driver;
    }

    private List<FilterNode> parametricFieldContainers() {
        List<FilterNode> containers = new ArrayList<>();
        for (WebElement container : getParametricFilters()) {
            containers.add(new ParametricFieldContainer(container, driver));
        }
        return containers;
    }

    private FilterNode dateFilterContainer() {
        return new DateFilterContainer(ElementUtil.ancestor(getDateFilter(), 2), getDriver());
    }

    private FilterNode indexesTreeContainer() {
        return new IndexesTreeContainer(ElementUtil.ancestor(getIndexFilter(), 2), getDriver());
    }

    private WebElement getIndexFilter() {
        return panel.findElement(By.xpath(".//h4[contains(text(), 'Indexes') or contains(text(), 'Databases')]"));
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

    public String getFirstHiddenFieldValue() {
        return panel.findElement(By.cssSelector(".parametric-value-element.hide")).getAttribute("data-value");
    }

    public WebElement parametricValue(String dataValue) {
        return panel.findElement(By.cssSelector(".parametric-value-element[data-value='" + dataValue + "']"));
    }

    public String getErrorMessage() {
        return panel.findElement(By.cssSelector("p:not(.hide)")).getText();
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
        indexesTreeContainer().expand();
        dateFilterContainer().expand();
        for (FilterNode parametricField : parametricFieldContainers()) {
            parametricField.expand();
        }
    }

    public void collapseAll() {
        indexesTreeContainer().collapse();
        dateFilterContainer().collapse();
        for (FilterNode parametricField : parametricFieldContainers()) {
            parametricField.collapse();
        }
    }

    public FilterNode parametricField(int i) {
        return parametricFieldContainers().get(i);
    }
}
