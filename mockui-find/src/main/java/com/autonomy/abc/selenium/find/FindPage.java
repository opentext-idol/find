package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.*;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FindPage extends AppElement implements AppPage,
        IndexFilter.Filterable,
        DatePickerFilter.Filterable,
        StringDateFilter.Filterable,
        ParametricFilter.Filterable {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private final FindResultsPage results;

    private FindPage(WebDriver driver){
        super(new WebDriverWait(driver,30)
                .withMessage("loading Find page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
        results = new FindResultsPage(driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    public FindResultsPage getResultsPage() {
        return results;
    }

    /**
     * waits until the list of indexes has been retrieved
     * from HOD if necessary
     */
    void waitForIndexes() {
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("not-loading")));
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

    public void sortBy(SortBy sortBy) {
        sortDropdown().select(sortBy.toString());
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
    public void filterBy(QueryFilter filter) {
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
        showCustomDateBoxes();
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

    @Override
    public String formatInputDate(Date date) {
        return FORMAT.format(date);
    }

    private FormInput dateInput(By locator) {
        showCustomDateBoxes();
        return new FormInput(findElement(locator), getDriver());
    }

    private void showCustomDateBoxes() {
        if (!results.isDateSelected(FindResultsPage.DateEnum.CUSTOM)) {
            results.toggleDateSelection(FindResultsPage.DateEnum.CUSTOM);
        }
    }

    @Override
    public WebElement parametricContainer() {
        return findElement(By.className("parametric-container"));
    }

    @Override
    public void waitForParametricValuesToLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("parametric-processing-indicator")));
    }

    // this can be used to check whether on the landing page,
    // as opposed to main reuslts page
    public WebElement footerLogo() {
        return findElement(By.className("hp-logo-footer"));
    }

    public WebElement rightContainerToggleButton() {
        return findElement(By.cssSelector(".right-container-icon .container-toggle"));
    }

    public WebElement leftContainerToggleButton(){
        return findElement(By.cssSelector(".left-side-container .container-toggle"));
    }

    public WebElement indexElement(Index index) {
        return ElementUtil.ancestor(getResultsPage().resultsDiv().findElement(By.xpath("//*[@class='database-name' and text()='" + index.getDisplayName() + "']")), 2);
    }

    public static class Factory implements ParametrizedFactory<WebDriver, FindPage> {
        public FindPage create(WebDriver context) {
            return new FindPage(context);
        }
    }
}
