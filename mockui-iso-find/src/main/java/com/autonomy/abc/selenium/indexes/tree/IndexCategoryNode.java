package com.autonomy.abc.selenium.indexes.tree;

import com.hp.autonomy.frontend.selenium.element.ChevronContainer;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IndexCategoryNode implements IndexNodeElement, Collapsible, Iterable<IndexNodeElement> {
    private final WebElement container;
    private final WebDriver driver;
    private final Collapsible collapsible;
    private final IndexNodeElement delegate;

    private IndexCategoryNode(WebElement element, WebDriver webDriver) {
        this(new IndexLeafNode(element, webDriver), element, webDriver);
    }

    protected IndexCategoryNode(IndexNodeElement inside, WebElement element, WebDriver webDriver) {
        delegate = inside;
        container = element;
        driver = webDriver;
        collapsible = new ChevronContainer(element, driver);
    }

    @Override
    public void select() {
        delegate.select();
    }

    @Override
    public void deselect() {
        delegate.deselect();
    }

    @Override
    public boolean isSelected() {
        return delegate.isSelected();
    }

    @Override
    public void expand() {
        collapsible.expand();
    }

    @Override
    public void collapse() {
        collapsible.collapse();
    }

    @Override
    public boolean isCollapsed() {
        return collapsible.isCollapsed();
    }

    @Override
    public String getName() {
        return container.getText();
    }

    protected List<IndexNodeElement> getIndexNodes() {
        List<IndexNodeElement> nodes = new ArrayList<>();
        for (WebElement element : container.findElements(By.cssSelector(".checkbox[data-name]"))) {
            nodes.add(new IndexLeafNode(element, driver));
        }
        return nodes;
    }

    protected IndexNodeElement find(String name) {
        WebElement childElement = container.findElement(By.cssSelector(".checkbox[data-name='" + name.toLowerCase() + "']"));
        return new IndexLeafNode(childElement, driver);
    }

    protected IndexCategoryNode findCategory(String name) {
        WebElement childElement = ElementUtil.ancestor(container.findElement(By.cssSelector(".checkbox[data-category-id='" + name.toLowerCase() + "']")), 1);
        return new IndexCategoryNode(childElement, driver);
    }

    @Override
    public Iterator<IndexNodeElement> iterator() {
        return getIndexNodes().iterator();
    }
}
