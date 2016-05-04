package com.autonomy.abc.selenium.indexes.tree;


public class DatabasesTree extends IndexesTree {
    public DatabasesTree(IndexesTree tree) {
            super(tree.allIndexes());
        }

    @Override
    public IndexCategoryNode publicIndexes() {
        return allIndexes();
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
