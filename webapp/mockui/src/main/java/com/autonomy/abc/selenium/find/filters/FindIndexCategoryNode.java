package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

class FindIndexCategoryNode extends IndexCategoryNode {
    private final WebElement container;
    private final WebDriver driver;

    FindIndexCategoryNode(final WebElement clickable, final WebDriver webDriver) {
        super(new FindIndexLeafNode(clickable, webDriver), clickable, webDriver);
        container = clickable;
        driver = webDriver;
    }

    @Override
    protected void seeMore() {
        By toggleMore = By.className("toggle-more");

        if(container.findElements(toggleMore).size() > 0) {
            container.findElement(toggleMore).click();
        }
    }

    @Override
    protected List<IndexNodeElement> getIndexNodes() {
        final List<IndexNodeElement> nodes = new ArrayList<>();
        for (final WebElement element : container.findElements(By.cssSelector(".clickable[data-name]"))) {
            nodes.add(new FindIndexLeafNode(element, driver));
        }
        return nodes;
    }

    @Override
    //TODO update this to use index name rather than data-name
    protected IndexNodeElement find(final String name) {
        final WebElement childElement = container.findElement(By.xpath(".//*[normalize-space(.)='" + name + "' and contains(@class,'clickable')]"));
        return new FindIndexLeafNode(childElement, driver);
    }

    @Override
    protected IndexCategoryNode findCategory(final String name) {
        final WebElement childElement = container.findElement(By.cssSelector(".clickable[data-category-id='" + name.toLowerCase() + "']"));
        return new FindIndexCategoryNode(childElement, driver);
    }

    @Override
    public String getName() {
        return container.findElement(By.className("category-name")).getText();
    }
}
