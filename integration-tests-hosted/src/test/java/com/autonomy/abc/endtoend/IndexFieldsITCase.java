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

import java.util.Collections;
import java.util.UUID;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;

@RelatedTo("CSA-2056")
public class IndexFieldsITCase extends HostedTestBase {
    private final Index index;
    private final String ingestUrl = "www.havenondemand.com";
    private final String indexFieldName = "description";
    private final String indexFieldValue = "speech recognition";
    private final String parametricFieldName = "publisher";
    private final String parametricFieldValue = "HPE Haven OnDemand";
    private final ParametricFilter parametricFilter = new ParametricFilter(parametricFieldName, parametricFieldValue);


    private boolean indexWasCreated = false;
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
        indexWasCreated = true;
    }

    @After
    public void tearDown() {
        if (indexWasCreated) {
            indexService.deleteIndex(index);
        }
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
        detailPage.addSiteToIndex(ingestUrl);

    }

    private void verifyIndexSearch() {
        searchPage = searchService.search(new SearchQuery("*").withFilter(new IndexFilter(index)));
        assertThat(searchPage.getHeadingResultsCount(), greaterThan(0));

        searchPage = searchService.search(indexFieldValue);
        verifyFirstSearchResult();

        searchPage = searchService.search("\"" + indexFieldValue + "\":" + indexFieldName);
        verifyFirstSearchResult();

        searchPage = searchService.search("\"" + indexFieldValue + "\":title");
        verifyThat(searchPage.getHeadingResultsCount(), is(0));
        verifyThat(searchPage, containsText(Errors.Search.NO_RESULTS));
    }

    private void verifyParametricSearch() {
        searchPage = searchService.search(new SearchQuery("*").withFilter(new IndexFilter(index)));
        verifyThat(searchPage.getHeadingResultsCount(), greaterThan(0));

        applyParametricFilter(searchPage);
        verifyFirstSearchResult();

        searchPage = searchService.search(new SearchQuery("*").withFilter(new FieldTextFilter("MATCH{" + parametricFieldValue + "}:" + parametricFieldName)));
        verifyFirstSearchResult();
    }

    private void verifyFirstSearchResult() {
        if (verifyThat(searchPage.getHeadingResultsCount(), greaterThan(0))) {
            searchPage.searchResult(1).click();
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
            find.search("\"" + indexFieldValue + "\":" + indexFieldName);
            verifyFirstFindResult();

            find.search("*");
            applyParametricFilter(find);
            verifyFirstFindResult();
        } finally {
            second.close();
            first.activate();
        }
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
        boolean success = true;
        try {
            filterable.filterBy(parametricFilter);
        } catch (NoSuchElementException e) {
            success = false;
        }
        verifyThat("able to filter by " + parametricFilter, success);
    }

}
