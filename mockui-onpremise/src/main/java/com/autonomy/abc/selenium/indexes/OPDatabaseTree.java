package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;

// decorate an IndexesTree - on-prem all indexes (databases) are private
public class OPDatabaseTree extends IndexesTree {
    public OPDatabaseTree(IndexesTree tree) {
        super(tree.allIndexes());
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
}
