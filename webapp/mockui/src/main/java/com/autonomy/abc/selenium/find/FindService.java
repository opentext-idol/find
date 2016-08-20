package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.find.results.SimilarDocumentsView;
import com.autonomy.abc.selenium.query.AggregateQueryFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;

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
        elementFactory.getTopNavBar().search(query.getTerm());
        elementFactory.getFilterPanel().waitForIndexes();
        findPage.filterBy(new AggregateQueryFilter(query.getFilters()));
        return elementFactory.getResultsPage();
    }

    public SimilarDocumentsView goToSimilarDocuments(final int resultNumber) {
        final ResultsView resultsPage = elementFactory.getResultsPage();
        resultsPage.getResult(resultNumber).similarDocuments().click();
        resultsPage.waitForResultsToLoad();
        return elementFactory.getSimilarDocumentsView();
    }

}
