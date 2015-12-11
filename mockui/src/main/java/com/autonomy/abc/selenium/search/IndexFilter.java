package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexesTree;
import com.autonomy.abc.selenium.page.search.SearchBase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IndexFilter implements SearchFilter {
    private Set<String> indexes;
    public final static IndexFilter ALL = new AllIndexFilter();
    public final static IndexFilter NONE = new EmptyIndexFilter();
    public final static IndexFilter PUBLIC = new PublicIndexFilter();
    public final static IndexFilter PRIVATE = new PrivateIndexFilter();

    public IndexFilter(String index) {
        indexes = new HashSet<>();
        indexes.add(index);
    }

    public IndexFilter(Collection<String> indexes) {
        this.indexes = new HashSet<>(indexes);
    }

    public IndexFilter(Index index){
        indexes = new HashSet<>();
        indexes.add(index.getName());
    }

    @Override
    public void apply(SearchBase searchBase) {
        NONE.apply(searchBase);
        IndexesTree indexesTree = searchBase.indexesTree();
        for (String index : indexes) {
            indexesTree.select(index);
        }
    }

    private static class AllIndexFilter extends IndexFilter {
        private AllIndexFilter() {
            super("All");
        }

        @Override
        public void apply(SearchBase searchBase) {
            searchBase.indexesTree().allIndexes().select();
        }
    }

    private static class EmptyIndexFilter extends IndexFilter {
        private EmptyIndexFilter() {
            super("None");
        }

        @Override
        public void apply(SearchBase searchBase) {
            searchBase.indexesTree().allIndexes().select();
            searchBase.indexesTree().allIndexes().deselect();
        }
    }

    private static class PublicIndexFilter extends IndexFilter {
        private PublicIndexFilter() {
            super("Public");
        }

        @Override
        public void apply(SearchBase searchBase) {
            searchBase.indexesTree().privateIndexes().deselect();
            searchBase.indexesTree().publicIndexes().select();
        }
    }

    private static class PrivateIndexFilter extends IndexFilter {
        private PrivateIndexFilter() {
            super("Private");
        }

        @Override
        public void apply(SearchBase searchBase) {
            searchBase.indexesTree().privateIndexes().select();
            searchBase.indexesTree().publicIndexes().deselect();
        }
    }
}
