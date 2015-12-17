package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.search.SearchPage;

public class SearchService extends ServiceBase {
    public SearchService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    public SearchPage search(final SearchQuery query) {
        getBody().getTopNavBar().search(query.getSearchTerm());
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.filterBy(new AggregateSearchFilter(query.getFilters()));
        return searchPage;
    }

    public SearchPage search(String term) {
        return search(new SearchQuery(term));
    }
}
