package com.autonomy.abc.selenium.query;

public interface SearchFilter {
    void apply(Filterable searchBase);

    interface Filterable {
        void filterBy(SearchFilter filter);
    }
}
