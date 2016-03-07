package com.autonomy.abc.selenium.query;

public class AggregateSearchFilter implements SearchFilter {
    private final Iterable<SearchFilter> filters;

    public AggregateSearchFilter(Iterable<SearchFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void apply(Filterable searchBase) {
        for (SearchFilter filter : filters) {
            filter.apply(searchBase);
        }
    }

    @Override
    public String toString() {
        return filters.toString();
    }
}
