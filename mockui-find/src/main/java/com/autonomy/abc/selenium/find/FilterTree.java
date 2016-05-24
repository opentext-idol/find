package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.indexes.tree.NodeElement;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.*;

public abstract class FilterTree{
    //} implements Iterable<FilterNode> {

    protected final WebElement container;
    protected final WebDriver driver;

    FilterTree(WebElement element, WebDriver webDriver) {
        container = element;
        driver = webDriver;
    }

    protected WebElement findFilter(String name){
        return container.findElement(By.xpath("//*[contains(text(),'"+name+"')]"));
    }

    public List<WebElement> getFilterTypes(){
        return container.findElements(By.tagName("h4"));
    }

    public List<String> getVisibleFilterTypes(){
        return returnsVisibleFromList(getFilterTypes());
    }

    protected List<String> returnsVisibleFromList(List<WebElement> potentialElements){
        List<WebElement> visibleElements=new ArrayList<>();
        for (WebElement el: potentialElements){
            if(el.isDisplayed()){
                visibleElements.add(el);
            }
        }
        return ElementUtil.getTexts(visibleElements);
    }



    /*public List<WebElement> getCollapsibleFilters(List<WebElement> filterTypes){
        List<WebElement> collapsibleFilters=new ArrayList<>();
        for(WebElement filterTitle: filterTypes){
            collapsibleFilters.add(ElementUtil.ancestor(filterTitle,2));
        }
        return collapsibleFilters;
    }

    protected List<NodeElement> getFilterNodes() {
        List<NodeElement> nodes = new ArrayList<>();
        nodes.addAll(getDateFilterNodes());
        nodes.addAll(getParametricFilterNodes());
        nodes.addAll(getDatabaseNodes());
        return nodes;
    }

    private List<DateFilterNode> getDateFilterNodes(){
        List<DateFilterNode> nodes = new ArrayList<>();
        for (WebElement element : container.findElements(By.cssSelector(".clickable[data-name]"))) {
            nodes.add(new DateFilterNode(element,driver));
        }
        return nodes;
    }

    private List<ParametricFilterNode> getParametricFilterNodes(){
        List<ParametricFilterNode> nodes = new ArrayList<>();
        for (WebElement element : container.findElements(By.cssSelector(".clickable[data-name]"))) {
            nodes.add(new ParametricFilterNode(element,driver));
        }
        return nodes;
    }

    private List<NodeElement> getDatabaseNodes(){
        List<NodeElement> nodes = new ArrayList<>();
        //nodes.add(new FilterNode(container.findElement(By.xpath("//h4[contains(text(),'Databases')]")),driver));
        //nodes.addAll(indexes.allIndexes().getIndexNodes());
        return nodes;
    }*/

    public ParametricFilterNode findNode(String name) {
        WebElement element = ElementUtil.getFirstChild(container.findElement
                (By.xpath("//*[contains(@data-field-display-name,'" + WordUtils.capitalize(name.toLowerCase()) + "')]")));
        return new ParametricFilterNode(element,driver);
    }



    /*@Override
    public Iterator<NodeElement> iterator() {
        return getFilterNodes().iterator();
    }*/

}
