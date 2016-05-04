package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.DatabasesTree;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DatabaseFilter implements QueryFilter{

    private Set<String> databases;
    public final static DatabaseFilter ALL = new AllIndexFilter();
    public final static DatabaseFilter NONE = new EmptyIndexFilter();

    public DatabaseFilter(String index) {
        databases = new HashSet<>();
        databases.add(index);
    }

    public DatabaseFilter(Collection<String> databases) {
        this.databases = new HashSet<>(databases);
    }

    public DatabaseFilter(Index index){
        databases = new HashSet<>();
        databases.add(index.getName());
    }

    @Override
    public final void apply(QueryFilter.Filterable page) {
        if (page instanceof DatabaseFilter.Filterable) {
            apply((DatabaseFilter.Filterable) page);
        }
    }

    protected void apply(DatabaseFilter.Filterable page) {
        NONE.apply(page);
        IndexesTree databasesTree = page.databasesTree();
        for (String index : databases) {
            databasesTree.select(index);
        }
    }

    @Override
    public String toString() {
        return "IndexFilter:" + databases;
    }

    private static class AllIndexFilter extends DatabaseFilter {
        private AllIndexFilter() {
            super("All");
        }

        @Override
        public void apply(DatabaseFilter.Filterable page) {
            page.databasesTree().allIndexes().select();
        }
    }

    private static class EmptyIndexFilter extends DatabaseFilter {
        private EmptyIndexFilter() {
            super("None");
        }

        @Override
        protected void apply(DatabaseFilter.Filterable page) {
            page.databasesTree().allIndexes().select();
            page.databasesTree().allIndexes().deselect();
        }
    }

    public interface Filterable extends QueryFilter.Filterable {
        DatabasesTree databasesTree();
    }


}


