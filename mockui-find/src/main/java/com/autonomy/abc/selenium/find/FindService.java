package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.query.AggregateQueryFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;

public class FindService implements QueryService<FindResultsPage> {
    private final FindElementFactory elementFactory;
    private final FindPage findPage;

    public FindService(FindApplication<?> find) {
        elementFactory = find.elementFactory();
        findPage = elementFactory.getFindPage();
    }

    @Override
    public FindResultsPage search(String query){
        return search(new Query(query));
    }

    @Override
    public FindResultsPage search(final Query query) {
        elementFactory.getTopNavBar().search(query.getTerm());
        findPage.waitForIndexes();
        findPage.filterBy(new AggregateQueryFilter(query.getFilters()));
        return elementFactory.getResultsPage();
    }

    public SimilarDocumentsView goToSimilarDocuments(final int resultNumber) {
        FindResultsPage resultsPage = findPage.getResultsPage();
        resultsPage.getResult(resultNumber).similarDocuments().click();
        resultsPage.waitForResultsToLoad();
        return elementFactory.getSimilarDocumentsView();
    }
}
