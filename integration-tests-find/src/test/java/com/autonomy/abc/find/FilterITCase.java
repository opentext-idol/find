package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.filters.DateOption;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.FindParametricCheckbox;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
import com.autonomy.abc.selenium.find.results.FindResultsPage;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryResult;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.StaleElementReferenceException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class FilterITCase extends FindTestBase {
    private FindPage findPage;
    private FindResultsPage results;
    private FindService findService;

    public FilterITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        results = getElementFactory().getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    public void testParametricFiltersResults() {
        findService.search("cats");
        findPage.waitForParametricValuesToLoad();

        int originalNumberOfResults = findPage.totalResultsNum();
        ParametricFieldContainer parametricFieldContainer = filters().parametricField(1);

        checkbox2().check();

        try{parametricFieldContainer.getParentName();}
        catch (StaleElementReferenceException e){fail("parametric fields reloaded");}
        results.waitForResultsToLoad();

        int newNumberOfResults = findPage.totalResultsNum();

        verifyThat("original results number changed", newNumberOfResults, lessThanOrEqualTo(originalNumberOfResults));
    }

    //correctly broken BUT not sure what behaviour
    @Test
    public void testFilteringByParametricValues() {
        findService.search("Alexis");
        findPage.waitForParametricValuesToLoad();
        int expectedResults = checkbox2().getResultsCount();
        checkbox2().check();
        results.waitForResultsToLoad();
        verifyParametricFields(checkbox2(), expectedResults);
        verifyTicks(true, false);

        expectedResults = checkbox1().getResultsCount();
        checkbox1().check();
        results.waitForResultsToLoad();
        verifyParametricFields(checkbox1(), expectedResults);
        verifyTicks(true, true);

        checkbox2().uncheck();
        results.waitForResultsToLoad();
        expectedResults = checkbox1().getResultsCount();
        verifyParametricFields(checkbox1(), expectedResults);
        verifyTicks(false, true);
    }

    private void verifyParametricFields(FindParametricCheckbox checked, int expectedResults) {
        Waits.loadOrFadeWait();
        int resultsTotal = results.getResultTitles().size();
        int checkboxResults = checked.getResultsCount();
        verifyThat(resultsTotal, is(Math.min(expectedResults, 30)));
        verifyThat(checkboxResults, is(expectedResults));
    }

    private void verifyTicks(boolean checkbox2, boolean checkbox1) {
        verifyThat(checkbox1().isChecked(), is(checkbox1));
        verifyThat(checkbox2().isChecked(), is(checkbox2));
    }

    private FindParametricCheckbox checkbox1() {
        if (isHosted()) {
            return filters().checkboxForParametricValue("source connector", "SIMPSONSARCHIVE");
        } else {
            return filters().checkboxForParametricValue("SOURCE", "GOOGLE");
        }
    }

    private FindParametricCheckbox checkbox2() {
        if (isHosted()) {
            return filters().checkboxForParametricValue("content type", "TEXT/PLAIN");
        } else {
            return filters().checkboxForParametricValue("CATEGORY", "ENTERTAINMENT");
        }
    }

    @Test
    public void testParametricFiltersModal() {
        findService.search("cats");
        findPage.waitForParametricValuesToLoad();

        checkbox2().check();

        //click on 'see all' button (under any category)
        //test if modal is open
        //test if the current parametric field is the active tab in the modal
        //test if the currently selected values are ticked in the modal as well

        //tick another value inside the modal
        //click apply in the modal
        //check if the value is ticked inside a parametric field container on the filters panel
    }

    @Test
    public void testDeselectingFiltersDoesNotRemove(){
        findService.search("confusion");
        findPage.waitForParametricValuesToLoad();

        String parametricFilterType = filters().getParametricFieldContainer(0).getParentName();
        List<FindParametricCheckbox> boxes = checkAllVisibleFiltersInFirstParametrics();

        for(FindParametricCheckbox checkbox:boxes){
            checkbox.uncheck();
            verifyThat("Unchecking not removing filter from list",filters().checkBoxesForParametricFieldContainer(0),hasSize(boxes.size()));
        }
        verifyThat("Removing all has not removed group", filters().getParametricFieldContainer(0).getParentName(),is(parametricFilterType));
    }

    @Test
    public void testDeselectingFiltersNoFloatingTooltips(){
        findService.search("boris");
        findPage.waitForParametricValuesToLoad();

        List<FindParametricCheckbox> boxes = checkAllVisibleFiltersInFirstParametrics();
        for(FindParametricCheckbox checkbox:boxes){
            checkbox.name().click();
        }

        verifyThat("Tooltips aren't floating everywhere",getElementFactory().getToolTips().toolTips(),not(hasSize((boxes.size()))));
    }

    private List<FindParametricCheckbox> checkAllVisibleFiltersInFirstParametrics(){
        List<FindParametricCheckbox> boxes = filters().checkBoxesForParametricFieldContainer(0);
        for(FindParametricCheckbox checkBox:boxes){
            checkBox.check();
        }
        return boxes;
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() {
        findService.search("wolf");

        FilterPanel panel = filters();
        panel.clickFirstIndex();
        panel.clickFirstIndex();

        results.waitForResultsToLoad();
        assertThat("No error message",!results.errorContainer().isDisplayed());
    }

    @Test
    public void testFilterByIndex() {
        findService.search("face");
        QueryResult queryResult = results.searchResult(1);
        String titleString = queryResult.getTitleString();

        DocumentViewer docPreview = queryResult.openDocumentPreview();
        String index = databaseOrIndex(docPreview);
        docPreview.close();

        findPage.filterBy(new IndexFilter(index));
        assertThat(results.searchResult(1).getTitleString(), is(titleString));
    }

    @Test
    public void testFilterByMultipleIndexes() {
        findService.search("unbelievable");

        IndexFilter filter = new IndexFilter(filters().getIndex(2));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        int firstFilterResults = findPage.totalResultsNum();

        filter.add(filters().getIndex(3));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        int bothFilterResults = findPage.totalResultsNum();

        findPage.filterBy(new IndexFilter(filters().getIndex(3)));
        int secondFilterResults = findPage.totalResultsNum();

        assertThat("Both filter indexes thus both results", firstFilterResults + secondFilterResults, is(bothFilterResults));
    }

    @Test
    public void testFilteredByIndexOnlyHasFilesFromIndex() {
        findService.search("Better");

        DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();
        String chosenIndex = databaseOrIndex(docPreview);
        docPreview.close();

        findPage.filterBy(new IndexFilter(chosenIndex));
        //weirdly failing to open the 2nd result (subsequent okay)
        for (int i = 1; i < 6; i++) {
            DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
            assertThat(databaseOrIndex(docPreview), is(chosenIndex));
            docViewer.close();
        }
    }

    private String databaseOrIndex(DocumentViewer docPreview){
        if(isHosted()){
            return docPreview.getIndexAsString();
        }
        else{return docPreview.getDatabase();}
    }
    @Test
    public void testQuickDoubleClickOnDateFilterNotCauseError() {
        findService.search("wookie");

        toggleDateSelection(DateOption.MONTH);
        toggleDateSelection(DateOption.MONTH);

        results.waitForResultsToLoad();
        assertThat(results.resultsDiv().getText().toLowerCase(), not(containsString("an error")));

    }

    @Test
    public void testPreDefinedWeekHasSameResultsAsCustomWeek() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.WEEK);
    }

    @Test
    public void testPreDefinedMonthHasSameResultsAsCustomMonth() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.MONTH);
    }

    @Test
    public void testPreDefinedYearHasSameResultsAsCustomYear() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.YEAR);
    }

    private void preDefinedDateFiltersVersusCustomDateFilters(DateOption period) {
        findService.search("*");

        toggleDateSelection(period);
        List<String> preDefinedResults = results.getResultTitles();
        findPage.filterBy(new StringDateFilter().from(getDate(period)).until(new Date()));
        List<String> customResults = results.getResultTitles();

        assertThat(preDefinedResults, is(customResults));
    }

    private Date getDate(DateOption period) {
        Calendar cal = Calendar.getInstance();

        if (period != null) {
            switch (period) {
                case WEEK:
                    cal.add(Calendar.DATE, -7);
                    break;
                case MONTH:
                    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
                    break;
                case YEAR:
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                    break;
            }
        }
        return cal.getTime();
    }

    @Test
    @ActiveBug("FIND-152")
    public void testDateRemainsWhenClosingAndReopeningDateFilters() {
        Date start = getDate(DateOption.MONTH);
        Date end = getDate(DateOption.WEEK);

        findService.search(new Query("Corbyn")
                .withFilter(new StringDateFilter().from(start).until(end)));

        Waits.loadOrFadeWait();
        for (int unused = 0; unused < 3; unused++) {
            toggleDateSelection(DateOption.CUSTOM);
            Waits.loadOrFadeWait();
        }
        assertThat(findPage.fromDateInput().getValue(), is(findPage.formatInputDate(start)));
        assertThat(findPage.untilDateInput().getValue(), is(findPage.formatInputDate(end)));
    }

    @Test
    @ResolvedBug("CSA-1577")
    public void testClickingCustomDateFilterDoesNotRefreshResults() {
        findService.search("O Captain! My Captain!");
        // may not happen the first time
        for (int unused = 0; unused < 5; unused++) {
            toggleDateSelection(DateOption.CUSTOM);
            assertThat(results.resultsDiv().getText(), not(containsString("Loading")));
        }
    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }

    private void toggleDateSelection(DateOption date) {
        filters().toggleFilter(date);
        results.waitForResultsToLoad();
    }
}
