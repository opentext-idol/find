package com.autonomy.abc.selenium.find.bi;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableView  extends ParametricFieldView {

    private static final Pattern SHOWING_ENTRIES = Pattern.compile("Showing (?<from>[0-9,]+) to (?<to>[0-9,]+) of (?<entries>[0-9,]+) entries");

    public TableView(final WebDriver driver) {
        super(driver);
    }

    //Display
    public boolean tableVisible(){
        return findElement(By.cssSelector("table.dataTable")).isDisplayed();
    }

    public void waitForTable(){
        new WebDriverWait(getDriver(), 15).until(ExpectedConditions.invisibilityOfElementLocated(By.className("parametric-loading")));
    }

    public int columnCount() {
        return findElements(By.cssSelector("table.dataTable:not(.DTFC_Cloned) th")).size();
    }

    public int rowCount() {
        return findElements(By.cssSelector("table.dataTable:not(.DTFC_Cloned) td:first-child")).size();
    }

    public int totalRows() {
        return getIntValue(getEntriesGroup("entries"));
    }

    public int minRow() {
        return getIntValue(getEntriesGroup("from"));
    }

    public int maxRow() {
        return getIntValue(getEntriesGroup("to"));
    }

    public void nextPage() {
        paginate(findElement(By.cssSelector(".dataTables_paginate .next a")));
    }

    public void previousPage() {
        paginate(findElement(By.cssSelector(".dataTables_paginate .previous a")));
    }

    public int currentPage() {
        return getIntValue(findElement(By.cssSelector(".dataTables_paginate .active a")).getText());
    }

    public String text(final int row, final int column) {
        final WebElement tr = findElements(By.cssSelector("table.dataTable:not(.DTFC_Cloned) tr")).get(row);
        final WebElement td = tr.findElements(By.tagName("td")).get(column);

        return td.getText();
    }

    public void sort(final int columnIndex, final SortDirection sortDirection) {
        final WebElement headerRow = findElements(By.cssSelector("table.dataTable:not(.DTFC_Cloned) tr")).get(0);
        final WebElement th = headerRow.findElements(By.tagName("th")).get(columnIndex);

        // if table already sorted, no work to do
        if (!ElementUtil.hasClass(sortDirection.className, th)) {
            th.click();

            // need to try again as the first sort direction may not be the desired one
            if (!ElementUtil.hasClass(sortDirection.className, th)) {
                th.click();

                // if it's still not sorted, something has gone horribly wrong
                if (!ElementUtil.hasClass(sortDirection.className, th)) {
                    throw new IllegalStateException("Cannot sort table by column " + columnIndex);
                }
            }
        }
    }

    public void searchInResults(final String text) {
        final WebElement input = findElement(By.cssSelector(".dataTables_filter input"));
        input.sendKeys(text);
    }

    public void showEntries(final EntryCount entryCount) {
        final Select select = new Select(findElement(By.cssSelector(".dataTables_length select")));

        select.selectByValue(entryCount.value);
    }

    private void paginate(final WebElement button) {
        final WebElement firstCell = findElement(By.cssSelector("table.dataTable:not(.DTFC_Cloned) td:first-child"));

        button.click();

        new WebDriverWait(getDriver(), 15).until(ExpectedConditions.stalenessOf(firstCell));
    }

    private String getEntriesGroup(final String groupName) {
        final String text = findElement(By.className("dataTables_info")).getText();
        final Matcher matcher = SHOWING_ENTRIES.matcher(text);

        // the call the matcher.matches is required to get the grouping to work
        if (!matcher.matches()) {
            throw new IllegalStateException("DataTables String in incorrect format (" + text + ')');
        }

        return matcher.group(groupName);
    }

    private int getIntValue(final String numberString) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);

        try {
            return numberFormat.parse(numberString).intValue();
        } catch (final ParseException e) {
            throw new IllegalStateException("Error parsing DataTables number", e);
        }
    }

    public enum SortDirection {
        ASCENDING("sorting_asc"),
        DESCENDING("sorting_desc");

        private final String className;

        SortDirection(final String className) {
            this.className = className;
        }
    }

    public enum EntryCount {
        TEN("10"),
        TWENTY_FIVE("25"),
        FIFTY("50"),
        ONE_HUNDRED("100");

        private final String value;

        EntryCount(final String value) {
            this.value = value;
        }
    }

}
