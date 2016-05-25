package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

public class ParametricFilterTree extends FilterTree {

    private List<ParametricFilterNode> containers;

    ParametricFilterTree(WebElement container, List<WebElement> nodes,WebDriver webDriver){
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

    public List<WebElement> getCurrentFiltersIncType(){
        List<WebElement> filters = new ArrayList<>();

        for(ParametricFilterNode node:containers) {
            for (WebElement potentialElement : node.getChildren()) {
                if (potentialElement.isDisplayed()) {
                    filters.add(potentialElement);
                }
            }
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
