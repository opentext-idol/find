package com.autonomy.abc.selenium.query;

public class AggregateQueryFilter implements QueryFilter {
    private final Iterable<QueryFilter> filters;

    public AggregateQueryFilter(Iterable<QueryFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void apply(Filterable searchBase) {
        for (QueryFilter filter : filters) {
            filter.apply(searchBase);
        }
    }

    @Override
    public String toString() {
        return filters.toString();
    }
}
