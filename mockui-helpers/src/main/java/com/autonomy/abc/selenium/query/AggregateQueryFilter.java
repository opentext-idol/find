package com.autonomy.abc.selenium.query;

public class AggregateQueryFilter implements QueryFilter {
    private final Iterable<QueryFilter> filters;

    public AggregateQueryFilter(final Iterable<QueryFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void apply(final Filterable searchBase) {
        for (final QueryFilter filter : filters) {
            filter.apply(searchBase);
        }
    }

    @Override
    public String toString() {
        return filters.toString();
    }
}
