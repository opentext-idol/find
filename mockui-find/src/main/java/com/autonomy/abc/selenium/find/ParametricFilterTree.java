package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ParametricFilterTree extends FilterTree {

    private List<ParametricFilterNode> containers;

    ParametricFilterTree(WebElement container, List<WebElement> nodes,WebDriver webDriver){
        //left side container
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

    //replacing expandAll
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

    public List<String> getCurrentFilters() {
        List<String> currentFilters = new ArrayList<>();
        for(ParametricFilterNode node:containers){
            //this needs to be node
           currentFilters.addAll(ElementUtil.getTexts(container.findElements(By.xpath("//*[contains(@class,'parametric-value-name')]"))));
        }
        return currentFilters;
    }
}
