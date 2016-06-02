package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

class DateFilterTree {

    private final DateFilterNode dateFilterNode;

    public DateFilterTree(WebElement element, WebDriver webDriver){
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

    private static class DateFilterNode extends FilterNode {

        DateFilterNode(WebElement element, WebDriver webDriver){
            super(element,webDriver);
        }

        public List<WebElement> getChildren(){
            return getContainer().findElements(By.cssSelector("[data-filter-id] > td:nth-child(2)"));
        }

        @Override
        public List<String> getChildNames(){
            return ElementUtil.getTexts(getChildren());
        }

    }
}
