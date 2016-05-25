package com.autonomy.abc.selenium.find;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFilterTree extends FilterTree{

    private DatabaseFilterNode databaseFilterNode;

    DatabaseFilterTree(WebElement element, WebDriver webDriver){
        super(element,webDriver);
        databaseFilterNode = new DatabaseFilterNode(element,webDriver);
    }

    public List<WebElement> getCurrentFiltersIncType(){
        List<WebElement> filters=new ArrayList<>();
        filters.addAll(getChildren());
        if(getParent().isDisplayed()){
            filters.add(getParent());
        }
        return filters;
    }

    public List<WebElement> getFilterTypes(){
        List<WebElement> filterTypes = new ArrayList<>();
        if(databaseFilterNode.findFilterType().isDisplayed()) {
            filterTypes.add(databaseFilterNode.findFilterType());
        }
        return filterTypes;
    }

    public List<WebElement> getChildren(){
        return databaseFilterNode.getChildren();
    }

    public WebElement getParent(){
        return databaseFilterNode.getParent();
    }

    public void expandAll(){
        databaseFilterNode.expand();
    }

    public void collapseAll(){
        databaseFilterNode.collapse();
    }


}




