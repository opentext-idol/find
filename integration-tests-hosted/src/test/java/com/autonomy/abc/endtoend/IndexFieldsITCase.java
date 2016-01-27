package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.*;
import com.autonomy.abc.selenium.util.Errors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.UUID;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;

@RelatedTo("CSA-2056")
public class IndexFieldsITCase extends HostedTestBase {
    private final Logger logger = LoggerFactory.getLogger(IndexFieldsITCase.class);

    private final Index index;
    private final String ingestUrl = "www.havenondemand.com";
    private final String indexFieldName = "description";
    private final String indexFieldValue = "speech recognition";
    private final String parametricFieldName = "publisher";
    private final String parametricFieldValue = "HPE Haven OnDemand";
    private final ParametricFilter parametricFilter = new ParametricFilter(parametricFieldName, parametricFieldValue);
    private final FieldTextFilter matchFilter = new FieldTextFilter("MATCH{" + parametricFieldValue + "}:" + parametricFieldName);

    private IndexService indexService;
    private SearchService searchService;
    private SearchPage searchPage;
    private Find find;

    public IndexFieldsITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));

        String indexName = UUID.randomUUID().toString().replace('-','a');
        index = new Index(indexName)
                .withIndexFields(Collections.singleton(indexFieldName))
                .withParametricFields(Collections.singleton(parametricFieldName));
    }

    @Before
    public void setUp() {
        indexService = getApplication().createIndexService(getElementFactory());
        searchService = getApplication().createSearchService(getElementFactory());

        indexService.setUpIndex(index);
        logger.info(index + " was created");
    }

    @After
    public void tearDown() {
        indexService.deleteAllIndexes();
    }

    @Test
    @KnownBug({"CSA-1618", "CSA-2055"})
    public void testIndexFields() {
        ingestDocument();
        verifyIndexSearch();
        verifyParametricSearch();
        verifyFind();
    }

    private void ingestDocument() {
        IndexesDetailPage detailPage = indexService.goToDetails(index);
        logger.info("ingesting document " + ingestUrl);
        detailPage.addSiteToIndex(ingestUrl);
    }

    private void verifyIndexSearch() {
        logSearch(new SearchQuery("*").withFilter(new IndexFilter(index)));
        assertThat(searchPage.getHeadingResultsCount(), greaterThan(0));

        logSearch(indexFieldValue);
        verifyFirstSearchResult();

        logSearch("\"" + indexFieldValue + "\":" + indexFieldName);
        verifyFirstSearchResult();

        logSearch("\"" + indexFieldValue + "\":title");
        verifyThat(searchPage.getHeadingResultsCount(), is(0));
        verifyThat(searchPage, containsText(Errors.Search.NO_RESULTS));
    }

    private void verifyParametricSearch() {
        logSearch(new SearchQuery("*").withFilter(new IndexFilter(index)));
        assertThat(searchPage.getHeadingResultsCount(), greaterThan(0));

        applyParametricFilter(searchPage);
        verifyFirstSearchResult();

        logSearch(new SearchQuery("*").withFilter(matchFilter));
        verifyFirstSearchResult();
    }

    private void logSearch(String query) {
        logSearch(new SearchQuery(query));
    }

    private void logSearch(SearchQuery query) {
        logger.info("searching for " + query);
        boolean quick = true;
        try {
            searchPage = searchService.search(query);
        } catch (TimeoutException e) {
            quick = false;
            searchPage = getElementFactory().getSearchPage();
            searchPage.waitForSearchLoadIndicatorToDisappear();
        }
        verifyThat("search responded within a reasonable time", quick);
    }

    private void verifyFirstSearchResult() {
        boolean noError = verifyThat(searchPage, not(containsText(Errors.Search.HOD)));
        // results count not shown if backend error
        boolean hasResults = noError && verifyThat(searchPage.getHeadingResultsCount(), greaterThan(0));
        if (noError && hasResults) {
            searchPage.getSearchResult(1).title().click();
            DocumentViewer viewer = DocumentViewer.make(getDriver());
            verifyThat(viewer.getReference(), containsString(ingestUrl));
            viewer.close();
        }
        // clear the filters
        getDriver().navigate().refresh();
    }

    private void verifyFind() {
        Window first = getMainSession().getActiveWindow();
        Window second = getMainSession().openWindow(config.getFindUrl());
        try {
            second.activate();
            find = getElementFactory().getFindPage();

            logFind("\"" + indexFieldValue + "\":" + indexFieldName);
            verifyFirstFindResult();

            logFind("*");
            applyParametricFilter(find);
            verifyFirstFindResult();
        } finally {
            second.close();
            first.activate();
        }
    }

    private void logFind(String query) {
        logger.info("finding " + query);
        boolean quick = true;
        try {
            find.search(query);
        } catch (TimeoutException e) {
            quick = false;
            find.getResultsPage().waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        }
        verifyThat("find responded within a reasonable time", quick);
    }

    private void verifyFirstFindResult() {
        FindResultsPage resultsPage = find.getResultsPage();
        if (verifyThat("has results", resultsPage.results(), not(empty()))) {
            verifyThat(resultsPage.getSearchResultReference(1), containsString(ingestUrl));

            resultsPage.searchResultTitle(1).click();
            DocumentViewer viewer = DocumentViewer.make(getDriver());
            verifyThat(viewer.getIndex(), is(index.getDisplayName()));
            verifyThat(viewer.getReference(), containsString(ingestUrl));
            viewer.close();
        }
    }

    private void applyParametricFilter(SearchFilter.Filterable filterable) {
        logger.info("filtering by " + parametricFilter);
        boolean success = true;
        try {
            filterable.filterBy(parametricFilter);
        } catch (NoSuchElementException e) {
            success = false;
        }
        verifyThat("able to filter by " + parametricFilter, success);
    }

}
