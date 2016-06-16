package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.ToolTips;
import com.autonomy.abc.selenium.find.filters.*;
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

import java.util.ArrayList;
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

    public FilterITCase(final TestConfig config) {
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
        final int originalNumberOfResults = findPage.totalResultsNum();

        final ParametricFieldContainer parametricFieldContainer = filters().parametricField(1);
        final List<FindParametricCheckbox> firstParametricContainerCheckboxes = parametricFieldContainer.values();
        firstParametricContainerCheckboxes.get(0).check();

        try{
            parametricFieldContainer.getParentName();
        }
        catch (final StaleElementReferenceException e){
            fail("Parametric fields reloaded");
        }

        results.waitForResultsToLoad();

        verifyThat("Added 1 filter: fewer or equal results", findPage.totalResultsNum(), lessThanOrEqualTo(originalNumberOfResults));
        int previousNumberOfResults = findPage.totalResultsNum();

        //within the same filter type
        firstParametricContainerCheckboxes.get(1).check();
        verifyThat("Added filter from same type: more or equal results", findPage.totalResultsNum(), greaterThanOrEqualTo(previousNumberOfResults));
        previousNumberOfResults = findPage.totalResultsNum();

        //different filter type
        filters().checkBoxesForParametricFieldContainer(2).get(1).check();
        verifyThat("Added filter from different type: fewer or equal results", findPage.totalResultsNum(), lessThanOrEqualTo(previousNumberOfResults));

    }

    @Test
    public void testParametricFiltersModal() {
        findService.search("cats");
        findPage.waitForParametricValuesToLoad();

        final ParametricFieldContainer container = filters().parametricField(2);
        final String filterCategory = container.getParentName();

        final List<String> selectedFilters = new ArrayList<>();
        selectedFilters.addAll(selectEvenFilters(1));
        selectedFilters.addAll(selectEvenFilters(2));

        container.seeAll();
        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());

        verifyThat("Correct tab is active",filterModal.activeTabName(),equalToIgnoringCase(filterCategory));
        verifyThat("Same fields selected in modal as panel",filterModal.checkedFieldsAllPanes(),is(selectedFilters));

        final String filterType = filterModal.activeTabName();
        final String checkedFilterName = filterModal.checkCheckBoxInActivePane(0);
        filterModal.applyButton().click();

        final FindParametricCheckbox panelBox = filters().checkboxForParametricValue(filterType,checkedFilterName);
        verifyThat("Filter: "+checkedFilterName+" is now checked on panel",panelBox.isChecked());
    }

    private List<String> selectEvenFilters(final int filterCategory){
        final List<String> filterNames = new ArrayList<>();
        int i=1;
        for(final FindParametricCheckbox box:filters().checkBoxesForParametricFieldContainer(filterCategory)){
            if((i % 2) == 0){
                filterNames.add(box.getName());
                box.check();
            }
            i++;
        }
        return filterNames;
    }

    @Test
    @ActiveBug("FIND-231")
    public void testDeselectingFiltersDoesNotRemove(){
        findService.search("confusion");
        findPage.waitForParametricValuesToLoad();

        final String parametricFilterType = filters().parametricField(0).getParentName();
        final List<FindParametricCheckbox> boxes = checkAllVisibleFiltersInFirstParametrics();

        for(final FindParametricCheckbox checkbox:boxes){
            checkbox.uncheck();
            verifyThat("Unchecking not removing filter from list",filters().checkBoxesForParametricFieldContainer(0),hasSize(boxes.size()));
        }
        verifyThat("Removing all has not removed group", filters().parametricField(0).getParentName(),is(parametricFilterType));
    }

    @Test
    @ActiveBug("FIND-231")
    public void testDeselectingFiltersNoFloatingTooltips(){
        findService.search("boris");
        findPage.waitForParametricValuesToLoad();

        final List<FindParametricCheckbox> boxes = checkAllVisibleFiltersInFirstParametrics();
        for(final FindParametricCheckbox checkbox:boxes){
            checkbox.name().click();
        }

        verifyThat("Tooltips aren't floating everywhere", ToolTips.toolTips(getDriver()),not(hasSize((boxes.size()))));
    }

    private List<FindParametricCheckbox> checkAllVisibleFiltersInFirstParametrics(){
        final List<FindParametricCheckbox> boxes = filters().checkBoxesForParametricFieldContainer(0);
        for(final FindParametricCheckbox checkBox:boxes){
            checkBox.check();
        }
        return boxes;
    }

    @Test
    @ActiveBug("FIND-247")
    public void testSelectDifferentCategoryFiltersAndResultsLoad(){
        findService.search("face");

        for(int i = 0 ; i<filters().numberParametricFieldContainers()-1;i++){
            filters().checkBoxesForParametricFieldContainer(i).get(0).check();
        }
        Waits.loadOrFadeWait();
        verifyThat("Loading indicator not present",!results.loadingIndicatorPresent());
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() {
        findService.search("wolf");

        final FilterPanel panel = filters();
        panel.clickFirstIndex();
        panel.clickFirstIndex();

        results.waitForResultsToLoad();
        assertThat("No error message",!results.errorContainer().isDisplayed());
    }

    @Test
    public void testFilterByIndex() {
        findService.search("face");
        final QueryResult queryResult = results.searchResult(1);
        final String titleString = queryResult.getTitleString();
        final DocumentViewer docPreview = queryResult.openDocumentPreview();

        final String index = databaseOrIndex(docPreview);
        docPreview.close();

        findPage.filterBy(new IndexFilter(index));
        assertThat(results.searchResult(1).getTitleString(), is(titleString));
    }

    @Test
    public void testFilterByMultipleIndexes() {
        findService.search("unbelievable");

        final IndexFilter filter = new IndexFilter(filters().getIndex(2));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        final int firstFilterResults = findPage.totalResultsNum();

        filter.add(filters().getIndex(3));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        final int bothFilterResults = findPage.totalResultsNum();

        findPage.filterBy(new IndexFilter(filters().getIndex(3)));
        final int secondFilterResults = findPage.totalResultsNum();

        assertThat("Both filter indexes thus both results", firstFilterResults + secondFilterResults, is(bothFilterResults));
    }

    @Test
    public void testFilteredByIndexOnlyHasFilesFromIndex() {
        findService.search("Better");

        final DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();
        final String chosenIndex = databaseOrIndex(docPreview);
        docPreview.close();

        findPage.filterBy(new IndexFilter(chosenIndex));
        //weirdly failing to open the 2nd result (subsequent okay)
        for (int i = 1; i < 6; i++) {
            final DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
            assertThat(databaseOrIndex(docPreview), is(chosenIndex));
            docViewer.close();
        }
    }

    private String databaseOrIndex(final DocumentViewer docPreview){
        if(isHosted()){
            return docPreview.getIndexAsString();
        }
        else{
            return docPreview.getDatabase();
        }
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

    private void preDefinedDateFiltersVersusCustomDateFilters(final DateOption period) {
        findService.search("*");

        toggleDateSelection(period);
        final List<String> preDefinedResults = results.getResultTitles();
        findPage.filterBy(new StringDateFilter().from(getDate(period)).until(new Date()));
        final List<String> customResults = results.getResultTitles();

        assertThat(preDefinedResults, is(customResults));
    }

    private Date getDate(final DateOption period) {
        final Calendar cal = Calendar.getInstance();

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
        final Date start = getDate(DateOption.MONTH);
        final Date end = getDate(DateOption.WEEK);

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

    private void toggleDateSelection(final DateOption date) {
        filters().toggleFilter(date);
        results.waitForResultsToLoad();
    }
}
