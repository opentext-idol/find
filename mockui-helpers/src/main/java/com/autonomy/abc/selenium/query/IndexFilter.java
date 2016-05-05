package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IndexFilter implements QueryFilter{

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

    public void add(Index index){add(index.getName());
    }
    public void add(String index){indexes.add(index);}

    @Override
    public final void apply(QueryFilter.Filterable page) {
        if (page instanceof IndexFilter.Filterable) {
            apply((IndexFilter.Filterable) page);
        }
    }

    protected void apply(IndexFilter.Filterable page) {
        NONE.apply(page);
        IndexesTree indexesTree = page.indexesTree();
        for (String index : indexes) {
            indexesTree.select(index);
        }
    }

    @Override
    public String toString() {
        return "IndexFilter:" + indexes;
    }

    private static class AllIndexFilter extends IndexFilter {
        private AllIndexFilter() {
            super("All");
        }

        @Override
        public void apply(IndexFilter.Filterable page) {
            page.indexesTree().allIndexes().select();
        }
    }

    private static class EmptyIndexFilter extends IndexFilter {
        private EmptyIndexFilter() {
            super("None");
        }

        @Override
        protected void apply(IndexFilter.Filterable page) {
            page.indexesTree().allIndexes().select();
            page.indexesTree().allIndexes().deselect();
        }
    }

    private static class PublicIndexFilter extends IndexFilter {
        private PublicIndexFilter() {
            super("Public");
        }

        @Override
        protected void apply(IndexFilter.Filterable page) {
            page.indexesTree().privateIndexes().deselect();
            page.indexesTree().publicIndexes().select();
        }
    }

    private static class PrivateIndexFilter extends IndexFilter {
        private PrivateIndexFilter() {
            super("Private");
        }

        @Override
        protected void apply(IndexFilter.Filterable page) {
            page.indexesTree().privateIndexes().select();
            page.indexesTree().publicIndexes().deselect();
        }
    }

    public interface Filterable extends QueryFilter.Filterable {
        IndexesTree indexesTree();
    }
}
