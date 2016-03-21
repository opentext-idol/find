package com.autonomy.abc.search;

import com.autonomy.abc.base.SOTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.element.Pagination;
import com.autonomy.abc.selenium.query.*;
import com.autonomy.abc.selenium.search.SOSearchResult;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class SearchDateITCase extends SOTestBase {
    private SearchPage searchPage;
    private SearchService searchService;
    
    public SearchDateITCase(TestConfig config) {
        super(config);
    }
    
    @Before
    public void setUp() {
        searchService = getApplication().searchService();
        searchPage = searchService.search("begin");
    }


    @Test
    @KnownBug("HOD-1116")
    public void testFromDateFilter() {
        final Date date = beginDateFilterTest();
        final String firstResult = searchPage.getSearchResult(1).getTitleString();
        final Date invalidDate = DateUtils.addMinutes(date, 1);

        searchPage.filterBy(new DatePickerFilter().from(date));
        for (final String label : searchPage.filterLabelList()) {
            assertThat("no 'Until' filter applied", label,not(containsString("Until: ")));
        }
        assertThat("applied 'From' filter", searchPage.fromDateInput().getValue(), not(isEmptyOrNullString()));
        verifyValidDate(firstResult);

        searchPage.filterBy(new DatePickerFilter().from(invalidDate));
        verifyInvalidDate(firstResult);

        searchPage.filterBy(new DatePickerFilter().from(date));
        verifyValidDate(firstResult);
    }

    @Test
    @KnownBug("HOD-1116")
    public void testWideFromDate() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy HH:mm");
        Date june = simpleDateFormat.parse("06/12/2015 00:00");

        searchService.search(new Query("Pertussis").withFilter(new IndexFilter("wiki_eng")));
        Waits.loadOrFadeWait();
        searchPage.filterBy(new DatePickerFilter().from(june));

        for(int i = 0; i < 2; i++) {
            for (SOSearchResult searchResult : searchPage.getSearchResults()) {
                verifyThat(searchResult.getDate(), greaterThanOrEqualTo(june));
            }

            searchPage.switchResultsPage(Pagination.NEXT);
        }
    }

    @Test
    @KnownBug("HOD-1116")
    public void testUntilDateFilter() {
        final Date date = beginDateFilterTest();
        final String firstResult = searchPage.getSearchResult(1).getTitleString();

        // plus 1 minute to be inclusive
        final Date validDate = DateUtils.addMinutes(date, 1);

        searchPage.filterBy(new DatePickerFilter().until(validDate));
        for (final String label : searchPage.filterLabelList()) {
            assertThat("no 'From' filter applied", label,not(containsString("From: ")));
        }
        assertThat("applied 'Until' filter", searchPage.untilDateInput().getValue(), not(isEmptyOrNullString()));
        verifyValidDate(firstResult);

        searchPage.filterBy(new DatePickerFilter().until(date));
        verifyInvalidDate(firstResult);

        searchPage.filterBy(new DatePickerFilter().until(validDate));
        verifyValidDate(firstResult);
    }

    @Test
    @KnownBug("HOD-1116")
    public void testWideUntilDate() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy HH:mm");
        Date june = simpleDateFormat.parse("06/12/2015 00:00");

        searchService.search(new Query("Pertussis").withFilter(new IndexFilter("wiki_eng")));
        Waits.loadOrFadeWait();
        searchPage.filterBy(new DatePickerFilter().until(june));

        //There should be results in this index
        verifyThat(searchPage.getSearchResults().size(), not(0));

        for(int i = 0; i < 2; i++) {
            for (SOSearchResult searchResult : searchPage.getSearchResults()) {
                verifyThat(searchResult.getDate(), lessThanOrEqualTo(june));
            }

            if(searchPage.getHeadingResultsCount() > SearchPage.RESULTS_PER_PAGE) {
                searchPage.switchResultsPage(Pagination.NEXT);
            }
        }
    }

    private Date beginDateFilterTest() {
        // not all indexes have times configured
        searchPage = searchService.search(new Query("Dog")
                .withFilter(new IndexFilter("news_eng"))
                .withFilter(new FieldTextFilter("EMPTY{}:Date"))
        );
        Date date = searchPage.getSearchResult(1).getDate();
        assumeThat("test requires first search result to have a date", date, notNullValue());
        LOGGER.info("First Result: " + searchPage.getSearchResult(1).getTitleString() + " " + date);
        return date;
    }

    private void verifyValidDate(String firstResult) {
        LOGGER.info("from: " + searchPage.fromDateInput().getValue());
        LOGGER.info("until: " + searchPage.untilDateInput().getValue());
        if (verifyThat(searchPage.getHeadingResultsCount(), greaterThan(0))) {
            verifyThat("Document should be displayed again", searchPage.getSearchResult(1).getTitleString(), is(firstResult));
        }
    }

    private void verifyInvalidDate(String firstResult) {
        LOGGER.info("from: " + searchPage.fromDateInput().getValue());
        LOGGER.info("until: " + searchPage.untilDateInput().getValue());
        if (searchPage.getHeadingResultsCount() > 0) {
            verifyThat("Document should not be displayed", searchPage.getSearchResult(1).getTitleString(), not(firstResult));
        }
    }

    @Test
    public void testFromDateAlwaysBeforeUntilDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.MAY, 4, 12, 0);
        final Date date = calendar.getTime();

        searchPage.filterBy(new StringDateFilter().from(date).until(date));
        assertThat("Dates should be equal", searchPage.fromDateInput().getValue(), is(searchPage.untilDateInput().getValue()));

        searchPage.filterBy(new StringDateFilter().from(DateUtils.addMinutes(date, 1)).until(date));
        searchPage.sortBy(SearchBase.Sort.RELEVANCE);
        assertThat("From date should be blank", searchPage.fromDateInput().getValue(), isEmptyOrNullString());

        searchPage.filterBy(new StringDateFilter().from(date).until(DateUtils.addMinutes(date, -1)));
        searchPage.sortBy(SearchBase.Sort.RELEVANCE);
        assertThat("Until date should be blank", searchPage.untilDateInput().getValue(), isEmptyOrNullString());
    }

    @Test
    public void testFromDateEqualsUntilDate() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.DECEMBER, 12, 12, 12);
        final Date date = calendar.getTime();

        searchPage.filterBy(new StringDateFilter().from(date).until(date));

        assertThat(searchPage.fromDateInput().getValue(), is(searchPage.untilDateInput().getValue()));

        Date nextDate = DateUtils.addMinutes(date, 1);
        searchPage.filterBy(new StringDateFilter().until(nextDate));
        assertThat(searchPage.untilDateInput().getValue(), is(searchPage.formatInputDate(nextDate)));

        nextDate = DateUtils.addMinutes(date, -1);
        searchPage.filterBy(new StringDateFilter().until(nextDate));
        assertThat(searchPage.untilDateInput().getValue(), isEmptyOrNullString());
    }

}
