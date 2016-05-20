package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.jetty.util.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

public class FilterTree {
    // implements Iterable<FilterCategoryNode>{
    private final WebElement container;
    private final WebDriver driver;
    public IndexesTree indexes;

    public FilterTree(IndexesTree tree, WebElement element, WebDriver webDriver) {
        //indexes = new IndexesTree(allNode);
        indexes = tree;
        container = element;
        driver = webDriver;
    }

    //THIS IS ALL TERRIBLE AND IS GOING TO BE FIXED
    protected FilterNode getFilterType(String name) {
        WebElement childElement = container.findElement(By.xpath("//h4[contains(text(),'" + name + "')]"));
        return new FilterNode(childElement, driver);
    }

    public WebElement getFilterNode(String name){
        LOGGER.info(container.findElement(By.xpath("//*[contains(text(),'"+name+"')]")).getText());
        LOGGER.info(container.findElement(By.xpath("//*[contains(text(),'"+name+"')]")).toString());
        LOGGER.info(container.findElement(By.xpath("//*[contains(text(),'"+name+"')]")).getTagName());

        return container.findElement(By.xpath("//*[contains(text(),'"+name+"')]"));
    }

    public WebElement getFilterTypeNode(String name){
        if(name.equals("DATES")||name.equals("DATABASES")) {
            return ElementUtil.ancestor(container.findElement
                (By.xpath("//h4[contains(text(),'" + WordUtils.capitalize(name.toLowerCase()) + "')]")),2);}
        else{
            return ElementUtil.getFirstChild(container.findElement
                    (By.xpath("//*[contains(@data-field-display-name,'" +  WordUtils.capitalize(name.toLowerCase())+ "')]")));
        }
    }


    protected void seeAllFilters(List<WebElement> filterTypes){
        expandAll(filterTypes);
        showFilters();
    }

    public void showFilters(){
        for(WebElement element:container.findElements(By.className("toggle-more-text"))){
            if (element.getText()!="See Less") {
                element.click();
            }
        }
    }
    public void expandAll(List<WebElement> filterTypes){
        for(WebElement filter:filterTypes){
            new FilterNode(filter,driver).expand();
        }
    }
    public void collapseAll(List<WebElement> filterTypes){
        for(WebElement filter:filterTypes){
            new FilterNode(filter,driver).collapse();
        }
    }

    //needs to iterate over both the index tree and the rest of the filter tree
    //@Override
   /* public Iterator<> iterator() {
        return indexes.allIndexes().iterator();
    }*/

}
