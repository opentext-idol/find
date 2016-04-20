package com.autonomy.abc.selenium.query;

public interface QueryFilter {
    void apply(Filterable searchBase);

    interface Filterable {
        void filterBy(QueryFilter filter);
    }
}
