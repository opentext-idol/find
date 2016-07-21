package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class IdolFilterPanel extends FilterPanel{
    private final WebDriver driver;

    public IdolFilterPanel(final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory, final WebDriver driver){
        super(indexesTreeFactory,driver);
        this.driver = driver;
    }

    public List<GraphFilterContainer> graphContainers() {
        final List<GraphFilterContainer> containers = new ArrayList<>();
        for(final WebElement container : getGraphContainers()) {
            containers.add(new GraphFilterContainer(container,driver));
        }
        return containers;
    }

    private List<WebElement> getGraphContainers() {
        final List<WebElement> ancestors = new ArrayList<>();
        for (WebElement element : getPanel().findElements(By.cssSelector("div.collapse .clickable-widget"))){
            ancestors.add(ElementUtil.ancestor(element,5));
        }
        return ancestors;
    }

}
