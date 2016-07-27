package com.autonomy.abc.selenium.find.numericWidgets;

import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
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

    private enum LimitType {
        max,
        min
    }

    public MainNumericWidget(final WebDriver driver) {
        super(driver.findElement(By.className("middle-container-time-bar")),driver);
        this.driver = driver;
        this.container = driver.findElement(By.className("middle-container-time-bar"));
        this.chart = new NumericWidget(driver, container);
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

    public int getRange() {
        noMin();
        noMax();
        return Integer.parseInt(maxFieldValue()) - Integer.parseInt(minFieldValue());
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

    public void selectFractionOfBars(int i, int j) {
        List<WebElement> bars = graphAsWidget().barsWithResults();
        int index = bars.size() * i / j;

        WebElement bar = bars.get(index);
        clickAndDrag(100, 0, bar);
    }

    //Drag and drop not element -> needs to go in DriverUtils in QA infrastructure!!!
    public void clickAndDrag(int x_dest, int y_dest, WebElement startingElement) {
        final Actions action = new Actions(driver);
        action.moveToElement(startingElement);
        action.clickAndHold().build().perform();
        action.moveByOffset(x_dest, y_dest).build().perform();
        action.release().build().perform();
    }

    //Waits
    public void waitUntilWidgetLoaded() {
        new WebDriverWait(driver, 10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("numeric-parametric-loading-indicator")));
    }

    public void waitUntilRectangleBack() {
        new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOf(graphAsWidget().selectionRec()));
    }

    public void waitUntilDatePickerGone() {
        new WebDriverWait(driver, 20).until(calendarPopUpsGone());
    }

    private ExpectedCondition<Boolean> calendarPopUpsGone() {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return findElements(By.cssSelector("div.bootstrap-datetimepicker-widget")).size() < 1;
            }
        };
    }

    public void rectangleHoverRight() {
        final Dimension dimensions = graphAsWidget().selectionRec().getSize();
        hoveringOffSide(graphAsWidget().selectionRec(), (dimensions.getWidth()), dimensions.getHeight() / 100);
    }

    public void rectangleHoverLeft() {
        final Dimension dimensions = graphAsWidget().selectionRec().getSize();
        hoveringOffSide(graphAsWidget().selectionRec(), 0, dimensions.getHeight() / 100);
    }

    //IS GOING TO DRIVERUTILS (also the one from SunburstView)
    private void hoveringOffSide(final WebElement element, final int xOffSet, final int yOffSet) {
        final Actions builder = new Actions(driver);
        builder.moveToElement(element, xOffSet, yOffSet);
        final Action hover = builder.build();
        hover.perform();
    }

    //Getting date field values
    public String minFieldValue() {
        return fieldValue(LimitType.min);
    }

    public String maxFieldValue() {
        return fieldValue(LimitType.max);
    }

    private String fieldValue(LimitType limit) {
        return findElement(By.cssSelector(".numeric-parametric-" + limit.toString() + "-input")).getAttribute("value");
    }

    public List<Date> getDates() {
        List<Date> dates = new ArrayList<>();
        dates.add(parseTheDates(minFieldValue()));
        dates.add(parseTheDates(maxFieldValue()));
        return dates;
    }

    //DATE FORMAT: YYYY-MM-DD hh:mm
    private Date parseTheDates(String stringDate) {
        String date = stringDate.split(" ")[0];
        String[] dateParts = date.split("-");
        return new Date(Integer.parseInt(dateParts[0]) - 1900, Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));
    }


    //Setting date field values
    private WebElement inputBox(LimitType limit) {
        return findElement(By.className("numeric-parametric-" + limit.toString() + "-input"));
    }

    public void setMinValueViaText(String value) {
        inputValue(value, minFieldValue().length(), inputBox(LimitType.min));
    }

    public void setMaxValueViaText(String value) {
        inputValue(value, maxFieldValue().length(), inputBox(LimitType.max));
    }

    //FormInput class is not used because the in-built clear and submit methods don't work w/ these boxes
    private void inputValue(final String term, int length, WebElement inputBox) {
        waitUntilWidgetLoaded();
        for (int i = 0; i < length; i++) {
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

    public DatePicker openCalendar(WebElement dateInput) {
        dateInput.findElement(By.className("hp-calendar")).click();
        return new DatePicker(dateInput, driver);
    }


}
