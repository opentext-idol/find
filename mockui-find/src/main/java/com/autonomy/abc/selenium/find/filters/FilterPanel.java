package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.find.FindIndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FilterPanel {
    private final WebElement panel;
    private final WebDriver driver;
    private final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory;

    public FilterPanel(ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory, WebDriver driver) {
        this.indexesTreeFactory = indexesTreeFactory;
        this.driver = driver;
        this.panel = Container.LEFT.findUsing(driver);
    }

    public IndexesTree indexesTree() {
        return indexesTreeFactory.create(new FindIndexCategoryNode(panel.findElement(By.cssSelector(".databases-list [data-category-id='all']")), getDriver()));
    }

    public List<String> getSelectedPublicIndexes() {
        List<String> indexes = new ArrayList<>();

        for(WebElement selectedIndex : panel.findElements(By.cssSelector("[data-category-id='public'] .icon-ok.database-icon"))){
            indexes.add(ElementUtil.ancestor(selectedIndex, 2).findElement(By.xpath("./span[@class='database-name' or @class='category-name']")).getText());
        }

        return indexes;
    }

    public boolean parametricEmptyExists() {
        return !panel.findElements(By.className("parametric-empty")).isEmpty();
    }

    //should check not already selected
    public void clickFirstIndex(){
        panel.findElement(By.cssSelector(".child-categories li:first-child")).click();
    }

    public void seeMoreOfCategory(WebElement element){element.findElement(By.className("toggle-more")).click();}

    private WebDriver getDriver() {
        return driver;
    }
}
