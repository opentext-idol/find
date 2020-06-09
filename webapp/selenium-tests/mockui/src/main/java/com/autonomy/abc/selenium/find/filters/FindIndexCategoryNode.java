/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

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
        super(new FindIndexLeafNode(clickable), clickable, webDriver);
        container = clickable;
        driver = webDriver;
    }

    @Override
    protected void seeMore() {
        final By toggleMore = By.className("toggle-more");

        if(container.findElements(toggleMore).size() > 0) {
            container.findElement(toggleMore).click();
        }
    }

    @Override
    public List<IndexNodeElement> getIndexNodes() {
        final List<IndexNodeElement> nodes = new ArrayList<>();
        for(final WebElement element : container.findElements(By.cssSelector(".clickable[data-name]"))) {
            nodes.add(new FindIndexLeafNode(element));
        }
        return nodes;
    }

    @Override
    protected IndexNodeElement find(final String name) {
        final WebElement childElement = container.findElement(By.xpath(".//*[normalize-space(.)='" + name + "' and contains(@class,'clickable')]"));
        return new FindIndexLeafNode(childElement);
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
