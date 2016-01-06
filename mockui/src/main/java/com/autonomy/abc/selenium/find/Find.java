package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.indexes.tree.FindIndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.search.DatePickerFilter;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchFilter;
import com.autonomy.abc.selenium.search.StringDateFilter;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class Find extends AppElement implements AppPage,
        IndexFilter.Filterable,
        DatePickerFilter.Filterable,
        StringDateFilter.Filterable {
    private final FormInput input;
    private final FindResultsPage results;

    public Find(WebDriver driver){
        super(new WebDriverWait(driver,30)
                .withMessage("loading Find page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
        input = new FormInput(driver.findElement(By.className("find-input")), driver);
        results = new FindResultsPage(driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    public FindResultsPage getResultsPage() {
        return results;
    }

    public String getSearchBoxTerm(){
        return input.getValue();
    }

    public void search(String searchTerm){
        input.clear();
        input.setAndSubmit(searchTerm);
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
    }

    public List<String> getSelectedPublicIndexes() {
        List<String> indexes = new ArrayList<>();

        for(WebElement selectedIndex : findElements(By.cssSelector("[data-category-id='public'] .icon-ok.database-icon"))){
            indexes.add(ElementUtil.ancestor(selectedIndex, 2).findElement(By.xpath("./span[@class='database-name' or @class='category-name']")).getText());
        }

        return indexes;
    }

    @Override
    public IndexesTree indexesTree() {
        return new IndexesTree(new FindIndexCategoryNode(findElement(By.cssSelector(".databases-list [data-category-id='all']")), getDriver()));
    }

    public void sortBy(SearchBase.Sort sort) {
        sortDropdown().select(sort.toString());
    }

    private Dropdown sortDropdown() {
        WebElement dropdownContainer = findElement(By.cssSelector(".sort-container"));
        return new Dropdown(dropdownContainer, getDriver());
    }

    public List<String> getPrivateIndexNames() {
        List<String> names = new ArrayList<>();
        indexesTree().privateIndexes().expand();
        for (IndexNodeElement element : indexesTree().privateIndexes()) {
            names.add(element.getName());
        }
        return names;
    }

    @Override
    public void filterBy(SearchFilter filter) {
        filter.apply(this);
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
    }

    @Override
    public DatePicker fromDatePicker() {
        return datePicker(By.className("results-filter-min-date"));
    }

    @Override
    public DatePicker untilDatePicker() {
        return datePicker(By.className("results-filter-max-date"));
    }

    private DatePicker datePicker(By locator) {
        return new DatePicker(findElement(locator), getDriver());
    }

    @Override
    public FormInput fromDateInput() {
        return dateInput(By.cssSelector(".results-filter-min-date input"));
    }

    @Override
    public FormInput untilDateInput() {
        return dateInput(By.cssSelector(".results-filter-max-date input"));
    }

    private FormInput dateInput(By locator) {
        return new FormInput(findElement(locator), getDriver());
    }
}
