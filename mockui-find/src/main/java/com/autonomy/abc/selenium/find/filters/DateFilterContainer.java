package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.query.DatePickerFilter;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class DateFilterContainer extends FilterContainer implements DatePickerFilter.Filterable, StringDateFilter.Filterable {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private final WebDriver driver;

    DateFilterContainer(WebElement element, WebDriver webDriver){
        super(element,webDriver);
        driver = webDriver;
    }

    public List<WebElement> getChildren(){
        return getContainer().findElements(By.cssSelector("[data-filter-id] > td:nth-child(2)"));
    }

    @Override
    public List<String> getChildNames(){
        return ElementUtil.getTexts(getChildren());
    }

    private boolean isFilteringBy(DateOption filter) {
        WebElement checkIcon = findDateFilter(filter).findElement(By.tagName("i"));
        return !ElementUtil.hasClass("hide", checkIcon);
    }

    void toggleFilter(DateOption filter) {
        findDateFilter(filter).click();
    }

    private WebElement findDateFilter(DateOption filter) {
        return getContainer().findElement(By.cssSelector("[data-filter-id='" + filter + "']"));
    }

    @Override
    public DatePicker fromDatePicker() {
        return datePicker(1);
    }

    @Override
    public DatePicker untilDatePicker() {
        return datePicker(2);
    }

    private DatePicker datePicker(int nthOfType) {
        showCustomDateBoxes();
        WebElement formGroup = getContainer().findElement(By.cssSelector(".search-dates-wrapper .form-group:nth-of-type(" + nthOfType + ")"));
        return new DatePicker(formGroup, getDriver());
    }

    @Override
    public FormInput fromDateInput() {
        return dateInput(1);
    }

    @Override
    public FormInput untilDateInput() {
        return dateInput(2);
    }

    @Override
    public String formatInputDate(Date date) {
        return FORMAT.format(date);
    }

    private FormInput dateInput(int nthOfType) {
        showCustomDateBoxes();
        WebElement inputBox = getContainer().findElement(By.cssSelector(".search-dates-wrapper .form-group:nth-of-type(" + nthOfType + ") input"));
        return new FormInput(inputBox, getDriver());
    }

    private void showCustomDateBoxes() {
        if (!isFilteringBy(DateOption.CUSTOM)) {
            toggleFilter(DateOption.CUSTOM);
        }
    }

    private WebDriver getDriver() {
        return driver;
    }
}
