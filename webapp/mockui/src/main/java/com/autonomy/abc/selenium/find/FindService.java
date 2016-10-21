package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.find.results.SimilarDocumentsView;
import com.autonomy.abc.selenium.query.AggregateQueryFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;
import com.hp.autonomy.frontend.selenium.util.Waits;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FindService implements QueryService<ResultsView> {
    private final FindElementFactory elementFactory;
    private final FindPage findPage;

    public FindService(final FindApplication<?> find) {
        elementFactory = find.elementFactory();
        findPage = elementFactory.getFindPage();
    }

    @Override
    public ResultsView search(final String query){
        return search(new Query(query));
    }

    @Override
    public ResultsView search(final Query query) {
        submitSearch(query.getTerm());
        elementFactory.getFilterPanel().waitForIndexes();
        findPage.filterBy(new AggregateQueryFilter(query.getFilters()));
        return elementFactory.getResultsPage();
    }

    protected void submitSearch(final String term) {
        elementFactory.getSearchBox().setValue(term);
        Waits.loadOrFadeWait();
        elementFactory.getSearchBox().submit();
    }

    public SimilarDocumentsView goToSimilarDocuments(final int resultNumber) {
        final ResultsView resultsPage = elementFactory.getResultsPage();
        resultsPage.getResult(resultNumber).similarDocuments().click();
        SimilarDocumentsView similarDocuments = elementFactory.getSimilarDocumentsView();
        similarDocuments.waitForLoad();
        return similarDocuments;
    }

    /**
     * @param query The query text
     * @return The URL of the search page for the given query text, relative to the application context path
     */
    public String getQueryUrl(final String query) {
        try {
            return "public/search/query/" + URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }
}
