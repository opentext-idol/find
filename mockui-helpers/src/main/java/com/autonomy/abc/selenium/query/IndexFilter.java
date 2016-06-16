package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IndexFilter implements QueryFilter{

    private final Set<String> indexes;
    public static final IndexFilter ALL = new AllIndexFilter();
    public static final IndexFilter NONE = new EmptyIndexFilter();
    public static final IndexFilter PUBLIC = new PublicIndexFilter();
    public static final IndexFilter PRIVATE = new PrivateIndexFilter();

    public IndexFilter(final String index) {
        indexes = new HashSet<>();
        indexes.add(index);
    }

    public IndexFilter(final Collection<String> indexes) {
        this.indexes = new HashSet<>(indexes);
    }

    public IndexFilter(final Index index){
        indexes = new HashSet<>();
        indexes.add(index.getName());
    }

    public void add(final Index index){add(index.getName());
    }
    public void add(final String index){indexes.add(index);}

    @Override
    public final void apply(final QueryFilter.Filterable page) {
        if (page instanceof IndexFilter.Filterable) {
            apply((IndexFilter.Filterable) page);
        }
    }

    protected void apply(final IndexFilter.Filterable page) {
        NONE.apply(page);
        final IndexesTree indexesTree = page.indexesTree();
        for (final String index : indexes) {
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
        public void apply(final IndexFilter.Filterable page) {
            page.indexesTree().allIndexes().select();
        }
    }

    private static class EmptyIndexFilter extends IndexFilter {
        private EmptyIndexFilter() {
            super("None");
        }

        @Override
        protected void apply(final IndexFilter.Filterable page) {
            page.indexesTree().allIndexes().select();
            page.indexesTree().allIndexes().deselect();
        }
    }

    private static class PublicIndexFilter extends IndexFilter {
        private PublicIndexFilter() {
            super("Public");
        }

        @Override
        protected void apply(final IndexFilter.Filterable page) {
            page.indexesTree().privateIndexes().deselect();
            page.indexesTree().publicIndexes().select();
        }
    }

    private static class PrivateIndexFilter extends IndexFilter {
        private PrivateIndexFilter() {
            super("Private");
        }

        @Override
        protected void apply(final IndexFilter.Filterable page) {
            page.indexesTree().privateIndexes().select();
            page.indexesTree().publicIndexes().deselect();
        }
    }

    public interface Filterable extends QueryFilter.Filterable {
        IndexesTree indexesTree();
    }
}
