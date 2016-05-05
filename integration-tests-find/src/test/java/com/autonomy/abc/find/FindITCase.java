package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentPreviewer;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.*;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.*;
import com.autonomy.abc.shared.QueryTestHelper;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import com.autonomy.abc.selenium.element.DocumentViewer;



import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class FindITCase extends FindTestBase {
    private FindPage findPage;
    private FindTopNavBar navBar;
    private FindResultsPage results;
    private FindService findService;

    public FindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        navBar = getElementFactory().getTopNavBar();
        results = findPage.getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    public void testSendKeys() throws InterruptedException {
        String searchTerm = "Fred is a chimpanzee";
        findService.search(searchTerm);
        assertThat(navBar.getSearchBoxTerm(), is(searchTerm));
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testFilteringByParametricValues(){
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

    private void verifyParametricFields(FindParametricCheckbox checked, int expectedResults){
        Waits.loadOrFadeWait();
        int resultsTotal = results.getResultTitles().size();
        int checkboxResults = checked.getResultsCount();
        verifyThat(resultsTotal, is(Math.min(expectedResults, 30)));
        verifyThat(checkboxResults, is(expectedResults));
    }

    private void verifyTicks(boolean checkbox2, boolean checkbox1){
        verifyThat(checkbox1().isChecked(), is(checkbox1));
        verifyThat(checkbox2().isChecked(), is(checkbox2));
    }

    private FindParametricCheckbox checkbox1(){
        if(getConfig().getType()==ApplicationType.HOSTED){
            return results.parametricTypeCheckbox("source connector", "SIMPSONSARCHIVE");
        }
        else{
            return results.parametricTypeCheckbox("SOURCE","GOOGLE");
        }
    }
    private FindParametricCheckbox checkbox2(){
        if(getConfig().getType()==ApplicationType.HOSTED){
            return results.parametricTypeCheckbox("content type", "TEXT/PLAIN");
        }
        else{
            return results.parametricTypeCheckbox("CATEGORY","ENTERTAINMENT");
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
    public void testSearch(){
        findService.search("Red");
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testFilterByIndex(){
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
    public void testFilterByMultipleIndexes(){
        findService.search("unbelievable");

        //currently the way to do both at a time is weird
        //findPage.filterBy(new IndexFilter(Arrays.asList(findPage.getIthIndex(3),findPage.getIthIndex(4))));
        //new add method in IndexFilter - BUT this still doesn't really replicate people because it takes em off...
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

        assertThat("Both filter indexes thus both results",firstFilterResults+secondFilterResults,is(bothFilterResults));
    }

    @Test
    public void testShowDocumentPreview(){
        assumeThat(getConfig().getType(),is(ApplicationType.ON_PREM));

        findService.search("cake");

        DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();

        if (findPage.loadingIndicatorExists()) {
            assertThat("Preview not stuck loading", !findPage.loadingIndicator().isDisplayed());
        }
        assertThat("There is content in preview",findPage.previewContents().getText(),not(isEmptyOrNullString()));

        assertThat("Index displayed",docPreview.getIndex(),not(nullValue()));

        assertThat("Reference displayed",docPreview.getReference(),not(nullValue()));

        Frame previewFrame = new Frame(getWindow(), docPreview.frame());

        String frameText=previewFrame.getText();

        verifyThat("Preview document has content",frameText,not(isEmptyOrNullString()));
        assertThat("Preview document has no error",previewFrame.getText(),not(containsString("encountered an error")));

        docPreview.close();
    }

    @Test
    public void testFilteredByIndexOnlyHasFilesFromIndex(){
        findService.search("Sad");

        DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();
        String chosenIndex = docPreview.getIndex().getDisplayName();
        docPreview.close();

        findPage.filterBy(new IndexFilter(chosenIndex));
        //weirdly failing to open the 2nd result (subsequent okay)
        for (int i=1; i<6; i++){
            DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
            assertThat(docViewer.getIndex().getDisplayName(),is(chosenIndex));
            docViewer.close();
        }
    }

    @Test
    public void testQuickDoubleClickOnDateFilterNotCauseError(){
        findService.search("wookie");

        results.toggleDateSelection(FindResultsPage.DateEnum.MONTH);
        results.toggleDateSelection(FindResultsPage.DateEnum.MONTH);

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.resultsDiv().getText().toLowerCase(), not(containsString("an error")));

    }

    //Correctly failing with OnPrem - because of entry 'in 21 days'
    @Test
    public void testPreDefinedWeekHasSameResultsAsCustomWeek(){
        preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum.WEEK);
    }

    @Test
    public void testPreDefinedMonthHasSameResultsAsCustomMonth(){
        preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum.MONTH);
    }

    @Test
    public void testPreDefinedYearHasSameResultsAsCustomYear(){
        preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum.YEAR);
    }

    private void preDefinedDateFiltersVersusCustomDateFilters(FindResultsPage.DateEnum period){
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
                    cal.add(Calendar.DATE,-7);
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
    public void testDateRemainsWhenClosingAndReopeningDateFilters(){
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
    public void testBooleanOperators(){
        String termOne = "musketeers";
        String termTwo = "\"dearly departed\"";

        findService.search(termOne);
        List<String> musketeersSearchResults = results.getResultTitles();
        int numberOfMusketeersResults = musketeersSearchResults.size();

        findService.search(termTwo);
        List<String> dearlyDepartedSearchResults = results.getResultTitles();
        int numberOfDearlyDepartedResults = dearlyDepartedSearchResults.size();

        findService.search(termOne + " AND " + termTwo);
        List<String> andResults = results.getResultTitles();
        int numberOfAndResults = andResults.size();

        assertThat(numberOfMusketeersResults,greaterThanOrEqualTo(numberOfAndResults));
        assertThat(numberOfDearlyDepartedResults, greaterThanOrEqualTo(numberOfAndResults));
        String[] andResultsArray = andResults.toArray(new String[andResults.size()]);
        assertThat(musketeersSearchResults, hasItems(andResultsArray));
        assertThat(dearlyDepartedSearchResults, hasItems(andResultsArray));

        findService.search(termOne + " OR " + termTwo);
        List<String> orResults = results.getResultTitles();
        Set<String> concatenatedResults = new HashSet<>(ListUtils.union(musketeersSearchResults, dearlyDepartedSearchResults));
        assertThat(orResults.size(), is(concatenatedResults.size()));
        assertThat(orResults, containsInAnyOrder(concatenatedResults.toArray()));

        findService.search(termOne + " XOR " + termTwo);
        List<String> xorResults = results.getResultTitles();
        concatenatedResults.removeAll(andResults);
        assertThat(xorResults.size(), is(concatenatedResults.size()));
        assertThat(xorResults, containsInAnyOrder(concatenatedResults.toArray()));

        findService.search(termOne + " NOT " + termTwo);
        List<String> notTermTwo = results.getResultTitles();
        Set<String> t1NotT2 = new HashSet<>(concatenatedResults);
        t1NotT2.removeAll(dearlyDepartedSearchResults);
        assertThat(notTermTwo.size(), is(t1NotT2.size()));
        assertThat(notTermTwo, containsInAnyOrder(t1NotT2.toArray()));

        findService.search(termTwo + " NOT " + termOne);
        List<String> notTermOne = results.getResultTitles();
        Set<String> t2NotT1 = new HashSet<>(concatenatedResults);
        t2NotT1.removeAll(musketeersSearchResults);
        assertThat(notTermOne.size(), is(t2NotT1.size()));
        assertThat(notTermOne, containsInAnyOrder(t2NotT1.toArray()));
    }

    //Following 3 correctly failing On_Prem
    @Test
    public void testCorrectErrorMessageDisplayed() {
        new QueryTestHelper<>(findService).booleanOperatorQueryText(Errors.Search.OPERATORS);
        new QueryTestHelper<>(findService).emptyQueryText(Errors.Search.STOPWORDS);
    }

    @Test
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        new QueryTestHelper<>(findService).hiddenQueryOperatorText();
    }

    @Test
    public void testSearchParentheses() {
        new QueryTestHelper<>(findService).mismatchedBracketQueryText();
    }

    @Test
    @KnownBug({"IOD-8454","CCUK-3634"})
    public void testSearchQuotationMarks() {
        new QueryTestHelper<>(findService).mismatchedQuoteQueryText(Errors.Search.QUOTES);
    }

    @Test
    @KnownBug("CCUK-3700")
    public void testWhitespaceSearch() {
        try {
            findService.search("       ");
        } catch (TimeoutException e) { /* Expected behaviour */ }

        assertThat(findPage.footerLogo(), displayed());

        findService.search("Kevin Costner");

        List<String> resultTitles = results.getResultTitles();

        findService.search(" ");
        assertThat(results.getResultTitles(), is(resultTitles));
        verifyThat("Empty Parametric Table does not exist",!findPage.parametricEmptyExists());
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
        assertThat(findPage.parametricContainer().getText(), not(isEmptyOrNullString()));
    }

    @Test
    @KnownBug("CSA-1577")
    public void testClickingCustomDateFilterDoesNotRefreshResults(){
        findService.search("O Captain! My Captain!");
        // may not happen the first time
        for (int unused = 0; unused < 5; unused++) {
            results.toggleDateSelection(FindResultsPage.DateEnum.CUSTOM);
            assertThat(results.resultsDiv().getText(), not(containsString("Loading")));
        }
    }

    @Test
    @KnownBug("CSA-1665")
    public void testSearchTermInResults(){
        String searchTerm = "tiger";

        findService.search(searchTerm);

        for(WebElement searchElement : getDriver().findElements(By.xpath("//*[contains(@class,'search-text') and contains(text(),'" + searchTerm+ "')]"))){
            if(searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText().toLowerCase(), containsString(searchTerm));
            }
            verifyThat(searchElement, not(hasTagName("a")));
        }
    }

    //Both correctly failing last part because some titles are duplicates
    @Test
    @KnownBug("CSA-2082")
    public void testAutoScroll(){
        findService.search("nightmare");

        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(30), lessThanOrEqualTo(60)));

        findPage.scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(60), lessThanOrEqualTo(90)));

        List<String> titles = results.getResultTitles();
        Set<String> titlesSet = new HashSet<>(titles);

        verifyThat("No duplicate titles", titles.size(), is(titlesSet.size()));
    }

    @Test
    public void testFewerThan30ResultsDoesNotAttemptLoadMore(){
        findService.search(new Query("oesophageal"));

        verifyThat(results.getResults().size(),lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.resultsDiv(),containsText("No more results found"));
    }

    @Test
    public void testNoResults(){
        findService.search("thissearchwillalmostcertainlyreturnnoresults");

        verifyThat(results.resultsDiv(), either(containsText("No results found")).or(containsText("No more results found")));

        findPage.scrollToBottom();

        int occurrences = StringUtils.countMatches(results.resultsDiv().getText(), "results found");
        verifyThat("Only one message showing at the bottom of search results", occurrences, is(1));
    }

    @Test
    @KnownBug("CCUK-3624")
    public void testRefreshEmptyQuery() throws InterruptedException {
        findService.search("something");
        findService.search("");
        Thread.sleep(5000);

        getWindow().refresh();
        findPage = getElementFactory().getFindPage();
        navBar = getElementFactory().getTopNavBar();

        verifyThat(navBar.getSearchBoxTerm(), is(""));
        verifyThat("taken back to landing page after refresh", findPage.footerLogo(), displayed());
    }

    @Test
    public void testOpenDocInNewTab() {
        findService.search("clueless");
        for (QueryResult queryResult : results.getResults(5)) {
            DocumentViewer docViewer = queryResult.openDocumentPreview();
            SharedPreviewTests.testOpenInNewTabFromViewer(getMainSession(), docViewer);
            docViewer.close();
        }
    }


}
