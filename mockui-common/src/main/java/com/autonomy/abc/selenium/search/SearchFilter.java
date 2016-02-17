package com.autonomy.abc.selenium.search;

public interface SearchFilter {
    void apply(Filterable searchBase);

    interface Filterable {
        void filterBy(SearchFilter filter);
    }
}
