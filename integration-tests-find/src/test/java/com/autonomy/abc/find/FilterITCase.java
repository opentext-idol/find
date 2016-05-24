package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindParametricCheckbox;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryResult;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
        results = findPage.getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    public void testFilteringByParametricValues() {
        findService.search("Alexis");
        findPage.waitForParametricValuesToLoad();
        int expectedResults = checkbox2().getResultsCount();
        checkbox2().check();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        verifyParametricFields(checkbox2(), expectedResults);
        verifyTicks(true, false);

        expectedResults = checkbox1().getResultsCount();
        checkbox1().check();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        verifyParametricFields(checkbox1(), expectedResults);    //TODO Maybe change plainTextCheckbox to whichever has the higher value??
        verifyTicks(true, true);

        checkbox2().uncheck();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
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
        if (getConfig().getType() == ApplicationType.HOSTED) {
            return results.parametricTypeCheckbox("source connector", "SIMPSONSARCHIVE");
        } else {
            return results.parametricTypeCheckbox("SOURCE", "GOOGLE");
        }
    }

    private FindParametricCheckbox checkbox2() {
        if (getConfig().getType() == ApplicationType.HOSTED) {
            return results.parametricTypeCheckbox("content type", "TEXT/PLAIN");
        } else {
            return results.parametricTypeCheckbox("CATEGORY", "ENTERTAINMENT");
        }
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() {
        findService.search("wolf");

        findPage.clickFirstIndex();
        findPage.clickFirstIndex();

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testFilterByIndex() {
        findService.search("face");
        QueryResult queryResult = results.searchResult(1);
        String titleString = queryResult.getTitleString();
        DocumentViewer docPreview = queryResult.openDocumentPreview();

        Index index = docPreview.getIndex();

        docPreview.close();

        findPage.filterBy(new IndexFilter(index));
        assertThat(results.searchResult(1).getTitleString(), is(titleString));
    }

    @Test
    public void testFilterByMultipleIndexes() {
        findService.search("unbelievable");

        IndexFilter filter = new IndexFilter(findPage.getIthIndex(2));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        int firstFilterResults = findPage.totalResultsNum();

        filter.add(findPage.getIthIndex(3));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        int bothFilterResults = findPage.totalResultsNum();

        findPage.filterBy(new IndexFilter(findPage.getIthIndex(3)));
        int secondFilterResults = findPage.totalResultsNum();

        assertThat("Both filter indexes thus both results", firstFilterResults + secondFilterResults, is(bothFilterResults));
    }

    @Test
    public void testFilteredByIndexOnlyHasFilesFromIndex() {
        findService.search("Sad");

        DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();
        String chosenIndex = docPreview.getIndex().getDisplayName();
        docPreview.close();

        findPage.filterBy(new IndexFilter(chosenIndex));
        //weirdly failing to open the 2nd result (subsequent okay)
        for (int i = 1; i < 6; i++) {
            DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
            assertThat(docViewer.getIndex().getDisplayName(), is(chosenIndex));
            docViewer.close();
        }
    }

    @Test
    public void testQuickDoubleClickOnDateFilterNotCauseError() {
        findService.search("wookie");

        results.toggleDateSelection(FindResultsPage.DateEnum.MONTH);
        results.toggleDateSelection(FindResultsPage.DateEnum.MONTH);

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.resultsDiv().getText().toLowerCase(), not(containsString("an error")));

    }

    //Correctly failing with OnPrem - because of entry 'in 21 days'
    @Test
    public void testPreDefinedWeekHasSameResultsAsCustomWeek() {
        preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum.WEEK);
    }

    @Test
    public void testPreDefinedMonthHasSameResultsAsCustomMonth() {
        preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum.MONTH);
    }

    @Test
    public void testPreDefinedYearHasSameResultsAsCustomYear() {
        preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum.YEAR);
    }

    private void preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum period) {
        findService.search("*");

        results.toggleDateSelection(period);
        List<String> preDefinedResults = results.getResultTitles();
        findPage.filterBy(new StringDateFilter().from(getDate(period)));
        List<String> customResults = results.getResultTitles();

        assertThat(preDefinedResults, is(customResults));
    }

    private Date getDate(FindResultsPage.DateEnum period) {
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

    //Correctly failing in OnPrem
    @Test
    public void testDateRemainsWhenClosingAndReopeningDateFilters() {
        Date start = getDate(FindResultsPage.DateEnum.MONTH);
        Date end = getDate(FindResultsPage.DateEnum.WEEK);

        findService.search(new Query("Corbyn")
                .withFilter(new StringDateFilter().from(start).until(end)));

        Waits.loadOrFadeWait();
        for (int unused = 0; unused < 3; unused++) {
            results.toggleDateSelection(FindResultsPage.DateEnum.CUSTOM);
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
            results.toggleDateSelection(FindResultsPage.DateEnum.CUSTOM);
            assertThat(results.resultsDiv().getText(), not(containsString("Loading")));
        }
    }
}
