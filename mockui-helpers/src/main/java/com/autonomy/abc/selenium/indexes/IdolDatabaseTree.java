package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;

// decorate an IndexesTree - on-prem all indexes (databases) are private
public class IdolDatabaseTree extends IndexesTree {
    public IdolDatabaseTree(IndexesTree tree) {
        super(tree.allIndexes());
    }

    private IdolDatabaseTree(IndexCategoryNode node) {
        super(node);
    }

    @Override
    public IndexCategoryNode publicIndexes() {
        throw new UnsupportedOperationException("no public indexes on-prem");
    }

    @Override
    public IndexCategoryNode privateIndexes() {
        return allIndexes();
    }

    @Override
    protected void expandAll() {
        allIndexes().expand();
    }

    public static class Factory implements ParametrizedFactory<IndexCategoryNode, IndexesTree> {
        @Override
        public IndexesTree create(IndexCategoryNode context) {
            return new IdolDatabaseTree(context);
        }
    }
}
