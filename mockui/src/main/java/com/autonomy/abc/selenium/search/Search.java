package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.search.SearchPage;

import java.util.ArrayList;
import java.util.List;

public class Search {
    private String searchTerm;
    private AppBody body;
    private ElementFactory elementFactory;
    private List<SearchFilter> searchFilters = new ArrayList<>();

    public Search(AppBody body, ElementFactory elementFactory, String searchTerm) {
        this.searchTerm = searchTerm;
        this.body = body;
        this.elementFactory = elementFactory;
    }

    public Search applyFilter(SearchFilter filter) {
        searchFilters.add(filter);
        return this; // allows chaining
    }

    public SearchPage go() {
        body.getTopNavBar().search(searchTerm);
        SearchPage searchPage = elementFactory.getSearchPage();
        for (SearchFilter filter : searchFilters) {
            filter.apply(searchPage);
        }
        searchPage.waitForSearchLoadIndicatorToDisappear();
        return searchPage;
    }

}
