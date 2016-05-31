package com.autonomy.abc.selenium.find.filters;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFilterTree extends FilterTree{

    private final DatabaseFilterNode databaseFilterNode;

    public DatabaseFilterTree(WebElement element, WebDriver webDriver){
        super(element,webDriver);
        databaseFilterNode = new DatabaseFilterNode(element,webDriver);
    }

    public List<WebElement> getAllFiltersInTree(){
        List<WebElement> filters=new ArrayList<>();
        filters.addAll(getChildren());
        if(getParent().isDisplayed()){
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
        return databaseFilterNode.getChildren();
    }

    private WebElement getParent(){
        return databaseFilterNode.getParent();
    }

    public void expandAll(){
        databaseFilterNode.expand();
    }

    public void collapseAll(){
        databaseFilterNode.collapse();
    }


}




