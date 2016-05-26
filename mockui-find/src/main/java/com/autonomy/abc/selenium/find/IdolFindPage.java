package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.DatabaseFilterTree;
import com.autonomy.abc.selenium.find.filters.DateFilterTree;
import com.autonomy.abc.selenium.find.filters.FilterNode;
import com.autonomy.abc.selenium.find.filters.ParametricFilterTree;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.xalan.xsltc.dom.Filter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdolFindPage extends FindPage {

    public IdolFindPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public IndexesTree indexesTree() {
        return new IdolDatabaseTree(super.indexesTree());
    }
    public ParametricFilterTree parametricFilterTree(){return new ParametricFilterTree(leftContainer(),getParametricFilters(),getDriver());}
    public DateFilterTree dateFilterTree(){return new DateFilterTree(ElementUtil.ancestor(getDateFilter(),2),getDriver());}
    public DatabaseFilterTree databaseFilterTree(){return new DatabaseFilterTree(ElementUtil.ancestor(getDatabaseFilter(),2),getDriver());}

    private WebElement getDatabaseFilter(){
        return leftContainer().findElement(By.xpath(".//h4[contains(text(),'Databases')]"));
    }
    private WebElement getDateFilter(){
        return leftContainer().findElement(By.xpath(".//h4[contains(text(),'Dates')]"));
    }

    private List<WebElement> getParametricFilters() {
        List<WebElement> ancestors = new ArrayList<>();
        for (WebElement element : findElements(By.className("parametric-fields-table"))) {
            ancestors.add(ElementUtil.ancestor(element, 3));
        }
        return ancestors;
    }

    public void filterResults(String term){
        filterSearch(term);
    }

    public void filterResults(Filter filter){
        filterSearch(filter.toString());
    }

    private void filterSearch(String term) {
        FormInput input= new FormInput(findElement(By.cssSelector("[placeholder='Search filters...']")),getDriver());
        input.clear();
        input.setAndSubmit(term);
    }

    public void clearFilter(){
        FormInput input= new FormInput(findElement(By.cssSelector("[placeholder='Search filters...']")),getDriver());
        input.clear();
        waitForIndexes();
    }

    public boolean parametricFilterExists(String filter){
        return findElements(By.cssSelector(".parametric-value-element[data-value='"+filter+"']")).size()>0;
    }

    //TODO: make this use the filter trees
    private WebElement findFilter(String name){
        return leftContainer().findElement(By.xpath(".//*[contains(text(),'"+name+"')]"));
    }
    public boolean filterVisible(String filter){
        return findFilter(filter).isDisplayed();
    }

    public boolean noneMatchingMessageVisible(){
        return leftContainer().findElement(By.xpath(".//p[contains(text(),'No filters matched')]")).isDisplayed();
    }

    public List<WebElement> getCurrentFilters(){
        List<WebElement> currentFilters = new ArrayList<>();
        currentFilters.addAll(databaseFilterTree().getAllFiltersInTree());
        currentFilters.addAll(dateFilterTree().getAllFiltersInTree());
        currentFilters.addAll(parametricFilterTree().getAllFiltersInTree());
        return currentFilters;
    }

    public List<String> getVisibleFilterTypes(){
        List<WebElement> elements = new ArrayList<>();

        elements.addAll(databaseFilterTree().getFilterTypes());
        elements.addAll(dateFilterTree().getFilterTypes());
        elements.addAll(parametricFilterTree().getFilterTypes());

        return ElementUtil.getTexts(elements);
    }

    public List<String> findFilterString(String targetFilter, List<WebElement> allFilters) {
        waitForIndexes();
        Set<String> matchingFilters = new HashSet<>();

        for (WebElement filter : allFilters) {
            if (StringUtils.containsIgnoreCase(filter.getText(),targetFilter)) {
                matchingFilters.add(filter.getText());

                if (getVisibleFilterTypes().contains(filter.getText())) {
                    matchingFilters.addAll(new FilterNode(ElementUtil.ancestor(filter,2), getDriver()).getChildNames());
                }
                //is child
                else{
                    matchingFilters.add(new FilterNode(filter,getDriver()).getParentName());
                }
            }
        }
        return new ArrayList<>(matchingFilters);
    }

    //toggling see more
    public void showFilters(){
        for(WebElement element:leftContainer().findElements(By.className("toggle-more-text"))){
            if (element.getText()!="See Less") {
                element.click();
            }
        }
    }

    public void expandFiltersFully(){
        waitForIndexes();
        expandAll();
        showFilters();
    }

    public void expandAll(){
        databaseFilterTree().expandAll();
        dateFilterTree().expandAll();
        parametricFilterTree().expandAll();
    }

    public void collapseAll(){
        databaseFilterTree().collapseAll();
        dateFilterTree().collapseAll();
        parametricFilterTree().collapseAll();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        public IdolFindPage create(WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}

