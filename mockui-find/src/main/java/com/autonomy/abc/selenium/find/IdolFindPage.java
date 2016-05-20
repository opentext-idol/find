package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.gargoylesoftware.htmlunit.WebWindowNotFoundException;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.apache.xalan.xsltc.dom.Filter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

public class IdolFindPage extends FindPage {

    public IdolFindPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public IndexesTree indexesTree() {
        return new IdolDatabaseTree(super.indexesTree());
    }

    public FilterTree filterTree(){return new FilterTree(indexesTree(), leftContainer(), getDriver()); }

    //THIS IS ALL TERRIBLE -> REDUCE
    public void filterResults(String term){
        filterSearch(term);
    }

    public void filterResults(Filter filter){
        filterSearch(filter.toString());
    }

    private void filterSearch(String term) {
        FormInput input= new FormInput(findElement(By.xpath("//*[contains(@class,'form-control') and contains(@placeholder,'Search filters...')]")), getDriver());
        input.clear();
        input.setAndSubmit(term);
    }

    public void clearFilter(){
        FormInput input= new FormInput(findElement(By.xpath("//*[contains(@class,'form-control') and contains(@placeholder,'Search filters...')]")), getDriver());
        input.clear();
    }

    public List<WebElement> getFilterTypes(){
        return leftContainer().findElements(By.tagName("h4"));
    }

    private List<WebElement> getCollapsibleFilters(List<WebElement> filterTypes){
        List<WebElement> collapsibleFilters=new ArrayList<>();
        for(WebElement filterTitle: filterTypes){
            collapsibleFilters.add(ElementUtil.ancestor(filterTitle,2));
        }
        return collapsibleFilters;
    }

    private List<String> getChildNames(String filter){
        WebElement filterElement = filterTree().getFilterTypeNode(filter);
        List<String> childNames = ElementUtil.getTexts(filterElement.findElements(By.xpath(".//*[contains(@class,'parametric-value-name') or contains(@class,'database-name')]")));
        if (childNames.size()<=0){
            childNames.addAll(ElementUtil.getTexts(filterElement.findElements(By.xpath((".//tr[@data-filter-id]/td[2]")))));
        }
        return childNames;
    }

    private String getParentName(String filter){
        WebElement filterElement  = ElementUtil.ancestor(filterTree().getFilterNode(filter),5);
        LOGGER.info(filterElement.getAttribute("class"));

        if (filterElement.getAttribute("class").equals("category-input")){
            filterElement.getAttribute("class");
            LOGGER.info("Class is equal to category-input");
            filterElement  = ElementUtil.ancestor(filterTree().getFilterNode(filter),9);
        }
        //ancestor 7 for
        return ElementUtil.getFirstChild(filterElement.findElement(By.xpath(".//preceding-sibling::div"))).getText();
    }

    public List<String> getVisibleFilterTypes(){
        return returnsVisibleFromList(getFilterTypes());
    }

    public List<String> getCurrentFilters(){
        waitForIndexes();
        List<String> baseFilters = ElementUtil.getTexts(findElements(By.xpath("//*[contains(@class,'parametric-value-name') or contains(@class,'database-name')]")));
        baseFilters.addAll(getVisibleDateFilters());
        baseFilters.addAll(getVisibleFilterTypes());
        return baseFilters;
    }

    private List<String> getVisibleDateFilters(){
        Waits.loadOrFadeWait();
        List<WebElement> potentialElements = leftContainer().findElements(By.xpath(".//tr[@data-filter-id]/td[2]"));
        return returnsVisibleFromList(potentialElements);
    }

    //this is unbelievably terrible and slow -> fix this ! BUT other way (with xpath hide) wasn't working
    private List<String> returnsVisibleFromList(List<WebElement> potentialElements){
        List<WebElement> visibleElements=new ArrayList<>();
        for (WebElement el: potentialElements){
            if(el.isDisplayed()){
                visibleElements.add(el);
            }
        }
        return ElementUtil.getTexts(visibleElements);
    }

    public boolean noneMatchingMessageVisible(){
        return findElement(By.xpath("//p[contains(text(),'No filters matched')]")).isDisplayed();
    }

    public void expandFiltersFully(){
        filterTree().seeAllFilters(getCollapsibleFilters(getFilterTypes()));
    }

    public void collapseFiltersFully(){
        filterTree().collapseAll(getCollapsibleFilters(getFilterTypes()));
    }

    public void expandFilters(){
        filterTree().showFilters();
    }

    public List<String> findFilterString(String targetFilter, List<String> allFilters) {
        Set<String> matchingFilters = new HashSet<>();
        for (String filter : allFilters) {
            if (StringUtils.containsIgnoreCase(filter,targetFilter)) {
                matchingFilters.add(filter);
                if (getVisibleFilterTypes().contains(filter)) {
                    matchingFilters.addAll(getChildNames(filter));
                }
                //is child
                else{
                    matchingFilters.add(getParentName(filter));
                }
            }
        }
        return new ArrayList<>(matchingFilters);
    }


    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        public IdolFindPage create(WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}

