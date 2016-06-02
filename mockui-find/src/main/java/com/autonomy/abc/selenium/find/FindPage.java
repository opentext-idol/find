package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.*;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FindPage extends AppElement implements AppPage,
        IndexFilter.Filterable,
        DatePickerFilter.Filterable,
        StringDateFilter.Filterable,
        ParametricFilter.Filterable {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private final FindResultsPage results;

    FindPage(WebDriver driver){
        super(new WebDriverWait(driver,30)
                .withMessage("loading Find page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
        results = new FindResultsPage(driver);
    }

    protected FilterPanel filters() {
        return new FilterPanel(new IndexesTree.Factory(), getDriver());
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    public void unhover() {
        /* click somewhere not important to remove hover -
        * clicking the user's username seems safe... */
        getDriver().findElement(By.className("user-username")).click();
        new WebDriverWait(getDriver(),2).until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("popover"))));
    }

    public FindResultsPage getResultsPage() {
        return results;
    }

    @Override
    public IndexesTree indexesTree() {
        return filters().indexesTree();
    }

    public void sortBy(SortBy sortBy) {
        sortDropdown().select(sortBy.toString());
    }

    private Dropdown sortDropdown() {
        WebElement dropdownContainer = findElement(By.cssSelector(".sort-container"));
        return new Dropdown(dropdownContainer, getDriver());
    }

    @Override
    public void filterBy(QueryFilter filter) {
        filter.apply(this);
        results.waitForResultsToLoad();
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
        if (minFindable()) {
            return dateInput(By.cssSelector(".results-filter-min-date input"));
        }
        return dateInput(By.xpath("//div[@data-date-attribute='customMinDate']/descendant::input[@class='form-control']"));
    }

    @Override
    public FormInput untilDateInput() {
        if (maxFindable()){
            return dateInput(By.cssSelector(".results-filter-max-date input"));
        }
        return dateInput(By.xpath("//div[@data-date-attribute='customMaxDate']/descendant::input[@class='form-control']"));
    }

    private Boolean minFindable(){
        return findElements(By.cssSelector(".results-filter-min-date input")).size()>0;
    }

    private Boolean maxFindable(){
        return findElements(By.cssSelector(".results-filter-max-date input")).size()>0;
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
        if (FindResultsPage.DateEnum.CUSTOM.isSelectedInside(results)) {
            FindResultsPage.DateEnum.CUSTOM.findInside(results).click();
            results.waitForResultsToLoad();
        }
    }

    @Override
    public WebElement parametricContainer() {
        WebElement firstParametric = findElement(By.cssSelector("[data-field]"));
        return ElementUtil.ancestor(firstParametric, 2);
    }

    @Override
    public void waitForParametricValuesToLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("parametric-processing-indicator")));
    }

    // this can be used to check whether on the landing page,
    // as opposed to main results page
    public WebElement footerLogo() {
        return findElement(By.className("hp-logo-footer"));
    }

    public Boolean loadingIndicatorExists(){return findElements(By.className("view-server-loading-indicator")).size()>0;}

    public WebElement loadingIndicator(){
        return findElement(By.className("view-server-loading-indicator"));
    }

    public WebElement previewContents(){
        return findElement(By.className("preview-mode-contents"));
    }

    public int totalResultsNum(){return Integer.parseInt(findElement(By.className("total-results-number")).getText());}

    public void openDetailedPreview(){
        findElement(By.className("preview-mode-open-detail-button")).click();
    }

    public void scrollToBottom() {
        findElement(By.className("results-number")).click();
        DriverUtil.scrollToBottom(getDriver());
        results.waitForResultsToLoad();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, FindPage> {
        public FindPage create(WebDriver context) {
            return new FindPage(context);
        }
    }
}
