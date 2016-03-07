package com.autonomy.abc.selenium.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchQuery {
    private String term;
    private List<SearchFilter> filters;

    public SearchQuery(String searchTerm) {
        term = searchTerm;
        filters = new ArrayList<>();
    }

    public SearchQuery withFilter(SearchFilter filter) {
        filters.add(filter);
        return this;
    }

    public List<SearchFilter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public String getSearchTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "SearchQuery<" + getSearchTerm() + "|" + getFilters() + ">";
    }
}
