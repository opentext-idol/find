package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.indexes.tree.NodeElement;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebWindowNotFoundException;
import com.gargoylesoftware.htmlunit.javascript.host.dom.Node;
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
    public ParametricFilterTree parametricFilterTree(){return new ParametricFilterTree(leftContainer(),getParametricFilters(),getDriver());}
    public DateFilterTree dateFilterTree(){return new DateFilterTree(ElementUtil.ancestor(getDateFilter(),2),getDriver());}

    private WebElement getDateFilter(){
        return findElement(By.xpath(".//h4[contains(text(),'Dates')]"));
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
        FormInput input= new FormInput(findElement(By.xpath("//*[contains(@class,'form-control') and contains(@placeholder,'Search filters...')]")), getDriver());
        input.clear();
        input.setAndSubmit(term);
    }

    public void clearFilter(){
        FormInput input= new FormInput(findElement(By.xpath("//*[contains(@class,'form-control') and contains(@placeholder,'Search filters...')]")), getDriver());
        input.clear();
    }

    public boolean filterExists(String filter){
        return findElements(By.xpath("//tr[contains(@data-value,'"+filter+"')]")).size()>0;
    }

    private WebElement findFilter(String name){
        return leftContainer().findElement(By.xpath("//*[contains(text(),'"+name+"')]"));
    }
    public boolean filterVisible(String filter){
        return findFilter(filter).isDisplayed();
    }

    public boolean noneMatchingMessageVisible(){
        return findElement(By.xpath("//p[contains(text(),'No filters matched')]")).isDisplayed();
    }

    public List<String> getIndexNames(){
        List<String> indexNames=new ArrayList<>();
        indexNames.add("DATABASES");
        for(NodeElement el:indexesTree().allIndexes().getIndexNodes()){
            indexNames.add(el.getName());
        }
        return indexNames;
    }

    //current filters including type
    //currently only has databases type....
    public List<String> getCurrentFiltersIncType(){
        List<String> currentFilters = new ArrayList<>();
        currentFilters.addAll(parametricFilterTree().getCurrentFilters());
        currentFilters.addAll(dateFilterTree().getCurrentFilters());
        currentFilters.addAll(getIndexNames());
        return currentFilters;
    }

    public List<String> getVisibleFilterTypes(){

        List<WebElement> elements = new ArrayList<>();

        elements.addAll(dateFilterTree().getFilterTypes());
        elements.addAll(parametricFilterTree().getFilterTypes());

        if(findElement(By.xpath("//h4[text(),'Databases']")).isDisplayed()){
            elements.add(findElement(By.xpath("//h4[text(),'Databases']")));
        }

        return ElementUtil.getTexts(elements);

    }

    private FilterNode findNodeFilterTrees(String filter){
        if (parametricFilterTree().findNode(filter) != null){
            return parametricFilterTree().findNode(filter);
        }

        else{
            return dateFilterTree().findNode(filter);
        }
    }
    

    public List<String> findFilterString(String targetFilter, List<String> allFilters) {
        Set<String> matchingFilters = new HashSet<>();
        for (String filter : allFilters) {
            if (StringUtils.containsIgnoreCase(filter,targetFilter)) {
                matchingFilters.add(filter);
                if (getVisibleFilterTypes().contains(filter)) {
                    //this is all worse than before
                    if(filter.equals("Databases")){
                        for(NodeElement el:(indexesTree().allIndexes().getIndexNodes())){
                            matchingFilters.add(el.getName());
                        }
                    }
                    else {
                        matchingFilters.addAll(findNodeFilterTrees(filter).getChildNames());
                    }
                }
                //is child
                else{
                    matchingFilters.add(findNodeFilterTrees(filter).getParentName());
                }
            }
        }
        return new ArrayList<>(matchingFilters);
    }

    private List<WebElement> getCollapsibleFilters(List<WebElement> filterTypes){
        List<WebElement> collapsibleFilters=new ArrayList<>();
        for(WebElement filterTitle: filterTypes){
            collapsibleFilters.add(ElementUtil.ancestor(filterTitle,2));
        }
        return collapsibleFilters;
    }

    //toggling see more
    public void showFilters(){
        for(WebElement element:leftContainer().findElements(By.className("toggle-more-text"))){
            if (element.getText()!="See Less") {
                element.click();
            }
        }
    }

    protected void expandFiltersFully(){
        expandAll();
        showFilters();
    }

    public void expandAll(){
        parametricFilterTree().expandAll();
        dateFilterTree().expandAll();
        indexesTree().expandAll();
    }

    //shouldn't be here -> maybe wrap IndexTree in FilterTree
    private void collapseIndexes(){
        ElementUtil.ancestor(findElement(By.xpath("//h4[text(),'Databases']")),1).click();
    }

    public void collapseAll(){
        parametricFilterTree().collapseAll();
        dateFilterTree().collapseAll();
        collapseIndexes();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        public IdolFindPage create(WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}

