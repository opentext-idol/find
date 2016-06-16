package com.autonomy.abc.selenium.indexes.tree;

import com.autonomy.abc.selenium.indexes.Index;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IndexesTree implements Iterable<IndexNodeElement> {
    private final IndexCategoryNode allIndexesNode;

    protected IndexesTree(final IndexCategoryNode node) {
        allIndexesNode = node;
    }

    public void select(final Index index) {
        select(index.getName());
    }

    public void select(final String indexName) {
        expandAll();
        allIndexes().find(indexName).select();
    }

    public void deselect(final Index index) {
        deselect(index.getName());
    }

    public void deselect(final String indexName) {
        expandAll();
        allIndexes().find(indexName).deselect();
    }

    protected void expandAll() {
        allIndexes().expand();
        publicIndexes().expand();
        publicIndexes().seeMore();
        privateIndexes().expand();
    }

    public boolean isSelected(final Index index) {
        return isSelected(index.getName());
    }

    public boolean isSelected(final String indexName) {
        return allIndexes().find(indexName).isSelected();
    }

    public List<Index> getSelected() {
        final List<Index> selected = new ArrayList<>();
        for (final IndexNodeElement node : this) {
            if (node.isSelected()) {
                selected.add(new Index(node.getName()));
            }
        }
        return selected;
    }

    public IndexCategoryNode allIndexes() {
        return allIndexesNode;
    }

    public IndexCategoryNode publicIndexes() {
        return allIndexes().findCategory("public");
    }

    public IndexCategoryNode privateIndexes() {
        return allIndexes().findCategory("private");
    }

    @Override
    public Iterator<IndexNodeElement> iterator() {
        return allIndexes().iterator();
    }

    public static class Factory implements ParametrizedFactory<IndexCategoryNode, IndexesTree> {
        @Override
        public IndexesTree create(final IndexCategoryNode context) {
            return new IndexesTree(context);
        }
    }
}
