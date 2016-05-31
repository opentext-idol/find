package com.autonomy.abc.selenium.find.filters;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class DateFilterTree extends FilterTree{

    private final DateFilterNode dateFilterNode;

    public DateFilterTree(WebElement element, WebDriver webDriver){
        super(element,webDriver);
        dateFilterNode=new DateFilterNode(element,webDriver);
    }

    public List<WebElement> getAllFiltersInTree(){
        List<WebElement> filters = new ArrayList<>();
        for(WebElement potentialElement:getChildren()){
            if(potentialElement.isDisplayed()){
                filters.add(potentialElement);
            }
        }
        if(getParent().isDisplayed()) {
            filters.add(getParent());
        }
        return filters;
    }

    public List<WebElement> getFilterTypes(){
        List<WebElement> filterTypes = new ArrayList<>();
        if(getParent().isDisplayed()) {
            filterTypes.add(getParent());
        }
        return filterTypes;
    }

    private List<WebElement> getChildren(){
        return dateFilterNode.getChildren();
    }

    private WebElement getParent(){
        return dateFilterNode.getParent();
    }

    public List<String> getChildNames(){
        return dateFilterNode.getChildNames();
    }

    public String getParentName(){
        return dateFilterNode.getParentName();
    }

    public void expandAll(){
        dateFilterNode.expand();
    }

    public void collapseAll(){
        dateFilterNode.collapse();
    }

}
