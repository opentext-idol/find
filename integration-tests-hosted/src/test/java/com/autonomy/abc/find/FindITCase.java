package com.autonomy.abc.find;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
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
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
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
    private PromotionsPage promotions;
    private List<String> browserHandles;
    private final String domain = (getConfig().getWebappUrl().contains(".com")) ? "2b7725de-bd04-4341-a4a0-5754f0655de8" : "";
    private final Matcher<String> noDocs = containsString("No results found");
    private PromotionService promotionService;
    private SearchActionFactory searchActionFactory;
    private KeywordService keywordService;

    public FindITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
        keywordService = new KeywordService(getApplication(), getElementFactory());

        promotions = getElementFactory().getPromotionsPage();

        browserHandles = DriverUtil.createAndListWindowHandles(getDriver());

        getDriver().switchTo().window(browserHandles.get(1));
        getDriver().get(config.getFindUrl());
        getDriver().manage().window().maximize();
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
        results.selectContentType("APPLICATION/PDF");
        for(String type : results.getDisplayedDocumentsDocumentTypes()){
            assertThat(type,containsString("pdf"));
        }
    }

    @Test
    public void testHtmlContentTypeValue(){
        find.search("red star");
        results.selectContentType("TEXT/HTML");
        for(String type : results.getDisplayedDocumentsDocumentTypes()){
            assertThat(type,containsString("html"));
        }
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError()  {
        find.search("wolf");
        results.selectContentType("TEXT/HTML");
        results.selectContentType("TEXT/HTML");
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
        getDriver().switchTo().window(browserHandles.get(0));
        body.getTopNavBar().search("stars bbc");
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.sortBy(SearchBase.Sort.RELEVANCE);
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("stars bbc");

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);

        assertThat(results.getResultTitles(), is(searchTitles));
    }

    @Test
    public void testSortByDate(){
        getDriver().switchTo().window(browserHandles.get(0));
        body.getTopNavBar().search("stars bbc");
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.sortBy(SearchBase.Sort.DATE);
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("stars bbc");

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        find.sortBy(SearchBase.Sort.DATE);

        assertThat(results.getResultTitles(), is(searchTitles));
    }

    //TODO ALL RELATED CONCEPTS TESTS - probably better to check if text is not("Loading...") rather than not("")
    @Test
    public void testRelatedConceptsHasResults(){
        find.search("Danye West");
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.RIGHT);
        WebElement relatedConcepts = results.getRelatedConcepts();

        for(WebElement top : relatedConcepts.findElements(By.xpath("./a"))){
            assertThat(top.getText(),not(""));

            WebElement table = top.findElement(By.xpath("./following-sibling::table[1]"));

            for(WebElement entry : table.findElements(By.tagName("a"))){
                assertThat(entry.getText(),not(""));
            }
        }
    }

    @Test
    public void testRelatedConceptsNavigateOnClick(){
        find.search("Red");
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.RIGHT);
        WebElement topRelatedConcept = results.getRelatedConcepts().findElement(By.tagName("a"));

        String concept = topRelatedConcept.getText();

        topRelatedConcept.click();

        assertThat(getDriver().getCurrentUrl(), containsString(concept));
        assertThat(find.getSearchBoxTerm(), containsString(concept));

        hoverOverElement(results.getResultsDiv());

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.RIGHT);

        WebElement tableRelatedConcept = results.getRelatedConcepts().findElement(By.cssSelector("table a"));

        concept = tableRelatedConcept.getText();

        tableRelatedConcept.click();

        assertThat(getDriver().getCurrentUrl(), containsString(concept));
        assertThat(find.getSearchBoxTerm(), containsString(concept));
    }

    @Test
    public void testRelatedConceptsHover(){
        find.search("Find");
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.RIGHT);
        WebElement topRelatedConcept = results.getRelatedConcepts().findElement(By.tagName("a"));
        WebElement tableLink = results.getRelatedConcepts().findElement(By.cssSelector("table a"));

        hoverOverElement(topRelatedConcept);

        WebElement popover = getDriver().findElement(By.className("popover"));

        new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));
        assertThat(popover.getText(), not(""));

        hoverOverElement(results.getResultsDiv());

        new WebDriverWait(getDriver(),2).until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("popover"))));

        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", tableLink);
        hoverOverElement(tableLink);

        WebElement tablePopover = new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOfElementLocated(By.className("popover")));
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(tablePopover, "Loading")));

        assertThat(tablePopover.getText(), not(""));

    }

    @Test
    public void testPinToPosition(){
        Search search = searchActionFactory.makeSearch("red");
        String trigger = "mate";
        PinToPositionPromotion promotion = new PinToPositionPromotion(1, trigger);

        getDriver().switchTo().window(browserHandles.get(0));

        promotionService.deleteAll();

        try {
            String documentTitle = promotionService.setUpPromotion(promotion, search, 1).get(0);

            getDriver().switchTo().window(browserHandles.get(1));
            find.search(trigger);
            assertThat(results.getSearchResultTitle(1).getText(), is(documentTitle));
        } finally {
            getDriver().switchTo().window(browserHandles.get(0));
            promotionService.deleteAll();
        }
    }

    @Test
    public void testPinToPositionThree(){
        Search search = searchActionFactory.makeSearch("red");
        String trigger = "mate";
        PinToPositionPromotion promotion = new PinToPositionPromotion(3, trigger);

        getDriver().switchTo().window(browserHandles.get(0));
        promotionService.deleteAll();

        try {
            String documentTitle = promotionService.setUpPromotion(promotion, search, 1).get(0);

            getDriver().switchTo().window(browserHandles.get(1));
            find.search(trigger);
            assertThat(results.getSearchResultTitle(3).getText(), is(documentTitle));
        } finally {
            getDriver().switchTo().window(browserHandles.get(0));
            promotionService.deleteAll();
        }
    }

    @Test
    public void testSpotlightPromotions(){
        Search search = searchActionFactory.makeSearch("Proper");
        String trigger = "Prim";
        SpotlightPromotion spotlight = new SpotlightPromotion(trigger);

        getDriver().switchTo().window(browserHandles.get(0));
        promotionService.deleteAll();

        try {
            List<String> createdPromotions = promotionService.setUpPromotion(spotlight, search, 3);

            getDriver().switchTo().window(browserHandles.get(1));
            find.search(trigger);

            List<String> findPromotions = results.getPromotionsTitles();

            assertThat(findPromotions, not(empty()));
            assertThat(createdPromotions, everyItem(isIn(findPromotions)));

            promotionShownCorrectly(results.getPromotions());
        } finally {
            getDriver().switchTo().window(browserHandles.get(0));
            promotionService.deleteAll();
        }
    }

    @Test
    public void testStaticPromotions(){
        String title = "TITLE";
        String content = "CONTENT";
        String trigger = "LOVE";
        StaticPromotion promotion = new StaticPromotion(title, content, trigger);

        getDriver().switchTo().window(browserHandles.get(0));
        promotionService.deleteAll();

        try {
            ((HSOPromotionService) promotionService).setUpStaticPromotion(promotion);

            getDriver().switchTo().window(browserHandles.get(1));
            find.search(trigger);
            List<WebElement> promotions = results.getPromotions();

            assertThat(promotions.size(), is(1));
            WebElement staticPromotion = promotions.get(0);
            assertThat(staticPromotion.findElement(By.tagName("h4")).getText(), is(title));
            assertThat(staticPromotion.findElement(By.className("result-summary")).getText(), containsString(content));
            promotionShownCorrectly(staticPromotion);
        } finally {
            getDriver().switchTo().window(browserHandles.get(0));
            promotionService.deleteAll();
        }
    }

    //THIS
    @Test
    public void testDynamicPromotions(){
        int resultsToPromote = 13;
        String trigger = "Rugby";
        DynamicPromotion dynamicPromotion = new DynamicPromotion(resultsToPromote, trigger);
        Search search = searchActionFactory.makeSearch("kittens");

        getDriver().switchTo().window(browserHandles.get(0));
        promotionService.deleteAll();

        try{
            List<String> promotedDocumentTitles = promotionService.setUpPromotion(dynamicPromotion, search, resultsToPromote);

            getDriver().switchTo().window(browserHandles.get(1));
            find.search(trigger);

            verifyThat(promotedDocumentTitles, everyItem(isIn(results.getPromotionsTitles())));

            promotionShownCorrectly(results.getPromotions());
        } finally {
            getDriver().switchTo().window(browserHandles.get(0));
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
    public void testMetadata(){
        find.search("stars");
        find.filterBy(new IndexFilter(Index.DEFAULT));

        results.getSearchResultTitle(1).click();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        String domain = docViewer.getDomain();

        for(WebElement searchResult : results.getResults()){
            String url = searchResult.findElement(By.className("document-reference")).getText();

            try {
                searchResult.findElement(By.tagName("h4")).click();
            } catch (WebDriverException e) {
                fail("Could not click on title - most likely CSA-1767");
            }

            assertThat(docViewer.getDomain(), is(domain));
            assertThat(docViewer.getIndex(), not(isEmptyOrNullString()));
            assertThat(docViewer.getReference(), is(url));
            docViewer.close();
        }
    }

    private void hoverOverElement(WebElement element){
        Actions builder = new Actions(getDriver());
        Dimension dimensions = element.getSize();
        builder.moveToElement(element, dimensions.getWidth() / 2, dimensions.getHeight() / 2);
        Action hover = builder.build();
        hover.perform();
    }

    @Test
    public void testFilterByIndex(){
        find.search("Sam");

        WebElement searchResult = results.getSearchResult(1);
        WebElement title = searchResult.findElement(By.tagName("h4"));

        String titleString = title.getText();
        title.click();

        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        String index = docViewer.getIndex();

        docViewer.close();

        find.filterBy(new IndexFilter(index));

        assertThat(results.getSearchResultTitle(1).getText(), is(titleString));
    }

    @Test
    public void testFilterByIndexOnlyContainsFilesFromThatIndex(){
        find.search("Happy");

        String indexTitle = find.getPrivateIndexNames().get(1);
        find.filterBy(new IndexFilter(indexTitle));
        results.getSearchResultTitle(1).click();
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
        assertThat(results.getResultsDiv().getText().toLowerCase(), not(containsString("an error occurred")));
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

        results.filterByDate(period);
        List<String> preDefinedResults = results.getResultTitles();
        results.filterByDate(getDateString(period), "");
        List<String> customResults = results.getResultTitles();

        assertThat(preDefinedResults, is(customResults));
    }

    private String getDateString (FindResultsPage.DateEnum period) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
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

        return dateFormat.format(cal.getTime());
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
                domain,
                TokenType.simple
        );

        Set<String> parametricFields = new HashSet<>();

        find.search("Something");

        for (String indexName : find.getPrivateIndexNames()) {
            RetrieveIndexFieldsResponse retrieveIndexFieldsResponse = retrieveIndexFieldsService.retrieveIndexFields(tokenProxy,
                    new ResourceIdentifier(domain, indexName), new RetrieveIndexFieldsRequestBuilder().setFieldType(FieldType.parametric));

            parametricFields.addAll(retrieveIndexFieldsResponse.getAllFields());
        }

        for(String field : parametricFields) {
            try {
                assertTrue(results.getParametricContainer(field).isDisplayed());
            } catch (ElementNotVisibleException | NotFoundException e) {
                fail("Could not find field '"+field+"'");
            }
        }
    }

    @Test
    public void testViewDocumentsOpenFromFind(){
        find.search("Review");

        for(WebElement result : results.getResults()){
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
        results.getSearchResultTitle(1).click();
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

        String start = getDateString(FindResultsPage.DateEnum.MONTH);
        String end = getDateString(FindResultsPage.DateEnum.WEEK);

        results.filterByDate(start, end);
        Waits.loadOrFadeWait();
        results.filterByDate(FindResultsPage.DateEnum.CUSTOM); //For some reason doesn't close first time
        results.filterByDate(FindResultsPage.DateEnum.CUSTOM);
        Waits.loadOrFadeWait();
        results.filterByDate(FindResultsPage.DateEnum.CUSTOM);
        Waits.loadOrFadeWait();

        assertThat(results.getStartDateFilter().getAttribute("value"), is(start));
        assertThat(results.getEndDateFilter().getAttribute("value"), is(end));
    }

    @Test
    public void testFileTypes(){
        find.search("love ");

        for(FileType f : FileType.values()) {
            results.selectContentType(f.getSidebarString());

            for(WebElement result : results.getResults()){
                assertThat(result.findElement(By.tagName("i")).getAttribute("class"), containsString(f.getFileIconString()));
            }

            results.selectContentType(f.getSidebarString());
        }
    }

    @Test
    public void testSynonyms() throws InterruptedException {
        String nonsense = "iuhdsafsaubfdja";
        getDriver().switchTo().window(browserHandles.get(0));
        keywordService.deleteAll(KeywordFilter.ALL);

        Waits.loadOrFadeWait();

        getDriver().switchTo().window(browserHandles.get(1));
        find.search(nonsense);

        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText(), noDocs);

        find.search("Cat");
        results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        assertThat(results.getText(), not(noDocs));

        getDriver().switchTo().window(browserHandles.get(0));
        keywordService.addSynonymGroup(Language.ENGLISH, "cat", nonsense);

        getDriver().switchTo().window(browserHandles.get(1));

        find.search("cat");
        String firstTitle = results.getSearchResultTitle(1).getText();

        find.search(nonsense);
        assertThat(results.getText(), not(noDocs));
        verifyThat(results.getSearchResultTitle(1).getText(), is(firstTitle));
    }

    @Test
    public void testBlacklist() throws InterruptedException {
        getDriver().switchTo().window(browserHandles.get(0));
        keywordService.deleteAll(KeywordFilter.ALL);

        Waits.loadOrFadeWait();

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("Cat");

        assertThat(results.getText(), not(noDocs));

        find.search("Holder");

        getDriver().switchTo().window(browserHandles.get(0));

        keywordService.addBlacklistTerms(Language.ENGLISH, "cat");

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("Cat");

        assertThat(results.getText(), noDocs);
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

    /*@Test
    public void testIdolSearchTypes() {
        find.search("leg");

        int initialSearchCount = find.countSearchResults();
        find.search("leg[2:2]");
        Waits.loadOrFadeWait();
        assertThat("Failed with the following search term: leg[2:2]  Search count should have reduced on initial search 'leg'",
                initialSearchCount, greaterThan(find.countSearchResults()));

        search("red");
        searchPage.Waits.loadOrFadeWait();
        initialSearchCount = searchPage.countSearchResults();
        search("red star");
        searchPage.Waits.loadOrFadeWait();
        final int secondSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red star  Search count should have increased on initial search: red",
                initialSearchCount, lessThan(secondSearchCount));

        search("\"red star\"");
        searchPage.Waits.loadOrFadeWait();
        final int thirdSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: '\"red star\"'  Search count should have reduced on initial search: red star",
                secondSearchCount, greaterThan(thirdSearchCount));

        search("red NOT star");
        searchPage.Waits.loadOrFadeWait();
        final int redNotStar = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red NOT star  Search count should have reduced on initial search: red",
                initialSearchCount, greaterThan(redNotStar));

        search("star");
        searchPage.Waits.loadOrFadeWait();
        final int star = searchPage.countSearchResults();

        search("star NOT red");
        searchPage.Waits.loadOrFadeWait();
        final int starNotRed = searchPage.countSearchResults();
        assertThat("Failed with the following search term: star NOT red  Search count should have reduced on initial search: star",
                star, greaterThan(starNotRed));

        search("red OR star");
        searchPage.Waits.loadOrFadeWait();
        assertThat("Failed with the following search term: red OR star  Search count should be the same as initial search: red star",
                secondSearchCount, CoreMatchers.is(searchPage.countSearchResults()));

        search("red AND star");
        searchPage.Waits.loadOrFadeWait();
        final int fourthSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red AND star  Search count should have reduced on initial search: red star",
                secondSearchCount, greaterThan(fourthSearchCount));
        assertThat("Failed with the following search term: red AND star  Search count should have increased on initial search: \"red star\"",
                thirdSearchCount, lessThan(fourthSearchCount));
        assertThat("Sum of 'A NOT B', 'B NOT A' and 'A AND B' should equal 'A OR B' where A is: red  and B is: star",
                fourthSearchCount + redNotStar + starNotRed, CoreMatchers.is(secondSearchCount));
    }*/

    //DUPLICATE
    @Test
    public void testSearchParentheses() {
        List<String> testSearchTerms = Arrays.asList("(",")",") (",")war"); //"()" appears to be fine

        for(String searchTerm : testSearchTerms){
            find.search(searchTerm);

            assertThat(results.getText(), containsString(Errors.Find.GENERAL));
        }
    }

    //DUPLICATE
    @Test
    public void testSearchQuotationMarks() {
        List<String> testSearchTerms = Arrays.asList("\"","","\"word","\" word","\" wo\"rd\""); //"\"\"" seems okay and " "
        for (String searchTerm : testSearchTerms){
            find.search(searchTerm);
            assertThat(results.getText(), Matchers.containsString(Errors.Find.GENERAL));
        }
    }

    //DUPLICATE
    @Test
    public void testWhitespaceSearch() {
        find.search(" ");
        assertThat(results.getText(),containsString(Errors.Find.GENERAL));
    }

    @Test
    //CSA-1577
    public void testClickingCustomDateFilterDoesNotRefreshResults(){
        find.search("O Captain! My Captain!");
        results.filterByDate(FindResultsPage.DateEnum.CUSTOM);
        assertThat(results.getResultsDiv().getText(),not(containsString("Loading")));
    }

    @Test
    public void testSearchTermHighlightedInResults(){
        String searchTerm = "Tiger";

        find.search(searchTerm);

        for(WebElement searchElement : getDriver().findElements(By.xpath("//*[not(self::h4) and contains(text(),'"+searchTerm+"')]"))){
            if(searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText(), containsString(searchTerm));
            }
            verifyThat(searchElement.getTagName(), is("span"));
            verifyThat(searchElement.getAttribute("class"), is("search-text"));

//            WebElement parent = searchElement.findElement(By.xpath(".//.."));
//            verifyThat(parent.getTagName(),is("span"));
//            verifyThat(parent.getAttribute("data-title"), is(searchTerm));
        }

        //TODO what happens when more than one word search term
    }

    @Test
    public void testRelatedConceptsHighlightedInResults(){
        find.search("Tiger");

        for(WebElement relatedConceptLink : results.getRelatedConcepts().findElements(By.tagName("a"))){
            String relatedConcept = relatedConceptLink.getText();
            for(WebElement relatedConceptElement : getDriver().findElements(By.xpath("//*[contains(@class,'middle-container')]//*[not(self::h4) and contains(text(),'"+relatedConcept+"')]"))){
                if(relatedConceptElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                    verifyThat(relatedConceptElement.getText(), containsString(relatedConcept));
                }
                verifyThat(relatedConceptElement.getTagName(), is("a"));
                verifyThat(relatedConceptElement.getAttribute("class"), is("entity-text clickable"));

                WebElement parent = ElementUtil.ancestor(relatedConceptElement, 1);
                verifyThat(parent.getTagName(),is("span"));
                verifyThat(parent.getAttribute("data-title"), is(relatedConcept));
            }
        }
    }

    @Test
    public void testSimilarDocumentsShowUp(){
        find.search("Doe");

        for (WebElement similarResultLink : Lists.reverse(results.getSimilarResultLinks())) {
            similarResultLink.click();

            WebElement popover = results.getPopover();

            new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));

            assertThat(popover.findElement(By.tagName("p")).getText(), not("An error occurred fetching similar documents"));

            for(WebElement similarResult : popover.findElements(By.tagName("li"))){
                assertThat(similarResult.findElement(By.tagName("a")).getText(), not(isEmptyString()));
                assertThat(similarResult.findElement(By.tagName("p")).getText(), not(isEmptyString()));
            }
        }
    }

    @Test
    //CSA1630
    public void testAllPromotedDocumentsHaveTitles(){
        getDriver().switchTo().window(browserHandles.get(0));

        PromotionService promotionService = getApplication().createPromotionService(getElementFactory());

        try {
            promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Tiger"),
                    new Search(getApplication(), getElementFactory(), "scg-2"), 10);

            getDriver().switchTo().window(browserHandles.get(1));

            find.search("Tiger");

            for(String title : results.getPromotionsTitles()){
                assertThat(title, is(not("")));
            }

        } finally {
            getDriver().switchTo().window(browserHandles.get(0));
            promotionService.deleteAll();
        }
    }

    @Test
    //CSA-1763
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
