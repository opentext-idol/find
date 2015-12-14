package com.autonomy.abc.selenium.indexes;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Iterator;

public class IndexesTree implements Iterable<IndexNodeElement> {
    private final IndexCategoryNode allIndexesNode;

    public IndexesTree(WebElement element, WebDriver driver) {
        this(new IndexCategoryNode(element, driver));
    }

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

    protected void expandAll() {
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

}
