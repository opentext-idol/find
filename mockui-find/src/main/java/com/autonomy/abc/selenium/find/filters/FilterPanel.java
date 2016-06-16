package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.DatePickerFilter;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
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

import static com.hp.autonomy.frontend.selenium.util.CssUtil.cssifyIndex;

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

    public void waitForParametricFields() {
        Container.LEFT.waitForLoad(driver);
    }

    //should check not already selected
    public void clickFirstIndex(){
        panel.findElement(By.cssSelector(".child-categories li:first-child")).click();
    }

    private WebDriver getDriver() {
        return driver;
    }

    private List<FilterContainer> allFilterContainers() {
        List<FilterContainer> nodes = new ArrayList<FilterContainer>(parametricFieldContainers());
        nodes.add(0, indexesTreeContainer());
        nodes.add(1, dateFilterContainer());
        return nodes;
    }

    private FilterContainer indexesTreeContainer() {
        WebElement heading = panel.findElement(By.xpath(".//h4[contains(text(), 'Indexes') or contains(text(), 'Databases')]"));
        WebElement container = ElementUtil.ancestor(heading, 2);
        return new IndexesTreeContainer(container, getDriver());
    }

    private DateFilterContainer dateFilterContainer() {
        WebElement heading = panel.findElement(By.xpath(".//h4[contains(text(), 'Dates')]"));
        WebElement container = ElementUtil.ancestor(heading, 2);
        return new DateFilterContainer(container, getDriver());
    }

    private List<ParametricFieldContainer> parametricFieldContainers() {
        List<ParametricFieldContainer> containers = new ArrayList<>();
        for (WebElement container : getParametricFilters()) {
            containers.add(new ParametricFieldContainer(container, driver));
        }
        return containers;
    }

    private List<WebElement> getParametricFilters() {
        List<WebElement> ancestors = new ArrayList<>();
        for (WebElement element : panel.findElements(By.className("parametric-fields-table"))) {
            ancestors.add(ElementUtil.ancestor(element, 3));
        }
        return ancestors;
    }

    public List<FindParametricCheckbox> checkBoxesForParametricFieldContainer(int i ){
        return parametricField(i).values();
    }

    public ParametricFieldContainer parametricField(int i) {
        return parametricFieldContainers().get(i);
    }

    public int numberParametricFieldContainers(){
        return parametricFieldContainers().size();
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

    public FindParametricCheckbox checkboxForParametricValue(String fieldName, String fieldValue) {
        WebElement checkbox = panel.findElement(By.cssSelector("[data-field='" + fieldName.replace(" ", "_") + "'] [data-value='" + fieldValue.toUpperCase() + "']"));
        return new FindParametricCheckbox(checkbox, getDriver());
    }

    public FindParametricCheckbox checkboxForParametricValue(int fieldIndex, int valueIndex) {
        WebElement checkbox = panel.findElement(By.cssSelector("[data-field]:nth-of-type(" + cssifyIndex(fieldIndex) +") [data-value]:nth-of-type(" + cssifyIndex(valueIndex) + ")"));
        return new FindParametricCheckbox(checkbox, driver);
    }

    public String getErrorMessage() {
        return panel.findElement(By.cssSelector("p:not(.hide)")).getText();
    }

    public void toggleFilter(DateOption filter) {
        dateFilterContainer().toggleFilter(filter);
    }

    public DatePickerFilter.Filterable datePickerFilterable() {
        return dateFilterContainer();
    }

    public StringDateFilter.Filterable stringDateFilterable() {
        return dateFilterContainer();
    }

    //toggling see more
    public void showFilters() {
        for (WebElement element : panel.findElements(By.className("toggle-more-text"))) {
                element.click();
        }
    }

    public void expandFiltersFully() {
        waitForIndexes();
        for (Collapsible collapsible : allFilterContainers()) {
            collapsible.expand();
        }
        showFilters();
    }

    public void collapseAll() {
        for (Collapsible collapsible : allFilterContainers()) {
            collapsible.collapse();
        }
    }


}
