package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.actions.Action;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.search.SearchPage;

import java.util.ArrayList;
import java.util.List;

public class Search implements Action<SearchPage> {
    private String searchTerm;
    private Application application;
    private AppBody body;
    private ElementFactory elementFactory;
    private List<SearchFilter> searchFilters = new ArrayList<>();

    // TODO: split this, Search has two responsibilities
    public Search(Application application, ElementFactory elementFactory, String searchTerm) {
        this.application = application;
        this.elementFactory = elementFactory;
        this.searchTerm = searchTerm;
    }

    private AppBody getBody() {
        if (application == null) {
            return body;
        }
        return application.createAppBody(elementFactory.getDriver());
    }

    public Search applyFilter(SearchFilter filter) {
        searchFilters.add(filter);
        return this; // allows chaining
    }

    public SearchPage apply() {
        getBody().getTopNavBar().search(searchTerm);
        SearchPage searchPage = elementFactory.getSearchPage();
        for (SearchFilter filter : searchFilters) {
            filter.apply(searchPage);
        }
        searchPage.waitForSearchLoadIndicatorToDisappear();
        return searchPage;
    }

}
