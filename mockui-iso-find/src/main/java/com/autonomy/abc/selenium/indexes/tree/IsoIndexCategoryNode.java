package com.autonomy.abc.selenium.indexes.tree;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IsoIndexCategoryNode extends IndexCategoryNode {
    public IsoIndexCategoryNode(WebElement element, WebDriver webDriver) {
        super(new IndexLeafNode(element, webDriver), element, webDriver);
    }
}
