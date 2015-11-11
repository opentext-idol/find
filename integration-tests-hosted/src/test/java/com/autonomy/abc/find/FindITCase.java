package com.autonomy.abc.find;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.Input;
import com.autonomy.abc.selenium.find.Service;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.fields.*;
import com.hp.autonomy.hod.client.config.HodServiceConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import org.apache.commons.collections.ListUtils;
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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class FindITCase extends ABCTestBase {
    private FindPage find;
    private Service service;
    private Input input;
    private Logger logger = LoggerFactory.getLogger(FindITCase.class);
    private PromotionsPage promotions;
    private List<String> browserHandles;
    private final String domain = "ce9f1f3d-a780-4793-8a6a-a74b12b7d1ae";
    private final Matcher<String> noDocs = containsString("No results found");
    private PromotionService promotionService;
    private SearchActionFactory searchActionFactory;

    public FindITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());

        promotions = getElementFactory().getPromotionsPage();

        browserHandles = promotions.createAndListWindowHandles();

        getDriver().switchTo().window(browserHandles.get(1));
        getDriver().get(config.getFindUrl());
        getDriver().manage().window().maximize();
        find = ((HSOElementFactory) getElementFactory()).getFindPage();
        input = find.getInput();
        service = find.getService();
    }

    @Test
    public void testSendKeys() throws InterruptedException {
        String searchTerm = "Fred is a chimpanzee";
        find.search(searchTerm);
        assertThat(input.getSearchTerm(), is(searchTerm));
        assertThat(service.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testPdfContentTypeValue(){
        find.search("red star");
        service.selectContentType("APPLICATION/PDF");
        for(String type : service.getDisplayedDocumentsDocumentTypes()){
            assertThat(type,containsString("pdf"));
        }
    }

    @Test
    public void testHtmlContentTypeValue(){
        find.search("red star");
        service.selectContentType("TEXT/HTML");
        for(String type : service.getDisplayedDocumentsDocumentTypes()){
            assertThat(type,containsString("html"));
        }
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError()  {
        find.search("red star");
        service.selectContentType("APPLICATION/PDF");
        service.selectContentType("APPLICATION/PDF");
        assertThat(service.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testSearch(){
        find.search("Red");
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
        assertThat(service.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testSortByRelevance() {
        getDriver().switchTo().window(browserHandles.get(0));
        body.getTopNavBar().search("stars bbc");
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.sortByRelevance();
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("stars bbc");

        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);

        List<String> findSearchTitles = service.getResultTitles();

        for(int i = 0; i < 30; i++){
            assertThat(findSearchTitles.get(i), is(searchTitles.get(i)));
        }
    }

    @Test
    public void testSortByDate(){
        getDriver().switchTo().window(browserHandles.get(0));
        body.getTopNavBar().search("stars bbc");
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.sortByDate();
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("stars bbc");

        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
        find.sortByDate();

        List<String> findSearchTitles = service.getResultTitles();

        for(int i = 0; i < 30; i++){
            assertThat(findSearchTitles.get(i), is(searchTitles.get(i)));
        }
    }

    //TODO ALL RELATED CONCEPTS TESTS - probably better to check if text is not("Loading...") rather than not("")
    @Test
    public void testRelatedConceptsHasResults(){
        find.search("Danye West");
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.RIGHT);
        WebElement relatedConcepts = service.getRelatedConcepts();

        int i = 1;
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
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.RIGHT);
        WebElement topRelatedConcept = service.getRelatedConcepts().findElement(By.tagName("a"));

        String concept = topRelatedConcept.getText();

        topRelatedConcept.click();

        assertThat(getDriver().getCurrentUrl(), containsString(concept));
        assertThat(input.getSearchTerm(), containsString(concept));

        hoverOverElement(service.getResultsDiv());

        service.waitForSearchLoadIndicatorToDisappear(Service.Container.RIGHT);

        WebElement tableRelatedConcept = service.getRelatedConcepts().findElement(By.cssSelector("table a"));

        concept = tableRelatedConcept.getText();

        tableRelatedConcept.click();

        assertThat(getDriver().getCurrentUrl(), containsString(concept));
        assertThat(input.getSearchTerm(), containsString(concept));
    }

    @Test
    public void testRelatedConceptsHover(){
        find.search("Find");
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.RIGHT);
        WebElement topRelatedConcept = service.getRelatedConcepts().findElement(By.tagName("a"));
        WebElement tableLink = service.getRelatedConcepts().findElement(By.cssSelector("table a"));

        hoverOverElement(topRelatedConcept);

        WebElement popover = getDriver().findElement(By.className("popover"));

        new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));
        assertThat(popover.getText(), not(""));

        hoverOverElement(service.getResultsDiv());

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
            assertThat(service.getSearchResultTitle(1).getText(), is(documentTitle));
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
            assertThat(service.getSearchResultTitle(3).getText(), is(documentTitle));
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

            List<String> findPromotions = service.getPromotionsTitles();

            assertThat(findPromotions, not(empty()));
            assertThat(createdPromotions, everyItem(isIn(findPromotions)));

            for(WebElement promotion : service.getPromotions()){
                promotionShownCorrectly(promotion);
            }
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
            List<WebElement> promotions = service.getPromotions();

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

            verifyThat(promotedDocumentTitles, everyItem(isIn(service.getPromotionsTitles())));

            for(WebElement promotion : service.getPromotions()){
                promotionShownCorrectly(promotion);
            }

        } finally {
            getDriver().switchTo().window(browserHandles.get(0));
            promotionService.deleteAll();
        }
    }

    private void promotionShownCorrectly (WebElement promotion) {
        assertThat(promotion.getAttribute("class"),containsString("promoted-document"));
        assertThat(promotion.findElement(By.className("promoted-label")).getText(), containsString("Promoted"));
        assertTrue(promotion.findElement(By.className("icon-star")).isDisplayed());
    }

    @Test
    public void testCheckMetadata(){
        find.search("stars");

        for(WebElement searchResult : service.getResults()){
            String url = searchResult.findElement(By.className("document-reference")).getText();

            find.scrollIntoViewAndClick(searchResult.findElement(By.tagName("h4")));

            WebElement metadata = service.getViewMetadata();

            assertThat(metadata.findElement(By.xpath(".//tr[1]/td")).getText(),is(domain));
            assertThat(metadata.findElement(By.xpath(".//tr[2]/td")).getText(),is(not("")));
            assertThat(metadata.findElement(By.xpath(".//tr[3]/td")).getText(),is(url));

            service.closeViewBox();
            find.loadOrFadeWait();
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

        WebElement searchResult = service.getSearchResult(1);
        WebElement title = searchResult.findElement(By.tagName("h4"));

        String titleString = title.getText();
        title.click();

        WebElement metadata = service.getViewMetadata();
        String index = metadata.findElement(By.xpath(".//tr[2]/td")).getText();

        service.closeViewBox();
        service.loadOrFadeWait();

        service.filterByIndex(domain,index);
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);

        assertThat(service.getSearchResultTitle(1).getText(), is(titleString));
    }

    @Test
    public void testFilterByIndexOnlyContainsFilesFromThatIndex(){
        find.search("Happy");

        service.filterByIndex(domain, Index.PDF.title);
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
        service.getSearchResultTitle(1).click();
        do{
            assertThat(service.getViewMetadata().findElement(By.xpath(".//tr[2]/td")).getText(), is(Index.PDF.title));
            service.viewBoxNextButton().click();
        } while (!service.cBoxFirstDocument());
    }

    @Test
    public void testQuicklyDoubleClickingIndexDoesNotLeadToError(){
        find.search("index");
        service.filterByIndex(domain, Index.DEFAULT.title);
        service.filterByIndex(domain, Index.DEFAULT.title);
        assertThat(service.getResultsDiv().getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testPreDefinedWeekHasSameResultsAsCustomWeek(){
        preDefinedDateFiltersVersusCustomDateFilters(Service.DateEnum.WEEK);
    }

    @Test
    public void testPreDefinedMonthHasSameResultsAsCustomMonth(){
        preDefinedDateFiltersVersusCustomDateFilters(Service.DateEnum.MONTH);
    }

    @Test
    public void testPreDefinedYearHasSameResultsAsCustomYear(){
        preDefinedDateFiltersVersusCustomDateFilters(Service.DateEnum.YEAR);
    }

    private void preDefinedDateFiltersVersusCustomDateFilters(Service.DateEnum period){
        find.search("Rugby");

        service.filterByDate(period);
        List<String> preDefinedResults = service.getResultTitles();
        service.filterByDate(getDateString(period),"");
        List<String> customResults = service.getResultTitles();

        assertThat(preDefinedResults.size(), is(customResults.size()));

        for(int i = 0; i < preDefinedResults.size(); i++){
            assertThat(preDefinedResults.get(i), is(customResults.get(i)));
        }
    }

    private String getDateString (Service.DateEnum period) {
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

        for(Index i : Index.values()) {
            RetrieveIndexFieldsResponse retrieveIndexFieldsResponse = retrieveIndexFieldsService.retrieveIndexFields(tokenProxy,
                    new ResourceIdentifier(domain, i.title), new RetrieveIndexFieldsRequestBuilder().setFieldType(FieldType.parametric));

            parametricFields.addAll(retrieveIndexFieldsResponse.getAllFields());
        }

        find.search("Something");

        for(String field : parametricFields) {
            try {
                assertTrue(service.getParametricContainer(field).isDisplayed());
            } catch (ElementNotVisibleException | NotFoundException e) {
                fail("Could not find field '"+field+"'");
            }
        }
    }

    @Test
    public void testViewDocumentsOpenFromFind(){
        find.search("Review");

        for(WebElement result : service.getResults()){
            service.scrollIntoViewAndClick(result.findElement(By.tagName("h4")));

            new WebDriverWait(getDriver(),20).until(new WaitForCBoxLoadIndicatorToDisappear());
            assertThat(service.getCBoxLoadedContent().getText(), not(containsString("500")));

            assertTrue(service.viewBoxNextButton().isDisplayed());
            assertTrue(service.viewBoxPrevButton().isDisplayed());
            assertTrue(service.colourBox().isDisplayed());

            service.closeViewBox();
            find.loadOrFadeWait();
        }
    }

    private class WaitForCBoxLoadIndicatorToDisappear implements ExpectedCondition {
        @Override
        public Object apply(Object o) {
            return !getDriver().findElement(By.cssSelector("#cboxLoadedContent .icon-spin")).isDisplayed();
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }

    @Test
    public void testViewDocumentsOpenWithArrows(){
        find.search("Review");

        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
        service.getSearchResultTitle(1).click();
        do{
            assertThat(service.getCBoxLoadedContent().getText(),not(containsString("500")));
            assertTrue(service.viewBoxNextButton().isDisplayed());
            assertTrue(service.viewBoxPrevButton().isDisplayed());
            assertTrue(service.colourBox().isDisplayed());
            service.viewBoxNextButton().click();
        } while (!service.cBoxFirstDocument());
    }

    @Test
    public void testDateRemainsWhenClosingAndReopeningDateFilters(){
        find.search("Corbyn");

        String start = getDateString(Service.DateEnum.MONTH);
        String end = getDateString(Service.DateEnum.WEEK);

        service.filterByDate(start,end);
        find.loadOrFadeWait();
        service.filterByDate(Service.DateEnum.CUSTOM); //For some reason doesn't close first time
        service.filterByDate(Service.DateEnum.CUSTOM);
        find.loadOrFadeWait();
        service.filterByDate(Service.DateEnum.CUSTOM);
        find.loadOrFadeWait();

        assertThat(service.getStartDateFilter().getAttribute("value"), is(start));
        assertThat(service.getEndDateFilter().getAttribute("value"), is(end));
    }

    @Test
    public void testFileTypes(){
        find.search("love ");

        for(FileType f : FileType.values()) {
            service.selectContentType(f.getSidebarString());

            for(WebElement result : service.getResults()){
                assertThat(result.findElement(By.tagName("i")).getAttribute("class"), containsString(f.getFileIconString()));
            }

            service.selectContentType(f.getSidebarString());
        }
    }

    @Test
    public void testSynonyms() throws InterruptedException {
        getDriver().switchTo().window(browserHandles.get(0));
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
        keywordsPage.deleteKeywords();

        find.loadOrFadeWait();

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("iuhdsafsaubfdja");

        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
        assertThat(service.getText(), noDocs);

        find.search("Cat");
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
        assertThat(service.getText(), not(noDocs));
        String firstTitle = service.getSearchResultTitle(1).getText();

        getDriver().switchTo().window(browserHandles.get(0));
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

        keywordsPage = getElementFactory().getKeywordsPage();
        keywordsPage.createNewKeywordsButton().click();

        CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createNewKeywordsPage.createSynonymGroup("cat iuhdsafsaubfdja", "English");

        getElementFactory().getSearchPage();

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("iuhdsafsaubfdja");

        assertThat(service.getText(), not(noDocs));
        verifyThat(service.getSearchResultTitle(1).getText(), is(firstTitle));
    }

    @Test
    public void testBlacklist() throws InterruptedException {
        getDriver().switchTo().window(browserHandles.get(0));
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
        keywordsPage.deleteKeywords();

        find.loadOrFadeWait();

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("Cat");

        assertThat(service.getText(), not(noDocs));

        find.search("Holder");

        getDriver().switchTo().window(browserHandles.get(0));

        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

        keywordsPage = getElementFactory().getKeywordsPage();
        keywordsPage.createNewKeywordsButton().click();

        CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createNewKeywordsPage.createBlacklistedTerm("Cat", "English");

        getElementFactory().getKeywordsPage();

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("Cat");

        assertThat(service.getText(), noDocs);
    }

    @Test
    public void testOverlappingSynonyms(){

    }

    @Test
    public void testBooleanOperators(){
        String termOne = "musketeers";
        String termTwo = "\"dearly departed\"";

        find.search(termOne);
        List<String> musketeersSearchResults = service.getResultTitles();
        int numberOfMusketeersResults = musketeersSearchResults.size();

        find.search(termTwo);
        List<String> dearlyDepartedSearchResults = service.getResultTitles();
        int numberOfDearlyDepartedResults = dearlyDepartedSearchResults.size();

        find.search(termOne + " AND " + termTwo);
        List<String> andResults = service.getResultTitles();
        int numberOfAndResults = andResults.size();

        assertThat(numberOfMusketeersResults,greaterThanOrEqualTo(numberOfAndResults));
        assertThat(numberOfDearlyDepartedResults, greaterThanOrEqualTo(numberOfAndResults));
        String[] andResultsArray = andResults.toArray(new String[andResults.size()]);
        assertThat(musketeersSearchResults, hasItems(andResultsArray));
        assertThat(dearlyDepartedSearchResults,hasItems(andResultsArray));

        find.search(termOne + " OR " + termTwo);
        List<String> orResults = service.getResultTitles();
        Set<String> concatenatedResults = new HashSet<String>(ListUtils.union(musketeersSearchResults, dearlyDepartedSearchResults));
        assertThat(orResults.size(), is(concatenatedResults.size()));
        assertThat(orResults, containsInAnyOrder(concatenatedResults.toArray()));

        find.search(termOne + " XOR " + termTwo);
        List<String> xorResults = service.getResultTitles();
        concatenatedResults.removeAll(andResults);
        assertThat(xorResults.size(), is(concatenatedResults.size()));
        assertThat(xorResults, containsInAnyOrder(concatenatedResults.toArray()));

        find.search(termOne + " NOT " + termTwo);
        List<String> notTermTwo = service.getResultTitles();
        Set<String> t1NotT2 = new HashSet<>(concatenatedResults);
        t1NotT2.removeAll(dearlyDepartedSearchResults);
        assertThat(notTermTwo.size(), is(t1NotT2.size()));
        assertThat(notTermTwo, containsInAnyOrder(t1NotT2.toArray()));

        find.search(termTwo + " NOT " + termOne);
        List<String> notTermOne = service.getResultTitles();
        Set<String> t2NotT1 = new HashSet<>(concatenatedResults);
        t2NotT1.removeAll(musketeersSearchResults);
        assertThat(notTermOne.size(), is(t2NotT1.size()));
        assertThat(notTermOne,containsInAnyOrder(t2NotT1.toArray()));
    }

    //DUPLICATE SEARCH TEST (almost)
    @Test
    public void testCorrectErrorMessageDisplayed() {
        //TODO: map error messages to application type

        List<String> boolOperators = Arrays.asList("OR", "WHEN", "SENTENCE", "DNEAR");
        List<String> stopWords = Arrays.asList("a", "the", "of", "SOUNDEX"); //According to IDOL team SOUNDEX isn't considered a boolean operator without brackets

        for (final String searchTerm : boolOperators) {
            find.search(searchTerm);
            assertThat("Correct error message not present for searchterm: " + searchTerm, find.getText(), containsString("An error occurred retrieving results"));
        }

        for (final String searchTerm : stopWords) {
            find.search(searchTerm);
            assertThat("Correct error message not present for searchterm: " + searchTerm, find.getText(), containsString("No results found"));
        }
    }

    //DUPLICATE SEARCH TEST
    @Test
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        final List<String> hiddenBooleansProximities = Arrays.asList("NOTed", "ANDREW", "ORder", "WHENCE", "SENTENCED", "PARAGRAPHING", "NEARLY", "SENTENCE1D", "PARAGRAPHING", "PARAGRAPH2inG", "SOUNDEXCLUSIVE", "XORING", "EORE", "DNEARLY", "WNEARING", "YNEARD", "AFTERWARDS", "BEFOREHAND", "NOTWHENERED");
        for (final String hiddenBooleansProximity : hiddenBooleansProximities) {
            find.search(hiddenBooleansProximity);
            find.loadOrFadeWait();
            assertThat(find.getText(), not(containsString("An error occurred retrieving results")));
        }
    }

    /*@Test
    public void testIdolSearchTypes() {
        find.search("leg");

        int initialSearchCount = find.countSearchResults();
        find.search("leg[2:2]");
        find.loadOrFadeWait();
        assertThat("Failed with the following search term: leg[2:2]  Search count should have reduced on initial search 'leg'",
                initialSearchCount, greaterThan(find.countSearchResults()));

        search("red");
        searchPage.loadOrFadeWait();
        initialSearchCount = searchPage.countSearchResults();
        search("red star");
        searchPage.loadOrFadeWait();
        final int secondSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red star  Search count should have increased on initial search: red",
                initialSearchCount, lessThan(secondSearchCount));

        search("\"red star\"");
        searchPage.loadOrFadeWait();
        final int thirdSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: '\"red star\"'  Search count should have reduced on initial search: red star",
                secondSearchCount, greaterThan(thirdSearchCount));

        search("red NOT star");
        searchPage.loadOrFadeWait();
        final int redNotStar = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red NOT star  Search count should have reduced on initial search: red",
                initialSearchCount, greaterThan(redNotStar));

        search("star");
        searchPage.loadOrFadeWait();
        final int star = searchPage.countSearchResults();

        search("star NOT red");
        searchPage.loadOrFadeWait();
        final int starNotRed = searchPage.countSearchResults();
        assertThat("Failed with the following search term: star NOT red  Search count should have reduced on initial search: star",
                star, greaterThan(starNotRed));

        search("red OR star");
        searchPage.loadOrFadeWait();
        assertThat("Failed with the following search term: red OR star  Search count should be the same as initial search: red star",
                secondSearchCount, CoreMatchers.is(searchPage.countSearchResults()));

        search("red AND star");
        searchPage.loadOrFadeWait();
        final int fourthSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red AND star  Search count should have reduced on initial search: red star",
                secondSearchCount, greaterThan(fourthSearchCount));
        assertThat("Failed with the following search term: red AND star  Search count should have increased on initial search: \"red star\"",
                thirdSearchCount, lessThan(fourthSearchCount));
        assertThat("Sum of 'A NOT B', 'B NOT A' and 'A AND B' should equal 'A OR B' where A is: red  and B is: star",
                fourthSearchCount + redNotStar + starNotRed, CoreMatchers.is(secondSearchCount));
    }*/

    String findErrorMessage = "An error occurred retrieving results";

    //DUPLICATE
    @Test
    public void testSearchParentheses() {
        List<String> testSearchTerms = Arrays.asList("(",")",") (",")war"); //"()" appears to be fine

        for(String searchTerm : testSearchTerms){
            find.search(searchTerm);

            assertThat(service.getText(), containsString(findErrorMessage));
        }
    }

    //DUPLICATE
    @Test
    public void testSearchQuotationMarks() {
        List<String> testSearchTerms = Arrays.asList("\"","","\"word","\" word","\" wo\"rd\""); //"\"\"" seems okay and " "
        for (String searchTerm : testSearchTerms){
            find.search(searchTerm);
            assertThat(service.getText(), Matchers.containsString(findErrorMessage));
        }
    }

    //DUPLICATE
    @Test
    public void testWhitespaceSearch() {
        find.search(" ");
        assertThat(service.getText(),containsString(findErrorMessage));
    }

    @Test
    //CSA-1577
    public void testClickingCustomDateFilterDoesNotRefreshResults(){
        find.search("O Captain! My Captain!");
        service.filterByDate(Service.DateEnum.CUSTOM);
        assertThat(service.getResultsDiv().getText(),not(containsString("Loading")));
    }

    @Test
    public void testSearchTermHighlightedInResults(){
        String searchTerm = "Tiger";

        find.search(searchTerm);

        for(WebElement searchElement : getDriver().findElements(By.xpath("//*[not(self::h4) and contains(text(),'"+searchTerm+"')]"))){
            if(searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText(), containsString(searchTerm));
            }
            verifyThat(searchElement.getTagName(), is("a"));
            verifyThat(searchElement.getAttribute("class"), is("query-text"));

            WebElement parent = searchElement.findElement(By.xpath(".//.."));
            verifyThat(parent.getTagName(),is("span"));
            verifyThat(parent.getAttribute("class"), containsString("label"));
        }

        //TODO what happens when more than one word search term
    }

    @Test
    public void testRelatedConceptsHighlightedInResults(){
        find.search("Tiger");

        for(WebElement relatedConceptLink : service.getRelatedConcepts().findElements(By.tagName("a"))){
            String relatedConcept = relatedConceptLink.getText();
            for(WebElement relatedConceptElement : getDriver().findElements(By.xpath("//*[contains(@class,'middle-container')]//*[not(self::h4) and contains(text(),'"+relatedConcept+"')]"))){
                if(relatedConceptElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                    verifyThat(relatedConceptElement.getText(), containsString(relatedConcept));
                }
                verifyThat(relatedConceptElement.getTagName(), is("a"));
                verifyThat(relatedConceptElement.getAttribute("class"), is("query-text"));

                WebElement parent = relatedConceptElement.findElement(By.xpath(".//.."));
                verifyThat(parent.getTagName(),is("span"));
                verifyThat(parent.getAttribute("class"), containsString("label"));
            }
        }
    }

    @Test
    public void testSimilarDocumentsShowUp(){
        find.search("Doe");

        for (WebElement similarResultLink : service.getSimilarResultLinks()){
            new Actions(getDriver()).moveByOffset(-50,-50).build().perform();
            service.loadOrFadeWait();
            similarResultLink.click();

            WebElement popover = service.getPopover();

            new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover,"Loading")));

            assertThat(popover.findElement(By.tagName("p")).getText(),not("An error occurred fetching similar documents"));

            for(WebElement similarResult : popover.findElements(By.tagName("li"))){
                assertThat(similarResult.findElement(By.tagName("h5")).getText(),not(isEmptyString()));
                assertThat(similarResult.findElement(By.tagName("p")).getText(),not(isEmptyString()));
            }
        }
    }

    private enum Index {
        DEFAULT("default_index"),
        PDF("pdf");

        private final String title;

        Index(String index){
            this.title = index;
        }


        public String getTitle() {
            return title;
        }
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
