package com.autonomy.abc.selenium.indexes.tree;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FindIndexCategoryNode extends IndexCategoryNode {
    private WebElement container;
    private WebDriver driver;

    public FindIndexCategoryNode(WebElement clickable, WebDriver webDriver) {
        super(new FindIndexLeafNode(clickable), clickable, webDriver);
        container = clickable;
        driver = webDriver;
    }

    @Override
    List<IndexNodeElement> getIndexNodes() {
        List<IndexNodeElement> nodes = new ArrayList<>();
        for (WebElement element : container.findElements(By.cssSelector(".clickable[data-name]"))) {
            nodes.add(new FindIndexLeafNode(element));
        }
        return nodes;
    }

    @Override
    IndexNodeElement find(String name) {
        WebElement childElement = container.findElement(By.cssSelector(".clickable[data-name='" + name.toLowerCase() + "']"));
        return new FindIndexLeafNode(childElement);
    }

    @Override
    IndexCategoryNode findCategory(String name) {
        WebElement childElement = container.findElement(By.cssSelector(".clickable[data-category-id='" + name.toLowerCase() + "']"));
        return new FindIndexCategoryNode(childElement, driver);
    }

    @Override
    public String getName() {
        return container.findElement(By.className("category-name")).getText();
    }
}
