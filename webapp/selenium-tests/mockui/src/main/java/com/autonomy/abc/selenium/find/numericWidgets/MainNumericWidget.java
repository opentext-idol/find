/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.selenium.find.numericWidgets;

import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainNumericWidget extends AppElement {
    private final WebElement container;
    private final WebDriver driver;
    private final NumericWidget chart;

    public MainNumericWidget(final WebDriver driver) {
        super(driver.findElement(By.className("middle-container-time-bar")), driver);
        this.driver = driver;
        container = driver.findElement(By.className("middle-container-time-bar"));
        chart = new NumericWidget(driver, container);
    }

    //Around the graph/chart
    public void closeWidget() {
        findElement(By.cssSelector(".hp-close.time-bar-container-icon")).click();
    }

    public void reset() {
        findElement(By.className("numeric-parametric-reset")).click();
    }

    public void noMin() {
        findElement(By.className("numeric-parametric-no-min")).click();
    }

    public void noMax() {
        findElement(By.className("numeric-parametric-no-max")).click();
    }

    public double setAndGetFullRange() {
        noMin();
        noMax();
        return getRange();
    }

    public double getRange() {
        return Double.parseDouble(maxFieldValue()) - Double.parseDouble(minFieldValue());
    }

    public WebElement errorMessage() {
        return findElement(By.className("numeric-parametric-error-text"));
    }

    public String hoverMessage() {
        return findElement(By.className("numeric-parametric-co-ordinates")).getText();
    }

    public WebElement messageRow() {
        return findElement(By.cssSelector(".numeric-parametric-inputs"));
    }

    public String header() {
        return findElement(By.className("time-bar-header")).getText();
    }

    //Actual graph
    public WebElement graph() {
        return chart.getContainer();
    }

    public NumericWidget graphAsWidget() {
        return chart;
    }

    public int graphWidth() {
        return Integer.parseInt(graph().getAttribute("width"));
    }

    public void selectHalfTheBars() {
        selectFractionOfBars(1, 2);
    }

    public void selectFractionOfBars(final int i, final int j) {
        final List<WebElement> bars = graphAsWidget().barsWithResults();
        final int index = bars.size() * i / j;

        final WebElement bar = bars.get(index);
        DriverUtil.clickAndDrag(100, bar, driver);
    }

    //Waits
    public void waitUntilWidgetLoaded() {
        new WebDriverWait(driver, 10)
            .until(ExpectedConditions.invisibilityOfElementLocated(By.className("numeric-parametric-loading-indicator")));
    }

    public void waitUntilRectangleBack() {
        new WebDriverWait(driver, 20)
            .until(ExpectedConditions.visibilityOf(graphAsWidget().selectionRec()));
    }

    public void waitUntilDatePickerGone() {
        new WebDriverWait(driver, 20).until(calendarPopUpsGone());
    }

    private ExpectedCondition<Boolean> calendarPopUpsGone() {
        return (WebDriver webdriver) -> findElements(By.cssSelector("div.bootstrap-datetimepicker-widget")).size() < 1;
    }

    public void rectangleHoverRight() {
        final Dimension dimensions = graphAsWidget().selectionRec().getSize();
        DriverUtil.hoveringOffSide(graphAsWidget().selectionRec(),
                                   new Point(dimensions.getWidth(), dimensions.getHeight() / 100),
                                   driver);
    }

    public void rectangleHoverLeft() {
        final Dimension dimensions = graphAsWidget().selectionRec().getSize();
        DriverUtil.hoveringOffSide(graphAsWidget().selectionRec(), new Point(0, dimensions.getHeight() / 100), driver);
    }

    //Getting date field values
    public String minFieldValue() {
        return fieldValue(LimitType.min);
    }

    public String maxFieldValue() {
        return fieldValue(LimitType.max);
    }

    private String fieldValue(final LimitType limit) {
        return findElement(By.cssSelector(".numeric-parametric-" + limit + "-input")).getAttribute("value");
    }

    public List<Date> getDates() {
        final List<Date> dates = new ArrayList<>();
        dates.add(parseTheDates(minFieldValue()));
        dates.add(parseTheDates(maxFieldValue()));
        return dates;
    }

    //DATE FORMAT: YYYY-MM-DD hh:mm
    private Date parseTheDates(final String stringDate) {
        final String date = stringDate.split(" ")[0];
        final String[] dateParts = date.split("-");
        return new Date(Integer.parseInt(dateParts[0]) - 1900, Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));
    }

    //Setting date field values
    private WebElement inputBox(final LimitType limit) {
        return findElement(By.className("numeric-parametric-" + limit + "-input"));
    }

    public void setMinValueViaText(final String value) {
        inputValue(value, minFieldValue().length(), inputBox(LimitType.min));
    }

    public void setMaxValueViaText(final String value) {
        inputValue(value, maxFieldValue().length(), inputBox(LimitType.max));
    }

    //FormInput class is not used because the in-built clear and submit methods don't work w/ these boxes
    private void inputValue(final String term, final int length, final WebElement inputBox) {
        waitUntilWidgetLoaded();
        for(int i = 0; i < length; i++) {
            inputBox.sendKeys(Keys.BACK_SPACE);
        }
        inputBox.sendKeys(term);
        inputBox.sendKeys(Keys.ENTER);
        waitUntilWidgetLoaded();
    }

    //Setting date via calendars
    public WebElement startCalendar() {
        return findElement(By.cssSelector(".input-group[data-date-attribute='min-date']"));
    }

    public WebElement endCalendar() {
        return findElement(By.cssSelector(".input-group[data-date-attribute='max-date']"));
    }

    public DatePicker openCalendar(final WebElement dateInput) {
        dateInput.findElement(By.className("hp-calendar")).click();
        new WebDriverWait(getDriver(), 3)
            .until((ExpectedCondition<Boolean>)driver -> calendarHasOpened());
        return new DatePicker(dateInput, driver);
    }

    public Boolean calendarHasOpened() {
        return !findElements(By.cssSelector(".datepicker-days .picker-switch")).isEmpty();
    }

    private enum LimitType {
        max,
        min
    }
}
