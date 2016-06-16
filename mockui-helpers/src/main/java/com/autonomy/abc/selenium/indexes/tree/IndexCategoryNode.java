package com.autonomy.abc.selenium.indexes.tree;

import com.autonomy.abc.selenium.indexes.Index;
import com.hp.autonomy.frontend.selenium.element.ChevronContainer;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class IndexCategoryNode implements IndexNodeElement, Collapsible, Iterable<IndexNodeElement> {
    private final WebElement container;
    private final WebDriver driver;
    private final Collapsible collapsible;
    private final IndexNodeElement delegate;

    protected IndexCategoryNode(final IndexNodeElement inside, final WebElement element, final WebDriver webDriver) {
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

    public List<String> getSelectedNames() {
        final List<String> selected = new ArrayList<>();
        for (final IndexNodeElement child : this) {
            if (child.isSelected()) {
                selected.add(child.getName());
            }
        }
        return selected;
    }

    public Index getIndex(final int i) {
        return new Index(getIndexNodes().get(i).getName());
    }

    /**
     * different from expand(): some applications only show a fixed
     * number of indexes after expanding by default, and allow
     * displaying the rest by clicking an additional "see more" option
     */
    protected abstract void seeMore();

    protected abstract List<IndexNodeElement> getIndexNodes();

    protected abstract IndexNodeElement find(String name);

    protected abstract IndexCategoryNode findCategory(String name);

    @Override
    public Iterator<IndexNodeElement> iterator() {
        return getIndexNodes().iterator();
    }

    public WebElement getContainer() {
        return container;
    }

    protected WebDriver getDriver() {
        return driver;
    }
}
