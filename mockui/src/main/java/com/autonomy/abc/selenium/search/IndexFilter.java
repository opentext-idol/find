package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.Checkbox;
import com.autonomy.abc.selenium.page.search.SearchBase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IndexFilter implements SearchFilter {
    private Set<String> indexes;
    public final static IndexFilter ALL = new AllIndexFilter();
    public final static IndexFilter NONE = new EmptyIndexFilter();

    public IndexFilter(String index) {
        indexes = new HashSet<>();
        indexes.add(index);
    }

    public IndexFilter(Collection<String> indexes) {
        this.indexes = new HashSet<>(indexes);
    }

    @Override
    public void apply(SearchBase searchBase) {
        for (Checkbox checkbox : searchBase.indexList()) {
            if (indexes.contains(checkbox.getName().trim())) {
                checkbox.check();
            } else {
                checkbox.uncheck();
            }
        }
    }

    private static class AllIndexFilter extends IndexFilter {
        private AllIndexFilter() {
            super("All");
        }

        @Override
        public void apply(SearchBase searchBase) {
            searchBase.allIndexesCheckbox().check();
        }
    }

    private static class EmptyIndexFilter extends IndexFilter {
        private EmptyIndexFilter() {
            super("None");
        }

        @Override
        public void apply(SearchBase searchBase) {
            searchBase.allIndexesCheckbox().check();
            searchBase.allIndexesCheckbox().uncheck();
        }
    }
}
