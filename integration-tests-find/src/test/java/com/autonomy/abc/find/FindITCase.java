package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.autonomy.abc.shared.QueryTestHelper;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.*;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.ParametricFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.autonomy.abc.selenium.query.QueryResult;
import com.autonomy.abc.selenium.auth.HsodUser;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.fields.*;
import com.hp.autonomy.hod.client.config.HodServiceConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;

import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasClass;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTagName;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.Matchers.*;
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
    public void testPdfContentTypeValue(){
        checkContentTypeFilter("APPLICATION/PDF", "pdf");
    }

    @Test
    public void testHtmlContentTypeValue(){
        checkContentTypeFilter("TEXT/HTML", "html");
    }

    private void checkContentTypeFilter(String filterType, String extension) {
        Query query = new Query("red star")
                .withFilter(new ParametricFilter("Content Type", filterType));
        findService.search(query);
        for(String type : results.getDisplayedDocumentsDocumentTypes()){
            assertThat(type, containsString(extension));
        }
    }

    @Test
    public void testFilteringByParametricValues(){
        findService.search("Alexis");
        findPage.waitForParametricValuesToLoad();

        int expectedResults = plainTextCheckbox().getResultsCount();
        plainTextCheckbox().check();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        verifyParametricFields(plainTextCheckbox(), expectedResults);
        verifyTicks(true, false);

        expectedResults = plainTextCheckbox().getResultsCount();
        simpsonsArchiveCheckbox().check();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        verifyParametricFields(plainTextCheckbox(), expectedResults);	//TODO Maybe change plainTextCheckbox to whichever has the higher value??
        verifyTicks(true, true);

        plainTextCheckbox().uncheck();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        expectedResults = simpsonsArchiveCheckbox().getResultsCount();
        verifyParametricFields(simpsonsArchiveCheckbox(), expectedResults);
        verifyTicks(false, true);
    }

    private void verifyParametricFields(FindParametricCheckbox checked, int expectedResults){
        Waits.loadOrFadeWait();
        int resultsTotal = results.getResultTitles().size();
        int checkboxResults = checked.getResultsCount();

        verifyThat(resultsTotal, is(Math.min(expectedResults, 30)));
        verifyThat(checkboxResults, is(expectedResults));
    }

    private void verifyTicks(boolean plainChecked, boolean simpsonsChecked){
        verifyThat(plainTextCheckbox().isChecked(), is(plainChecked));
        verifyThat(simpsonsArchiveCheckbox().isChecked(), is(simpsonsChecked));
    }

    private FindParametricCheckbox simpsonsArchiveCheckbox(){
        return results.parametricTypeCheckbox("Source Connector", "SIMPSONSARCHIVE");
    }

    private FindParametricCheckbox plainTextCheckbox(){
        return results.parametricTypeCheckbox("Content Type", "TEXT/PLAIN");
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() {
        findService.search("wolf");
        results.parametricTypeCheckbox("Content Type", "TEXT/HTML").check();
        Waits.loadOrFadeWait();
        results.parametricTypeCheckbox("Content Type", "TEXT/HTML").uncheck();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testSearch(){
        findService.search("Red");
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }


    @Test
    @KnownBug("CCUK-3641")
    public void testAuthor(){
        String author = "FIFA.COM";

        findService.search(new Query("football")
                .withFilter(new IndexFilter("Fifa"))
                .withFilter(new ParametricFilter("Author", author)));

        assertThat(results.resultsDiv(), not(containsText(Errors.Find.GENERAL)));

        List<FindResult> searchResults = results.getResults();

        for(int i = 0; i < 6; i++){
            DocumentViewer documentViewer = searchResults.get(i).openDocumentPreview();
            verifyThat(documentViewer.getAuthor(), equalToIgnoringCase(author));
            documentViewer.close();
        }
    }

    @Test
    public void testFilterByIndex(){
        findService.search("Sam");

        QueryResult queryResult = results.searchResult(1);
        String titleString = queryResult.getTitleString();

        DocumentViewer docViewer = queryResult.openDocumentPreview();
        Index index = docViewer.getIndex();

        docViewer.close();

        findPage.filterBy(new IndexFilter(index));

        assertThat(results.searchResult(1).getTitleString(), is(titleString));
    }

    @Test
    public void testFilterByIndexOnlyContainsFilesFromThatIndex(){
        findService.search("Happy");

        // TODO: what if this index has no results?
        //This breaks if using default index
        String indexTitle = findPage.getPrivateIndexNames().get(1);
        findPage.filterBy(new IndexFilter(indexTitle));
        DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
        for(int i = 0; i < 5; i++){
            assertThat(docViewer.getIndex().getDisplayName(), is(indexTitle));
            docViewer.next();
        }
    }

    @Test
    public void testQuicklyDoubleClickingIndexDoesNotLeadToError(){
        findService.search("index");
        // async filters
        new IndexFilter(Index.DEFAULT).apply(findPage);
        IndexFilter.PRIVATE.apply(findPage);
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.resultsDiv().getText().toLowerCase(), not(containsString("an error occurred")));
    }

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
        findService.search("Rugby");

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

    @Ignore //TODO seems to have broken
    @Test
    public void testAllParametricFieldsAreShown() throws HodErrorException {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setProxy(new HttpHost("proxy.sdc.hp.com", 8080));


        final HodServiceConfig config = new HodServiceConfig.Builder("https://api.int.havenondemand.com")
                .setHttpClient(httpClientBuilder.build()) // use a custom Apache HttpClient - useful if you're behind a proxy
                .build();

        final AuthenticationService authenticationService = new AuthenticationServiceImpl(config);
        final RetrieveIndexFieldsService retrieveIndexFieldsService = new RetrieveIndexFieldsServiceImpl(config);

        final TokenProxy tokenProxy = authenticationService.authenticateApplication(
                new ApiKey("098b8420-f85f-4164-b8a8-42263e9405a1"),
                "733d64e8-41f7-4c46-a1c8-60d083255159",
                ((HsodUser) getCurrentUser()).getDomain(),
                TokenType.simple
        );

        Set<String> parametricFields = new HashSet<>();

        findService.search("Something");

        for (String indexName : findPage.getPrivateIndexNames()) {
            RetrieveIndexFieldsResponse retrieveIndexFieldsResponse = retrieveIndexFieldsService.retrieveIndexFields(tokenProxy,
                    new ResourceIdentifier(((HsodUser)getCurrentUser()).getDomain(), indexName), new RetrieveIndexFieldsRequestBuilder().setFieldType(FieldType.parametric));

            parametricFields.addAll(retrieveIndexFieldsResponse.getAllFields());
        }

        for(String field : parametricFields) {
            try {
                assertThat(results.parametricContainer(field), displayed());
            } catch (ElementNotVisibleException | NotFoundException e) {
                fail("Could not find field '"+field+"'");
            }
        }
    }

    @Test
    @KnownBug("CSA-1767 - footer not hidden properly")
    public void testViewDocumentsOpenFromFind(){
        findService.search("Review");

        for(FindResult result : results.getResults(5)){
            try {
                DocumentViewer docViewer = result.openDocumentPreview();
                verifyDocumentViewer(docViewer);
                docViewer.close();
            } catch (WebDriverException e){
                fail("Could not click on preview button - most likely CSA-1767");
            }
        }
    }

    @Test
    public void testViewDocumentsOpenWithArrows(){
        findService.search("Review");

        DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
        for(int i = 0; i < 5; i++) {
            verifyDocumentViewer(docViewer);
            docViewer.next();
        }
    }

    private void verifyDocumentViewer(DocumentViewer docViewer) {
        final Frame frame = new Frame(getWindow(), docViewer.frame());

        verifyThat("document visible", docViewer, displayed());
        verifyThat("next button visible", docViewer.nextButton(), displayed());
        verifyThat("previous button visible", docViewer.prevButton(), displayed());

        frame.activate();

        Locator errorHeader = new Locator()
                .withTagName("h1")
                .containingText("500");
        Locator errorBody = new Locator()
                .withTagName("h2")
                .containingCaseInsensitive("error");
        verifyThat("no backend error", frame.content().findElements(errorHeader), empty());
        verifyThat("no view server error", frame.content().findElements(errorBody), empty());
        frame.deactivate();
    }

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
    public void testFileTypes(){
        findService.search("love ");

        for(FileType f : FileType.values()) {
            findPage.filterBy(new ParametricFilter("Content Type",f.getSidebarString()));

            for(FindResult result : results.getResults()){
                assertThat(result.icon().getAttribute("class"), containsString(f.getFileIconString()));
            }

            findPage.filterBy(new ParametricFilter("Content Type",f.getSidebarString()));
        }
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
        String searchTerm = "Tiger";

        findService.search(searchTerm);

        for(WebElement searchElement : getDriver().findElements(By.xpath("//*[not(self::h4) and contains(text(),'" + searchTerm + "')]"))){
            if(searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText(), containsString(searchTerm));
            }
            verifyThat(searchElement, not(hasTagName("a")));
            verifyThat(searchElement, hasClass("search-text"));
        }
    }

    @Test
    @KnownBug({"CSA-1726", "CSA-1763"})
    public void testPublicIndexesVisibleNotSelectedByDefault(){
        findService.search("Marina and the Diamonds");

        verifyThat("public indexes are visible", findPage.indexesTree().publicIndexes(), not(emptyIterable()));
        verifyThat(findPage.getSelectedPublicIndexes(), empty());
    }

    @Test
    @KnownBug("CSA-2082")
    public void testAutoScroll(){
        findService.search("my very easy method just speeds up naming ");

        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(30), lessThanOrEqualTo(60)));

        scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(60), lessThanOrEqualTo(90)));

        List<String> titles = results.getResultTitles();
        Set<String> titlesSet = new HashSet<>(titles);

        verifyThat("No duplicate titles", titles.size(), is(titlesSet.size()));
    }

    @Test
    public void testViewportSearchResultNumbers(){
        findService.search("Messi");

        results.getResult(1).openDocumentPreview();
        verifyDocViewerTotalDocuments(30);

        scrollToBottom();
        results.getResult(31).openDocumentPreview();
        verifyDocViewerTotalDocuments(60);

        scrollToBottom();
        results.getResult(61).openDocumentPreview();
        verifyDocViewerTotalDocuments(90);
    }

    @Test
    @KnownBug("CCUK-3647")
    public void testLessThan30ResultsDoesntAttemptToLoadMore() {
        findService.search(new Query("roland garros")
                .withFilter((new IndexFilter("fifa"))));

        results.getResult(1).openDocumentPreview();
        verifyDocViewerTotalDocuments(lessThanOrEqualTo(30));

        scrollToBottom();
        verifyThat(results.resultsDiv(), not(containsText("results found")));
    }

    @Test
    public void testBetween30And60Results(){
        findService.search(new Query("idol")
                .withFilter(new IndexFilter("sitesearch")));

        scrollToBottom();
        results.getResult(1).openDocumentPreview();
        verifyDocViewerTotalDocuments(lessThanOrEqualTo(60));

        Waits.loadOrFadeWait();

        verifyThat(results.resultsDiv(), containsText("No more results found"));
    }

    @Test
    public void testNoResults(){
        findService.search("thissearchwillalmostcertainlyreturnnoresults");

        verifyThat(results.resultsDiv(), containsText("No results found"));

        scrollToBottom();

        int occurrences = StringUtils.countMatches(results.resultsDiv().getText(), "results found");
        verifyThat("Only one message showing at the bottom of search results", occurrences, is(1));
    }

    private void scrollToBottom() {
        DriverUtil.scrollToBottom(getDriver());
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
    }

    private void verifyDocViewerTotalDocuments(int docs){
        verifyDocViewerTotalDocuments(is(docs));
    }

    private void verifyDocViewerTotalDocuments(Matcher matcher){
        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        verifyThat(docViewer.getTotalDocumentsNumber(), matcher);
        docViewer.close();
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
    public void testOpenDocumentFromSearch(){
        findService.search("Refuse to Feel");

        for(int i = 1; i <= 5; i++){
            Window original = getWindow();
            FindResult result = results.getResult(i);
            String reference = result.getReference();
            result.title().click();
            Waits.loadOrFadeWait();
            Window newWindow = getMainSession().switchWindow(getMainSession().countWindows() - 1);

            verifyThat(getDriver().getCurrentUrl(), containsString(reference));

            newWindow.close();
            original.activate();
        }
    }

    @Test
    @KnownBug("CSA-1767 - footer not hidden properly")
    @RelatedTo({"CSA-946", "CSA-1656", "CSA-1657", "CSA-1908"})
    public void testDocumentPreview(){
        Index index = new Index("fifa");
        findService.search(new Query("document preview").withFilter(new IndexFilter(index)));

        SharedPreviewTests.testDocumentPreviews(getMainSession(), results.getResults(5), index);
    }

    private enum FileType {
        HTML("TEXT/HTML","html"),
        PDF("APPLICATION/PDF","pdf"),
        PLAIN("TEXT/PLAIN","file");

        private final String sidebarString;
        private final String fileIconString;

        FileType(String sidebarString, String fileIconString){
            this.sidebarString = sidebarString;
            this.fileIconString = fileIconString;
        }

        public String getFileIconString() {
            return fileIconString;
        }

        public String getSidebarString() {
            return sidebarString;
        }
    }
}
