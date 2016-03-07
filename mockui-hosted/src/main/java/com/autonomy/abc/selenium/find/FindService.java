package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.query.AggregateSearchFilter;
import com.autonomy.abc.selenium.query.SearchQuery;

public class FindService {
    private HSODFindElementFactory elementFactory;
    private FindPage findPage;

    FindService(HSODFind find) {
        elementFactory = find.elementFactory();
        findPage = elementFactory.getFindPage();
    }

    public FindResultsPage search(String query){
        return search(new SearchQuery(query));
    }

    public FindResultsPage search(final SearchQuery query) {
        elementFactory.getTopNavBar().search(query.getSearchTerm());
        findPage.waitForIndexes();
        findPage.filterBy(new AggregateSearchFilter(query.getFilters()));
        return elementFactory.getResultsPage();
    }

    public SimilarDocumentsView goToSimilarDocuments(final int resultNumber) {
        FindResultsPage resultsPage = findPage.getResultsPage();
        resultsPage.getResult(resultNumber).similarDocuments().click();
        resultsPage.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        return elementFactory.getSimilarDocumentsView();
    }
}
