package com.autonomy.abc.selenium.indexes;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Iterator;

public class IndexesTree implements Iterable<IndexNodeElement> {
    private final IndexCategoryNode allIndexesNode;

    public IndexesTree(WebElement element, WebDriver driver) {
        allIndexesNode = new IndexCategoryNode(element, driver);
    }

    public void select(Index index) {
        select(index.getName());
    }

    public void select(String indexName) {
        expandAll();
        allIndexesNode.find(indexName).select();
    }

    public void deselect(Index index) {
        deselect(index.getName());
    }

    public void deselect(String indexName) {
        expandAll();
        allIndexesNode.find(indexName).deselect();
    }

    private void expandAll() {
        allIndexesNode.expand();
        publicIndexes().expand();
        privateIndexes().expand();
    }

    public boolean isSelected(Index index) {
        return isSelected(index.getName());
    }

    public boolean isSelected(String indexName) {
        return allIndexesNode.find(indexName).isSelected();
    }

    public IndexCategoryNode allIndexes() {
        return allIndexesNode;
    }

    public IndexCategoryNode publicIndexes() {
        return allIndexesNode.findCategory("public");
    }

    public IndexCategoryNode privateIndexes() {
        return allIndexesNode.findCategory("private");
    }

    @Override
    public Iterator<IndexNodeElement> iterator() {
        return allIndexesNode.getIndexNodes().iterator();
    }

}
