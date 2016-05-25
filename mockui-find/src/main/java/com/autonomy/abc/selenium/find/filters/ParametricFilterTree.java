package com.autonomy.abc.selenium.find.filters;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ParametricFilterTree extends FilterTree {

    private List<ParametricFilterNode> containers;

    public ParametricFilterTree(WebElement container, List<WebElement> nodes,WebDriver webDriver){
        super(container,webDriver);
        containers = new ArrayList<>();
        for(WebElement element:nodes){
            containers.add(new ParametricFilterNode(element,webDriver));
        }
    }

    public WebElement findFilter(String name){
        return container.findElement(By.xpath("//*[contains(text(),'"+name+"')]"));
    }

    public List<WebElement> getFilterTypes(){
        List<WebElement> filterTypes = new ArrayList<>();
        for(ParametricFilterNode node:containers){
            if(node.findFilterType().isDisplayed())
                filterTypes.add(node.findFilterType());
        }
        return filterTypes;
    }

    public List<WebElement> getAllFiltersInTree(){
        List<WebElement> filters = new ArrayList<>();

        for(ParametricFilterNode node:containers) {
            filters.addAll(node.getChildren());
            if (node.getParent().isDisplayed()) {
                filters.add(node.getParent());
            }
        }
        return filters;
    }

    public void expandAll(){
        for(ParametricFilterNode node:containers){
            node.expand();
        }
    }

    public void collapseAll(){
        for(ParametricFilterNode node:containers){
            node.collapse();
        }
    }
}
