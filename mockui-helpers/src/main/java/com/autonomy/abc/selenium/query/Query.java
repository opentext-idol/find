package com.autonomy.abc.selenium.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Query {
    private final String term;
    private final List<QueryFilter> filters;

    public Query(String searchTerm) {
        term = searchTerm;
        filters = new ArrayList<>();
    }

    public Query withFilter(QueryFilter filter) {
        filters.add(filter);
        return this;
    }

    public List<QueryFilter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "Query<" + getTerm() + "|" + getFilters() + ">";
    }
}
