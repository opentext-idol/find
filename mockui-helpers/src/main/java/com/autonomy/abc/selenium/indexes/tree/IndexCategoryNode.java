package com.autonomy.abc.selenium.indexes.tree;

import com.hp.autonomy.frontend.selenium.element.ChevronContainer;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.List;

public abstract class IndexCategoryNode implements IndexNodeElement, Collapsible, Iterable<IndexNodeElement> {
    private final WebElement container;
    private final WebDriver driver;
    private final Collapsible collapsible;
    private final IndexNodeElement delegate;

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

    protected abstract List<IndexNodeElement> getIndexNodes();

    protected abstract IndexNodeElement find(String name);

    protected abstract IndexCategoryNode findCategory(String name);

    @Override
    public Iterator<IndexNodeElement> iterator() {
        return getIndexNodes().iterator();
    }

    protected WebElement getContainer() {
        return container;
    }

    protected WebDriver getDriver() {
        return driver;
    }
}
