package com.autonomy.abc.selenium.find.filters;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ParametricFilterTree extends FilterContainer {

    private final List<ParametricFieldContainer> containers;

    public ParametricFilterTree(WebElement container, List<WebElement> nodes,WebDriver webDriver){
        super(container,webDriver);
        containers = new ArrayList<>();
        for(WebElement element:nodes){
            containers.add(new ParametricFieldContainer(element,webDriver));
        }
    }

    public List<WebElement> getFilterTypes(){
        List<WebElement> filterTypes = new ArrayList<>();
        for(ParametricFieldContainer node:containers){
            if(node.findFilterType().isDisplayed())
                filterTypes.add(node.findFilterType());
        }
        return filterTypes;
    }

    public WebElement getIthFilterType(int i){
        return getFilterTypes().get(i);
    }

    public ParametricFieldContainer findParametricFilterNode(String name){
        for(ParametricFieldContainer node:containers){
            if(node.getParentName().equals(name)){
                return node;
            }
        }
        return null;
    }

    public List<WebElement> getAllFiltersInTree(){
        List<WebElement> filters = new ArrayList<>();

        for(ParametricFieldContainer node:containers) {
            filters.addAll(node.getChildren());

            if (node.getParent().isDisplayed()) {
                filters.add(node.getParent());
            }
        }
        return filters;
    }

    public void expandAll(){
        for(ParametricFieldContainer node:containers){
            node.expand();
        }
    }

    public void collapseAll(){
        for(ParametricFieldContainer node:containers){
            node.collapse();
        }
    }
}
