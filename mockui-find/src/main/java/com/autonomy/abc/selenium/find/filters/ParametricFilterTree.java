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

    private List<WebElement> getFilterTypes(){
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

}
