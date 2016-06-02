package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

class DatabaseFilterTree {

    private final Node databaseFilterNode;

    public DatabaseFilterTree(WebElement element, WebDriver webDriver){
        databaseFilterNode = new Node(element,webDriver);
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


    private static class Node extends FilterNode{

        Node(WebElement element, WebDriver webDriver){
            super(element,webDriver);
        }

        public List<WebElement> getChildren(){
            return getContainer().findElements(By.className("database-name"));
        }

        @Override
        public List<String> getChildNames(){
            return ElementUtil.getTexts(getChildren());
        }
    }
}




