package com.autonomy.abc.selenium.indexes.tree;

import com.autonomy.abc.selenium.indexes.Index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IndexesTree implements Iterable<NodeElement> {
    private final IndexCategoryNode allIndexesNode;

    public IndexesTree(IndexCategoryNode node) {
        allIndexesNode = node;
    }

    public void select(Index index) {
        select(index.getName());
    }

    public void select(String indexName) {
        expandAll();
        allIndexes().find(indexName).select();
    }

    public void deselect(Index index) {
        deselect(index.getName());
    }

    public void deselect(String indexName) {
        expandAll();
        allIndexes().find(indexName).deselect();
    }

    public void expandAll() {
        allIndexes().expand();
        publicIndexes().expand();
        privateIndexes().expand();
    }

    public boolean isSelected(Index index) {
        return isSelected(index.getName());
    }

    public boolean isSelected(String indexName) {
        return allIndexes().find(indexName).isSelected();
    }

    public List<Index> getSelected() {
        List<Index> selected = new ArrayList<>();
        for (NodeElement node : this) {
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
    public Iterator<NodeElement> iterator() {
        return allIndexes().iterator();
    }

}
