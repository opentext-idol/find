package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.*;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.*;
import com.autonomy.abc.shared.QueryTestHelper;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
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
    public void testPdfContentTypeValue(){
        checkContentTypeFilter("APPLICATION/PDF", "pdf");
    }

    @Test
    public void testHtmlContentTypeValue(){
        checkContentTypeFilter("TEXT/HTML", "html");
    }

    private void checkContentTypeFilter(String filterType, String extension) {
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
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
            return results.parametricTypeCheckbox("Source Connector", "SIMPSONSARCHIVE");
        }
        else{
            return results.parametricTypeCheckbox("SOURCE","GOOGLE");
        }
    }
    private FindParametricCheckbox checkbox2(){
        if(getConfig().getType()==ApplicationType.HOSTED){
            return results.parametricTypeCheckbox("Content Type", "TEXT/PLAIN");
        }
        else{
            return results.parametricTypeCheckbox("CATEGORY","ENTERTAINMENT");
        }
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() {
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
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
    public void testFilterByDatabase(){
        findService.search("face");
        QueryResult queryResult = results.searchResult(1);
        String titleString = queryResult.getTitleString();
        showDocumentPreview(1);
        Index index=getPreviewIndex();
        findPage.filterBy(new DatabaseFilter(index));
        assertThat(results.searchResult(1).getTitleString(), is(titleString));
    }

    @Test
    public void testShowDocumentPreview(){
        findService.search("cake");
        showDocumentPreview(1);
        Waits.loadOrFadeWait();
        assertThat("Preview is not loading",!(findPage.findElement(By.cssSelector(".view-server-loading-indicator")).isDisplayed()));
        WebElement previewContents= findPage.findElement(By.className("preview-mode-contents"));
        assertThat("There is content in preview",previewContents.getText(),not(isEmptyOrNullString()));
        assertThat("Index displayed",getPreviewIndex().getDisplayName(),not(nullValue()));
        assertThat("Reference displayed",getPreviewRef(),not(nullValue()));

        Frame previewFrame = new Frame(getWindow(), findPage.findElement(By.tagName("iframe")));

        String frameText=previewFrame.getText();
        verifyThat("Preview document has content",frameText,not(isEmptyOrNullString()));

        assertThat("Preview document has an error",previewFrame.getText(),not(containsString("error")));

        hideDocumentPreview();
        assertThat("There is no content in preview",frameText,isEmptyOrNullString());
    }

    private void showDocumentPreview(int i){
        FindResult findResult = results.searchResult(i);
        findResult.title().click();
    }
    private void hideDocumentPreview(){
        findPage.findElement(By.tagName("i")).click();
    }

    private Index getPreviewIndex() {
        new WebDriverWait(getDriver(),15).until(ExpectedConditions.visibilityOf(findPage.findElement(By.xpath("//th[contains(text(),'Index')]/following-sibling::td[@class='break-all']"))));
        return new Index(findPage.findElement(By.xpath("//th[contains(text(),'Index')]/following-sibling::td[@class='break-all']")).getText());
    }

    private String getPreviewRef(){
        return findPage.findElement(By.xpath("//th[contains(text(),'Reference')]/following-sibling::td[@class='break-all']")).getText();
    }

    //move
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

    //move
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
    public void testFilteredByDatabaseOnlyHasFilesFromDatabase(){
        findService.search("Sad");
        showDocumentPreview(1);
        String chosenIndex = getPreviewIndex().getDisplayName();
        hideDocumentPreview();
        findPage.filterBy(new DatabaseFilter(chosenIndex));
        for (int i=1; i<6; i++){
            showDocumentPreview(i);
            assertThat(getPreviewIndex().getDisplayName(),is(chosenIndex));
        }
    }

    //move
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
    public void testQuicklyDoubleClickingDatabaseNotCauseError(){
        findService.search("wookie");
        for(int i=0;i<2;i++) {
            new DatabaseFilter("Wookiepedia").apply(findPage);
        }
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.resultsDiv().getText().toLowerCase(), not(containsString("an error occurred")));

    }

    //Correctly failing
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

    //move
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
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
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

    //Correctly failing
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
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
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

    //Following 3 correctly failing
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

    @Test
    @KnownBug({"CSA-1726", "CSA-1763"})
    public void testPublicIndexesVisibleNotSelectedByDefault(){
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
        findService.search("Marina and the Diamonds");

        verifyThat("public indexes are visible", findPage.indexesTree().publicIndexes(), not(emptyIterable()));
        verifyThat(findPage.getSelectedPublicIndexes(), empty());
    }

    //correctly failing last part because some titles are duplicates
    @Test
    @KnownBug("CSA-2082")
    public void testAutoScroll(){
        findService.search("abysmal atrocious");
        findPage.findElement(By.className("highlight-result-entities")).click();
        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(30), lessThanOrEqualTo(60)));

        scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(60), lessThanOrEqualTo(90)));

        List<String> titles = results.getResultTitles();
        Set<String> titlesSet = new HashSet<>(titles);

        verifyThat("No duplicate titles", titles.size(), is(titlesSet.size()));
    }

    //move
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


    //move
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
    public void testFewerThan30ResultsNoLoadingAttempt(){
        findService.search(new Query("oesophageal"));

        verifyThat(results.getResults().size(),lessThanOrEqualTo(30));
        findPage.findElement(By.className("highlight-result-entities")).click();

        scrollToBottom();
        verifyThat(results.resultsDiv(),containsText("No more results found"));
    }

    //move
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

        verifyThat(results.resultsDiv(), either(containsText("No results found")).or(containsText("No more results found")));

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
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
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

    //move
    @Test
    @KnownBug("CSA-1767 - footer not hidden properly")
    @RelatedTo({"CSA-946", "CSA-1656", "CSA-1657", "CSA-1908"})
    public void testDocumentPreview(){
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
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
