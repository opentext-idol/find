package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

class ParametricFilterTree {

    private final List<Node> containers;

    public ParametricFilterTree(WebElement container, List<WebElement> nodes,WebDriver webDriver){
        containers = new ArrayList<>();
        for(WebElement element:nodes){
            containers.add(new Node(element,webDriver));
        }
    }

    public List<WebElement> getFilterTypes(){
        List<WebElement> filterTypes = new ArrayList<>();
        for(Node node:containers){
            if(node.findFilterType().isDisplayed())
                filterTypes.add(node.findFilterType());
        }
        return filterTypes;
    }

    public List<WebElement> getAllFiltersInTree(){
        List<WebElement> filters = new ArrayList<>();

        for(Node node:containers) {
            filters.addAll(node.getChildren());

            if (node.getParent().isDisplayed()) {
                filters.add(node.getParent());
            }
        }
        return filters;
    }

    public void expandAll(){
        for(Node node:containers){
            node.expand();
        }
    }

    public void collapseAll(){
        for(Node node:containers){
            node.collapse();
        }
    }

    public FilterNode getField(int i) {
        return containers.get(i);
    }

    private static class Node extends FilterNode {

        Node(WebElement element, WebDriver webDriver) {
            super(element, webDriver);
        }

        public List<WebElement> getChildren(){
            return getContainer().findElements(By.className("parametric-value-name"));
        }

        @Override
        public List<String> getChildNames() {
            return ElementUtil.getTexts(getChildren());
        }

    }
}
