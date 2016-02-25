package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.search.AggregateSearchFilter;
import com.autonomy.abc.selenium.search.SearchQuery;

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
        findPage.search(query.getSearchTerm());
        findPage.filterBy(new AggregateSearchFilter(query.getFilters()));
        return findPage.getResultsPage();
    }

    public SimilarDocumentsView goToSimilarDocuments(final int resultNumber) {
        FindResultsPage resultsPage = findPage.getResultsPage();
        resultsPage.getResult(resultNumber).similarDocuments().click();
        resultsPage.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        return elementFactory.getSimilarDocumentsView();
    }
}
