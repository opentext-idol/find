/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TableView;
import com.autonomy.abc.selenium.find.bi.TableView.SortDirection;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import org.apache.commons.lang3.text.WordUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.autonomy.abc.selenium.find.bi.TableView.EntryCount.TWENTY_FIVE;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

@Role(UserRole.BIFHI)
public class TableITCase extends IdolFindTestBase {

    private static final int NUMBER_PER_PAGE = 10;
    private BIIdolFindElementFactory elementFactory;
    private TableView tableView;
    private FindService findService;

    public TableITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        elementFactory = (BIIdolFindElementFactory)getElementFactory();
        findService = getApplication().findService();
    }

    @Test
    @ResolvedBug("FIND-251")
    public void testTableTabShowsTable() {
        init("s");

        tableView.waitForTable();
        verifyThat("Table element displayed", tableView.tableVisible());
        verifyThat("Parametric selectors appear", tableView.parametricSelectionDropdownsExist());

        elementFactory.getConceptsPanel().removeAllConcepts();
        findService.searchAnyView("shambolicwolic");

        final IdolFindPage findPage = elementFactory.getFindPage();
        assumeThat("There are no results for this", findPage.goToListView().getTotalResultsNum(), is(0));

        //TODO: this doesn't work - dunno why though
        tableView = findPage.goToTable();

        final WebElement message = tableView.message();
        assertThat("Message appearing when no sunburst & search from Sunburst", message, displayed());
        final String correctMessage = "Could not display Table View: your search returned no parametric values";
        verifyThat("Message is: " + correctMessage, message, containsText(correctMessage));
    }

    @Test
    public void testSingleFieldGivesCorrectTableValues() {
        init("dog");

        tableView.waitForTable();
        verifyThat("With single field, table has 2 columns", tableView.columnCount(), is(2));

        checkRowNumber(0);
    }

    // TODO: test contains potentially unreasonable assumption of filter with 1 - 10 values
    @Test
    public void testTwoFieldsGiveCorrectTableValues() {
        tableView = elementFactory.getFindPage().goToTable();

        final FilterPanel filters = filters();
        final int reasonableFilterNumber = 10;
        final int goodCategory = filters.nthParametricThatSatisfiedCondition(0,
                                                                             (Integer x) -> x < reasonableFilterNumber && x > 0);

        assertThat("There is a filter category with between 1 & " + reasonableFilterNumber + " filters", goodCategory, greaterThan(0));

        final String categoryName = filters.parametricField(goodCategory).filterCategoryName();
        final Map<String, Integer> filterCounts = getHighestResultCountForOtherFilters(goodCategory, categoryName);
        tableView.waitForTable();
        DriverUtil.scrollIntoView(getDriver(), tableView.firstParametricSelectionDropdown().getElement());
        tableView.firstParametricSelectionDropdown().select(WordUtils.capitalize(categoryName.toLowerCase()));
        tableView.waitForTable();

        for(final Map.Entry<String, Integer> stringIntegerEntry : filterCounts.entrySet()) {
            tableView.secondParametricSelectionDropdown().select(WordUtils.capitalize(stringIntegerEntry.getKey().toLowerCase()));
            tableView.waitForTable();
            verifyThat("Number of columns is: " + tableView.columnCount() + " for main category " + categoryName + " with second category " + stringIntegerEntry.getKey()
                    , tableView.columnCount(), greaterThan(stringIntegerEntry.getValue()));
        }

        checkRowNumber(goodCategory);
    }

    /* Selects each filter in categoryName in turn & returns a map
    of the other categories and the highest no. of filters they contain
    when any of the filters in categoryName is selected*/
    private Map<String, Integer> getHighestResultCountForOtherFilters(final int goodCategory, final String categoryName) {
        final FilterPanel filters = filters();
        final FindPage findPage = elementFactory.getFindPage();
        final Map<String, Integer> filterCounts = new HashMap<>();

        for(int i = 0; i < filters.parametricField(goodCategory).getFilters().size(); i++) {
            tableView.waitForTable();
            filters.parametricField(goodCategory).getFilters().get(i).check();

            tableView.waitForTable();
            findPage.waitForParametricValuesToLoad();

            for(final ParametricFieldContainer cont : filters.parametricFieldContainers()) {
                final String filterCat = cont.filterCategoryName();
                if(!filterCat.equals(categoryName)) {
                    final Integer filterNum = filterCounts.get(filterCat);
                    if(filterNum == null || filterNum < cont.getFilterCount()) {
                        filterCounts.put(filterCat, cont.getFilterCount());
                    }
                }
            }
            filters.parametricField(goodCategory).getFilters().get(0).uncheck();
            findPage.waitForParametricValuesToLoad();
        }
        return filterCounts;
    }

    private void checkRowNumber(final int index) {
        final int filterNumber = filters().parametricField(index).getFilterCount();
        verifyThat("Number of rows equals number of filters in filter type (or max per page)",
                   tableView.rowCount(),
                   anyOf(is(NUMBER_PER_PAGE), is(filterNumber)));
    }

    @Test
    public void testPagination() {
        tableView = elementFactory.getFindPage().goToTable();
        tableView.waitForTable();

        assumeThat(tableView.currentPage(), is(1));

        final String initialText = tableView.text(1, 0);

        assumeThat("There needs to be enough parametric values to have >1 page", tableView.nextButton(), not(disabled()));

        tableView.nextPage();
        verifyThat(tableView.text(1, 0), is(not(initialText)));
        verifyThat(tableView.currentPage(), is(2));

        tableView.previousPage();
        verifyThat(tableView.text(1, 0), is(initialText));
        verifyThat(tableView.currentPage(), is(1));
    }

    @Test
    public void testSorting() {
        init("*");

        tableView.waitForTable();
        tableView.sort(1, SortDirection.DESCENDING);

        final int rowCount = tableView.rowCount();

        final List<Integer> values = new ArrayList<>(rowCount);

        for(int i = 1; i <= rowCount; i++) {
            values.add(Integer.parseInt(tableView.text(i, 1)));
        }

        final List<Integer> sortedValues = new ArrayList<>(values);

        // sort will give us ascending order
        Collections.sort(sortedValues);
        Collections.reverse(sortedValues);

        verifyThat(values, is(sortedValues));
    }

    @Test
    public void testSearchInResults() {
        init("whirlpool");

        tableView.waitForTable();

        final String searchText = tableView.text(2, 0);
        tableView.searchInResults(searchText);

        verifyThat(tableView.text(1, 0), is(searchText));
    }

    @Test
    public void testShowEntries() {
        init("*");

        tableView.waitForTable();

        assumeThat("Table needs at least " + NUMBER_PER_PAGE + " rows to test increasing the number to view",
                   tableView.maxRow(),
                   is(NUMBER_PER_PAGE));

        tableView.showEntries(TWENTY_FIVE);

        verifyThat(tableView.maxRow(), is(greaterThan(NUMBER_PER_PAGE)));
    }

    @Test
    @ResolvedBug("FIND-383")
    public void testSideBarFiltersChangeTable() {
        init("lashing");

        tableView.waitForTable();

        final FilterPanel filters = filters();
        final String parametricSelectionFirst = tableView.getFirstSelectedFieldName();

        filters.parametricContainer(parametricSelectionFirst).getFilters().get(0).check();

        tableView.waitForTable();
        assertThat("Parametric selection changed", tableView.getFirstSelectedFieldName(), not(Matchers.is(parametricSelectionFirst)));
    }

    @Test
    @ResolvedBug("FIND-405")
    public void testParametricSelectors() {
        init("wild horses");

        final int index = filters().nonZeroParamFieldContainer(0);
        final String firstParametric = filters().parametricField(index).filterCategoryName();
        verifyThat("Default parametric selection is 1st parametric type", firstParametric, startsWith(tableView.getFirstSelectedFieldName().toUpperCase()));

        tableView.secondParametricSelectionDropdown().open();
        verifyThat("1st selected parametric does not appear as choice in 2nd", tableView.getParametricDropdownItems(tableView.secondParametricSelectionDropdown()), not(contains(firstParametric)));
    }

    private void init(final String searchText) {
        findService.searchAnyView(searchText);
        tableView = elementFactory.getFindPage().goToTable();
    }

    private FilterPanel filters() {
        return elementFactory.getFilterPanel();
    }
}
