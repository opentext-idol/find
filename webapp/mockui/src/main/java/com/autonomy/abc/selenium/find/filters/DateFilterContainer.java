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

class DateFilterContainer extends ListFilterContainer implements DatePickerFilter.Filterable, StringDateFilter.Filterable {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private final WebDriver driver;

    DateFilterContainer(final WebElement element, final WebDriver webDriver){
        super(element,webDriver);
        driver = webDriver;
    }

    public List<WebElement> getFilters(){
        return getContainer().findElements(By.cssSelector("[data-filter-id] > td:nth-child(2)"));
    }

    @Override
    public List<String> getFilterNames(){
        return ElementUtil.getTexts(getFilters());
    }

    private boolean isFilteringBy(final DateOption filter) {
        final WebElement checkIcon = findDateFilter(filter).findElement(By.tagName("i"));
        return !ElementUtil.hasClass("hide", checkIcon);
    }

    void toggleFilter(final DateOption filter) {
        findDateFilter(filter).click();
    }

    private WebElement findDateFilter(final DateOption filter) {
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

    private DatePicker datePicker(final int nthOfType) {
        showCustomDateBoxes();
        final WebElement formGroup = getContainer().findElement(By.cssSelector(".search-dates-wrapper .form-group:nth-of-type(" + nthOfType + ')'));
        return new DatePicker(formGroup, driver);
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
    public String formatInputDate(final Date date) {
        return FORMAT.format(date);
    }

    private FormInput dateInput(final int nthOfType) {
        showCustomDateBoxes();
        final WebElement inputBox = getContainer().findElement(By.cssSelector(".search-dates-wrapper .form-group:nth-of-type(" + nthOfType + ") input"));
        return new FormInput(inputBox, driver);
    }

    private void showCustomDateBoxes() {
        if (!isFilteringBy(DateOption.CUSTOM)) {
            toggleFilter(DateOption.CUSTOM);
        }
    }
}
