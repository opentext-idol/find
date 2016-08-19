package com.autonomy.abc.selenium.indexes.tree;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class IsoIndexCategoryNode extends IndexCategoryNode {
    public IsoIndexCategoryNode(final WebElement element, final WebDriver webDriver) {
        super(new IndexLeafNode(element, webDriver), element, webDriver);
    }

    @Override
    protected void seeMore() {}

    @Override
    protected List<IndexNodeElement> getIndexNodes() {
        final List<IndexNodeElement> nodes = new ArrayList<>();
        for (final WebElement element : getContainer().findElements(By.cssSelector(".checkbox[data-name]"))) {
            nodes.add(new IndexLeafNode(element, getDriver()));
        }
        return nodes;
    }

    @Override
    protected IndexNodeElement find(final String name) {
        final WebElement childElement = ElementUtil.ancestor(getContainer().findElement(By.xpath(".//span[normalize-space(text()) = '" + name + "']")), 2);
        return new IndexLeafNode(childElement, getDriver());
    }

    @Override
    protected IndexCategoryNode findCategory(final String name) {
        final WebElement childElement = ElementUtil.ancestor(getContainer().findElement(By.cssSelector(".checkbox[data-category-id='" + name.toLowerCase() + "']")), 1);
        return new IsoIndexCategoryNode(childElement, getDriver());
    }
}
