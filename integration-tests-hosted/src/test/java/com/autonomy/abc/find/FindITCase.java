package com.autonomy.abc.find;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.element.FindParametricCheckbox;
import com.autonomy.abc.selenium.application.FindApplication;
import com.autonomy.abc.selenium.application.HSODFindApplication;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.ParametricFilter;
import com.autonomy.abc.selenium.search.StringDateFilter;
import com.autonomy.abc.selenium.util.*;
import com.google.common.collect.Lists;
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
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static com.thoughtworks.selenium.SeleneseTestBase.assertTrue;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.openqa.selenium.lift.Matchers.displayed;

public class FindITCase extends HostedTestBase {
    private Find find;
    private FindResultsPage results;
    private final Matcher<String> noDocs = containsString(Errors.Search.NO_RESULTS);
    private PromotionService<?> promotionService;
    private KeywordService keywordService;
    private Window searchWindow;
    private Window findWindow;
    private FindApplication<?> findApplication;

    public FindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        keywordService = getApplication().createKeywordService(getElementFactory());

        searchWindow = getMainSession().getActiveWindow();
        findWindow = getMainSession().openWindow(config.getFindUrl());
        findApplication = FindApplication.ofType(config.getType());
        find = getElementFactory().getFindPage();
        results = find.getResultsPage();
    }

    @Test
    public void testSendKeys() throws InterruptedException {
        String searchTerm = "Fred is a chimpanzee";
        find.search(searchTerm);
        assertThat(find.getSearchBoxTerm(), is(searchTerm));
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testPdfContentTypeValue(){
        find.search("red star");
        find.filterBy(new ParametricFilter("Content Type", "APPLICATION/PDF"));
        for(String type : results.getDisplayedDocumentsDocumentTypes()){
            assertThat(type,containsString("pdf"));
        }
    }

    @Test
    public void testHtmlContentTypeValue(){
        find.search("red star");
        find.filterBy(new ParametricFilter("Content Type", "TEXT/HTML"));
        for(String type : results.getDisplayedDocumentsDocumentTypes()){
            assertThat(type,containsString("html"));
        }
    }

    @Test
    public void testFilteringByParametricValues(){
        find.search("Alexis");
        find.waitForParametricValuesToLoad();

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
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() throws InterruptedException {
        find.search("wolf");
        results.parametricTypeCheckbox("Content Type", "TEXT/HTML").check();
        Waits.loadOrFadeWait();
        results.parametricTypeCheckbox("Content Type", "TEXT/HTML").uncheck();
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testSearch(){
        find.search("Red");
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testSortByRelevance() {
        searchWindow.activate();
        getElementFactory().getTopNavBar().search("stars bbc");
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.sortBy(SearchBase.Sort.RELEVANCE);
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        findWindow.activate();
        find.search("stars bbc");

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);

        assertThat(results.getResultTitles(), is(searchTitles));
    }

    @Test
    public void testSortByDate(){
        searchWindow.activate();
        getElementFactory().getTopNavBar().search("stars bbc");
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.sortBy(SearchBase.Sort.DATE);
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        findWindow.activate();
        find.search("stars bbc");

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        find.sortBy(SearchBase.Sort.DATE);

        assertThat(results.getResultTitles(), is(searchTitles));
    }

    //TODO ALL RELATED CONCEPTS TESTS - probably better to check if text is not("Loading...") rather than not("")
    @Test
    public void testRelatedConceptsHasResults(){
        find.search("Danye West");
        for (WebElement concept : results.relatedConcepts()) {
            assertThat(concept, hasTextThat(not(isEmptyOrNullString())));
        }
    }

    @Test
    public void testRelatedConceptsNavigateOnClick(){
        find.search("Red");
        WebElement topRelatedConcept = results.relatedConcepts().get(0);
        String concept = topRelatedConcept.getText();

        topRelatedConcept.click();
        assertThat(getDriver().getCurrentUrl(), containsString(concept));
        assertThat(find.getSearchBoxTerm(), containsString(concept));
    }

    @Test
    // CCUK-3498
    public void testRelatedConceptsHover(){
        find.search("Find");
        WebElement popover = results.hoverOverRelatedConcept(0);
        verifyThat(popover, hasTextThat(not(isEmptyOrNullString())));
        verifyThat(popover.getText(), not(containsString("QueryText-Placeholder")));
        results.unhover();
    }

    @Test
    public void testPinToPosition(){
        String search = "red";
        String trigger = "mate";
        PinToPositionPromotion promotion = new PinToPositionPromotion(1, trigger);

        searchWindow.activate();

        promotionService.deleteAll();

        try {
            String documentTitle = promotionService.setUpPromotion(promotion, search, 1).get(0);

            findWindow.activate();
            find.search(trigger);
            assertThat(results.searchResultTitle(1).getText(), is(documentTitle));
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    public void testPinToPositionThree(){
        String search = "red";
        String trigger = "mate";
        PinToPositionPromotion promotion = new PinToPositionPromotion(3, trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try {
            String documentTitle = promotionService.setUpPromotion(promotion, search, 1).get(0);

            findWindow.activate();
            find.search(trigger);
            assertThat(results.searchResultTitle(3).getText(), is(documentTitle));
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    public void testSpotlightPromotions(){
        String search = "Proper";
        String trigger = "Prim";
        SpotlightPromotion spotlight = new SpotlightPromotion(trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try {
            List<String> createdPromotions = promotionService.setUpPromotion(spotlight, search, 3);

            findWindow.activate();
            find.search(trigger);

            List<String> findPromotions = results.getPromotionsTitles();

            assertThat(findPromotions, not(empty()));
            assertThat(createdPromotions, everyItem(isIn(findPromotions)));

            promotionShownCorrectly(results.promotions());
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    public void testStaticPromotions(){
        String title = "TITLE";
        String content = "CONTENT";
        String trigger = "LOVE";
        StaticPromotion promotion = new StaticPromotion(title, content, trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try {
            ((HSOPromotionService) promotionService).setUpStaticPromotion(promotion);

            findWindow.activate();
            find.search(trigger);
            List<WebElement> promotions = results.promotions();

            assertThat(promotions.size(), is(1));
            WebElement staticPromotion = promotions.get(0);
            assertThat(staticPromotion.findElement(By.tagName("h4")).getText(), is(title));
            assertThat(staticPromotion.findElement(By.className("result-summary")).getText(), containsString(content));
            promotionShownCorrectly(staticPromotion);
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    //THIS
    @Test
    public void testDynamicPromotions(){
        int resultsToPromote = 13;
        String search = "kittens";
        String trigger = "Rugby";
        DynamicPromotion dynamicPromotion = new DynamicPromotion(resultsToPromote, trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try{
            List<String> promotedDocumentTitles = promotionService.setUpPromotion(dynamicPromotion, search, resultsToPromote);

            findWindow.activate();
            find.search(trigger);

            verifyThat(promotedDocumentTitles, everyItem(isIn(results.getPromotionsTitles())));

            promotionShownCorrectly(results.promotions());
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    private void promotionShownCorrectly (WebElement promotion) {
        assertThat(promotion.getAttribute("class"), containsString("promoted-document"));
        assertThat(promotion.findElement(By.className("promoted-label")).getText(), containsString("Promoted"));
        assertThat(promotion.findElement(By.className("fa-star")), displayed());
    }

    private void promotionShownCorrectly (List<WebElement> promotions){
        for(WebElement promotion : promotions){
            promotionShownCorrectly(promotion);
        }
    }

    @Test
    //TODO update this based on CSA-1657
    @KnownBug("CSA-1767 - footer not hidden properly")
    public void testMetadata(){
        find.search("stars");
        find.filterBy(new IndexFilter(Index.DEFAULT));

        for(WebElement searchResult : results.results()){
            String url = searchResult.findElement(By.className("document-reference")).getText();

            try {
                searchResult.findElement(By.tagName("h4")).click();
            } catch (WebDriverException e) {
                fail("Could not click on title - most likely CSA-1767");
            }

            DocumentViewer docViewer = DocumentViewer.make(getDriver());

            assertThat(docViewer.getDomain(), is(getCurrentUser().getDomain()));
            assertThat(docViewer.getIndex(), not(isEmptyOrNullString()));
            assertThat(docViewer.getReference(), is(url));
            docViewer.close();
        }
    }

    @Test
    public void testFilterByIndex(){
        find.search("Sam");

        WebElement searchResult = results.searchResult(1);
        WebElement title = searchResult.findElement(By.tagName("h4"));

        String titleString = title.getText();
        title.click();

        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        String index = docViewer.getIndex();

        docViewer.close();

        find.filterBy(new IndexFilter(index));

        assertThat(results.searchResultTitle(1).getText(), is(titleString));
    }

    @Test
    public void testFilterByIndexOnlyContainsFilesFromThatIndex(){
        find.search("Happy");

        // TODO: what if this index has no results?
        String indexTitle = find.getPrivateIndexNames().get(2);
        find.filterBy(new IndexFilter(indexTitle));
        results.searchResultTitle(1).click();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        do{
            assertThat(docViewer.getIndex(), is(indexTitle));
            docViewer.next();
        } while (docViewer.getCurrentDocumentNumber() != 1);
    }

    @Test
    public void testQuicklyDoubleClickingIndexDoesNotLeadToError(){
        find.search("index");
        // async filters
        new IndexFilter(Index.DEFAULT).apply(find);
        IndexFilter.PRIVATE.apply(find);
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
        find.search("Rugby");

        results.toggleDateSelection(period);
        List<String> preDefinedResults = results.getResultTitles();
        find.filterBy(new StringDateFilter().from(getDate(period)));
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
                getCurrentUser().getDomain(),
                TokenType.simple
        );

        Set<String> parametricFields = new HashSet<>();

        find.search("Something");

        for (String indexName : find.getPrivateIndexNames()) {
            RetrieveIndexFieldsResponse retrieveIndexFieldsResponse = retrieveIndexFieldsService.retrieveIndexFields(tokenProxy,
                    new ResourceIdentifier(getCurrentUser().getDomain(), indexName), new RetrieveIndexFieldsRequestBuilder().setFieldType(FieldType.parametric));

            parametricFields.addAll(retrieveIndexFieldsResponse.getAllFields());
        }

        for(String field : parametricFields) {
            try {
                assertTrue(results.parametricContainer(field).isDisplayed());
            } catch (ElementNotVisibleException | NotFoundException e) {
                fail("Could not find field '"+field+"'");
            }
        }
    }

    @Test
    @KnownBug("CSA-1767 - footer not hidden properly")
    public void testViewDocumentsOpenFromFind(){
        find.search("Review");

        for(WebElement result : results.results()){
            try {
                ElementUtil.scrollIntoViewAndClick(result.findElement(By.tagName("h4")), getDriver());
            } catch (WebDriverException e){
                fail("Could not click on title - most likely CSA-1767");
            }

            DocumentViewer docViewer = DocumentViewer.make(getDriver());
            verifyDocumentViewer(docViewer);
            docViewer.close();
        }
    }

    @Test
    public void testViewDocumentsOpenWithArrows(){
        find.search("Review");

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        results.searchResultTitle(1).click();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        do{
            verifyDocumentViewer(docViewer);
            docViewer.next();
        } while (docViewer.getCurrentDocumentNumber() != 1);
    }

    private void verifyDocumentViewer(DocumentViewer docViewer) {
        verifyThat("document visible", docViewer, displayed());
        verifyThat("next button visible", docViewer.nextButton(), displayed());
        verifyThat("previous button visible", docViewer.prevButton(), displayed());

        String handle = getDriver().getWindowHandle();
        getDriver().switchTo().frame(docViewer.frame());

        //TODO these aren't working properly - did Fred not fix these?
        verifyThat("no backend error", getDriver().findElements(new Locator().withTagName("h1").containingText("500")), empty());
        verifyThat("no view server error", getDriver().findElements(new Locator().withTagName("h2").containingCaseInsensitive("error")), empty());
        getDriver().switchTo().window(handle);
    }

    @Test
    public void testDateRemainsWhenClosingAndReopeningDateFilters(){
        find.search("Corbyn");

        Date start = getDate(FindResultsPage.DateEnum.MONTH);
        Date end = getDate(FindResultsPage.DateEnum.WEEK);

        find.filterBy(new StringDateFilter().from(start).until(end));
        Waits.loadOrFadeWait();
        for (int unused = 0; unused < 3; unused++) {
            results.toggleDateSelection(FindResultsPage.DateEnum.CUSTOM);
            Waits.loadOrFadeWait();
        }

        assertThat(find.fromDateInput().getValue(), is(find.formatInputDate(start)));
        assertThat(find.untilDateInput().getValue(), is(find.formatInputDate(end)));
    }

    @Test
    public void testFileTypes(){
        find.search("love ");

        for(FileType f : FileType.values()) {
            find.filterBy(new ParametricFilter("Content Type",f.getSidebarString()));

            for(WebElement result : results.results()){
                assertThat(result.findElement(By.tagName("i")).getAttribute("class"), containsString(f.getFileIconString()));
            }

            find.filterBy(new ParametricFilter("Content Type",f.getSidebarString()));
        }
    }

    @Test
    public void testSynonyms() throws InterruptedException {
        String nonsense = "iuhdsafsaubfdja";
        searchWindow.activate();
        keywordService.deleteAll(KeywordFilter.ALL);

        Waits.loadOrFadeWait();

        findWindow.activate();
        find.search(nonsense);

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText(), noDocs);

        find.search("Cat");
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText(), not(noDocs));

        searchWindow.activate();
        keywordService.addSynonymGroup(Language.ENGLISH, "cat", nonsense);

        /* need a separate session due to caching */
        Session secondSession = getSessionRegistry().startSession(config.getFindUrl());
        try {
            Find otherFind = initialiseSession(secondSession);
            otherFind.search("Cat");
            FindResultsPage otherResults = otherFind.getResultsPage();
            String firstTitle = otherResults.searchResultTitle(1).getText();

            otherFind.search(nonsense);
            assertThat(otherResults.getText(), not(noDocs));
            verifyThat(otherResults.searchResultTitle(1).getText(), is(firstTitle));

        } finally {
            getSessionRegistry().endSession(secondSession);
        }
    }

    @Test
    public void testBlacklist() throws InterruptedException {
        searchWindow.activate();
        keywordService.deleteAll(KeywordFilter.ALL);

        Waits.loadOrFadeWait();

        findWindow.activate();
        find.search("Cat");

        assertThat(results.getText(), not(noDocs));

        find.search("Holder");

        searchWindow.activate();

        keywordService.addBlacklistTerms(Language.ENGLISH, "cat");

        /* need a separate session due to caching */
        Session secondSession = getSessionRegistry().startSession(config.getFindUrl());
        try {
            Find otherFind = initialiseSession(secondSession);
            otherFind.search("Cat");

            assertThat(otherFind.getResultsPage(), hasTextThat(noDocs));
        } finally {
            getSessionRegistry().endSession(secondSession);
        }
    }

    // TODO: this does not belong here
    private Find initialiseSession(Session session) {
        HSOElementFactory otherElementFactory = new HSODFindApplication().createElementFactory(session.getDriver());
        loginTo(otherElementFactory.getFindLoginPage(), session.getDriver(), config.getDefaultUser());
        return otherElementFactory.getFindPage();
    }

    @Test   @Ignore("Not implemented")
    public void testOverlappingSynonyms(){}

    @Test
    public void testBooleanOperators(){
        String termOne = "musketeers";
        String termTwo = "\"dearly departed\"";

        find.search(termOne);
        List<String> musketeersSearchResults = results.getResultTitles();
        int numberOfMusketeersResults = musketeersSearchResults.size();

        find.search(termTwo);
        List<String> dearlyDepartedSearchResults = results.getResultTitles();
        int numberOfDearlyDepartedResults = dearlyDepartedSearchResults.size();

        find.search(termOne + " AND " + termTwo);
        List<String> andResults = results.getResultTitles();
        int numberOfAndResults = andResults.size();

        assertThat(numberOfMusketeersResults,greaterThanOrEqualTo(numberOfAndResults));
        assertThat(numberOfDearlyDepartedResults, greaterThanOrEqualTo(numberOfAndResults));
        String[] andResultsArray = andResults.toArray(new String[andResults.size()]);
        assertThat(musketeersSearchResults, hasItems(andResultsArray));
        assertThat(dearlyDepartedSearchResults, hasItems(andResultsArray));

        find.search(termOne + " OR " + termTwo);
        List<String> orResults = results.getResultTitles();
        Set<String> concatenatedResults = new HashSet<>(ListUtils.union(musketeersSearchResults, dearlyDepartedSearchResults));
        assertThat(orResults.size(), is(concatenatedResults.size()));
        assertThat(orResults, containsInAnyOrder(concatenatedResults.toArray()));

        find.search(termOne + " XOR " + termTwo);
        List<String> xorResults = results.getResultTitles();
        concatenatedResults.removeAll(andResults);
        assertThat(xorResults.size(), is(concatenatedResults.size()));
        assertThat(xorResults, containsInAnyOrder(concatenatedResults.toArray()));

        find.search(termOne + " NOT " + termTwo);
        List<String> notTermTwo = results.getResultTitles();
        Set<String> t1NotT2 = new HashSet<>(concatenatedResults);
        t1NotT2.removeAll(dearlyDepartedSearchResults);
        assertThat(notTermTwo.size(), is(t1NotT2.size()));
        assertThat(notTermTwo, containsInAnyOrder(t1NotT2.toArray()));

        find.search(termTwo + " NOT " + termOne);
        List<String> notTermOne = results.getResultTitles();
        Set<String> t2NotT1 = new HashSet<>(concatenatedResults);
        t2NotT1.removeAll(musketeersSearchResults);
        assertThat(notTermOne.size(), is(t2NotT1.size()));
        assertThat(notTermOne, containsInAnyOrder(t2NotT1.toArray()));
    }

    //DUPLICATE SEARCH TEST (almost)
    @Test
    public void testCorrectErrorMessageDisplayed() {
        //TODO: map error messages to application type

        List<String> boolOperators = Arrays.asList("OR", "WHEN", "SENTENCE", "DNEAR");
        List<String> stopWords = Arrays.asList("a", "the", "of", "SOUNDEX"); //According to IDOL team SOUNDEX isn't considered a boolean operator without brackets

        for (final String searchTerm : boolOperators) {
            find.search(searchTerm);
            verifyThat("Correct error message for searchterm: " + searchTerm, find.getText(), containsString(Errors.Search.OPERATORS));
        }

        for (final String searchTerm : stopWords) {
            find.search(searchTerm);
            verifyThat("Correct error message for searchterm: " + searchTerm, find.getText(), containsString(Errors.Search.STOPWORDS));
        }
    }

    //DUPLICATE SEARCH TEST
    @Test
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        final List<String> hiddenBooleansProximities = Arrays.asList("NOTed", "ANDREW", "ORder", "WHENCE", "SENTENCED", "PARAGRAPHING", "NEARLY", "SENTENCE1D", "PARAGRAPHING", "PARAGRAPH2inG", "SOUNDEXCLUSIVE", "XORING", "EORE", "DNEARLY", "WNEARING", "YNEARD", "AFTERWARDS", "BEFOREHAND", "NOTWHENERED");
        for (final String hiddenBooleansProximity : hiddenBooleansProximities) {
            find.search(hiddenBooleansProximity);
            Waits.loadOrFadeWait();
            assertThat(find.getText(), not(containsString(Errors.Search.GENERAL)));
        }
    }

    //DUPLICATE
    @Test
    public void testSearchParentheses() {
        List<String> testSearchTerms = Arrays.asList("(",")",") (",")war"); //"()" appears to be fine

        for(String searchTerm : testSearchTerms){
            find.search(searchTerm);

            assertThat(results, containsText(Errors.Search.OPERATORS));
        }
    }

    //DUPLICATE
    @Test
    public void testSearchQuotationMarks() {
        List<String> testSearchTerms = Arrays.asList("\"","","\"word","\" word","\" wo\"rd\""); //"\"\"" seems okay and " "
        for (String searchTerm : testSearchTerms){
            find.search(searchTerm);
            assertThat(results, containsText(Errors.Search.QUOTES));
        }
    }

    //DUPLICATE
    @Test
    public void testWhitespaceSearch() {
        find.search(" ");
        assertThat(results, containsText(Errors.Search.STOPWORDS));
    }

    @Test
    @KnownBug("CSA-1577")
    public void testClickingCustomDateFilterDoesNotRefreshResults(){
        find.search("O Captain! My Captain!");
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

        find.search(searchTerm);

        for(WebElement searchElement : getDriver().findElements(By.xpath("//*[not(self::h4) and contains(text(),'" + searchTerm + "')]"))){
            if(searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText(), containsString(searchTerm));
            }
            verifyThat(searchElement, not(hasTagName("a")));
            verifyThat(searchElement, hasClass("search-text"));
        }
    }

    // TODO: testMultiWordSearchTermInResults

    @Test
    public void testRelatedConceptsInResults(){
        find.search("Tiger");

        for(WebElement relatedConceptLink : results.relatedConcepts()){
            String relatedConcept = relatedConceptLink.getText();
            for (WebElement relatedConceptElement : getDriver().findElements(By.xpath("//*[contains(@class,'middle-container')]//*[not(self::h4) and contains(text(),'" + relatedConcept + "')]"))) {
                if (relatedConceptElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                    verifyThat(relatedConceptElement.getText(), containsString(relatedConcept));
                }
                verifyThat(relatedConceptElement, hasTagName("a"));
                verifyThat(relatedConceptElement, hasClass("clickable"));
            }
        }
    }

    @Test
    public void testSimilarDocumentsShowUp(){
        find.search("Doe");

        for (WebElement similarResultLink : Lists.reverse(results.similarResultLinks())) {
            similarResultLink.click();

            WebElement popover = results.popover();

            new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));

            assertThat(popover.findElement(By.tagName("p")).getText(), not("An error occurred fetching similar documents"));

            for(WebElement similarResult : popover.findElements(By.tagName("li"))){
                assertThat(similarResult.findElement(By.tagName("a")).getText(), not(isEmptyString()));
                assertThat(similarResult.findElement(By.tagName("p")).getText(), not(isEmptyString()));
            }
        }
    }

    @Test
    @KnownBug("CSA-1630")
    public void testAllPromotedDocumentsHaveTitles(){
        searchWindow.activate();

        PromotionService promotionService = getApplication().createPromotionService(getElementFactory());

        try {
            promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Tiger"), "scg-2", 10);

            findWindow.activate();

            find.search("Tiger");

            for(String title : results.getPromotionsTitles()){
                assertThat(title, is(not("")));
            }

        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    @KnownBug("CSA-1763")
    public void testPublicIndexesNotSelectedByDefault(){
        find.search("Marina and the Diamonds");

        verifyThat(find.getSelectedPublicIndexes().size(), is(0));
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
