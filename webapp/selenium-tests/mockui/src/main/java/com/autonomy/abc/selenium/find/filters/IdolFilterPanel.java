package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IdolFilterPanel extends FilterPanel {
    private final WebDriver driver;

    public IdolFilterPanel(final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory, final WebDriver driver) {
        super(indexesTreeFactory, driver);
        this.driver = driver;
    }

    public GraphFilterContainer getNthGraph(final int n) {
        if (n == 0) {
            final WebElement graphItself = getPanel().findElement(By.cssSelector("div.collapse .clickable-widget"));
            return new GraphFilterContainer(ElementUtil.ancestor(graphItself, 5), driver);
        } else {
            return graphContainers().get(n);
        }
    }

    public GraphFilterContainer getFirstNumericGraph() {
        return numericGraphContainers().get(0);
    }

    public GraphFilterContainer getFirstDateGraph() {
        return dateGraphContainers().get(0);
    }

    public GraphFilterContainer getNamedGraph(final String name) {
        for (final GraphFilterContainer container : graphContainers()) {
            if (container.filterCategoryName().toLowerCase().equals(name.toLowerCase())) {
                return container;
            }
        }

        throw new IllegalStateException("There are no graphs with name " + name);
    }

    public List<GraphFilterContainer> numericGraphContainers() {
        return graphContainers().stream().filter(container -> !container.filterCategoryName().toLowerCase().contains("date")).collect(Collectors.toList());
    }

    public List<GraphFilterContainer> dateGraphContainers() {
        return graphContainers().stream().filter(container -> container.filterCategoryName().toLowerCase().contains("date")).collect(Collectors.toList());
    }

    public List<GraphFilterContainer> graphContainers() {
        final List<GraphFilterContainer> containers = new ArrayList<>();
        for (final WebElement container : getGraphContainers()) {
            containers.add(new GraphFilterContainer(container, driver));
        }
        return containers;
    }

    private List<WebElement> getGraphContainers() {
        final List<WebElement> ancestors = new ArrayList<>();
        for (final WebElement element : getPanel().findElements(By.cssSelector("div.collapse .clickable-widget"))) {
            ancestors.add(ElementUtil.ancestor(element, 5));
        }
        return ancestors;
    }

    @Override
    public List<FilterContainer> allFilterContainers() {
        final List<FilterContainer> nodes = new ArrayList<>();
        nodes.add(indexesTreeContainer());
        nodes.add(dateFilterContainer());
        nodes.addAll(parametricFieldContainers());
        nodes.addAll(graphContainers());
        return nodes;
    }

    //METAFILTERING
    public void searchFilters(final String term) {
        final FormInput input = new FormInput(getPanel().findElement(By.cssSelector("[placeholder='Filter\u2026']")), driver);
        for(int i = 0; i < term.length(); i++) {
            input.getElement().sendKeys(Keys.BACK_SPACE);
        }
        input.getElement().sendKeys(term);
        input.submit();
    }

    public void clearMetaFilter() {
        final FormInput input = new FormInput(getPanel().findElement(By.cssSelector("[placeholder='Filter\u2026']")), driver);
        input.clear();
        waitForIndexes();
    }
}
