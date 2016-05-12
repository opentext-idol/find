package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.*;
import com.google.common.collect.Iterables;
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

    protected FindPage(WebDriver driver){
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
        if (!results.isDateSelected(FindResultsPage.DateEnum.CUSTOM)) {
            results.toggleDateSelection(FindResultsPage.DateEnum.CUSTOM);
        }
    }

    @Override
    public WebElement parametricContainer() {
        return findElement(By.className("parametric-container"));
    }

    public Boolean parametricEmptyExists(){
        return findElements(By.className("parametric-empty")).size()!=0;
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

    //should check not already selected
    public void clickFirstIndex(){
        findElement(By.cssSelector(".child-categories li:first-child")).click();
    }

    public String getIthIndex(int i){return Iterables.get(indexesTree(),i).getName();}

    public void seeMoreOfCategory(WebElement element){element.findElement(By.className("toggle-more")).click();}

    public void openDetailedPreview(){
        findElement(By.className("preview-mode-open-detail-button")).click();
    }

    public WebElement rightContainerToggleButton() {
        return findElement(By.cssSelector(".right-container-icon .container-toggle"));
    }

    public WebElement leftContainerToggleButton(){
        return findElement(By.cssSelector(".left-side-container .container-toggle"));
    }

    public void scrollToBottom() {
        findElement(By.className("results-number")).click();
        DriverUtil.scrollToBottom(getDriver());
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
    }

    public static class Factory implements ParametrizedFactory<WebDriver, FindPage> {
        public FindPage create(WebDriver context) {
            return new FindPage(context);
        }
    }
}
