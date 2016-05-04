package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IndexFilter extends DatabaseFilter{

    public final static IndexFilter PUBLIC = new PublicIndexFilter();
    public final static IndexFilter PRIVATE = new PrivateIndexFilter();

    public IndexFilter(String index){
        super(index);
    }
    public IndexFilter(Index index){
        super(index);
    }
    public IndexFilter(Collection<String> indexes){
        super(indexes);
    }

    private static class PublicIndexFilter extends IndexFilter {
        private PublicIndexFilter() {
            super("Public");
        }

        //@Override
        protected void apply(IndexFilter.Filterable page) {
            page.indexesTree().privateIndexes().deselect();
            page.indexesTree().publicIndexes().select();
        }
    }

    private static class PrivateIndexFilter extends IndexFilter {
        private PrivateIndexFilter() {
            super("Private");
        }

//        @Override
        protected void apply(IndexFilter.Filterable page) {
            page.indexesTree().privateIndexes().select();
            page.indexesTree().publicIndexes().deselect();
        }
    }

    public interface Filterable extends QueryFilter.Filterable {
        IndexesTree indexesTree();
    }
}
