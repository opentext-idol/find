package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import oracle.jrockit.jfr.StringConstantPool;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class DateFilterTree extends FilterTree{

    private DateFilterNode dateFilterNode;

    DateFilterTree(WebElement element, WebDriver webDriver){
        super(element,webDriver);
        dateFilterNode=new DateFilterNode(element,webDriver);
    }

    public List<WebElement> getCurrentFiltersIncType(){
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

    //assumed should be visible
    public List<WebElement> getFilterTypes(){
        List<WebElement> filterTypes = new ArrayList<>();
        if(dateFilterNode.findFilterType().isDisplayed()) {
            filterTypes.add(dateFilterNode.findFilterType());
        }
        return filterTypes;
    }

    public List<WebElement> getChildren(){
        return dateFilterNode.getChildren();
    }

    public WebElement getParent(){
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
