package com.autonomy.abc.selenium.find;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.*;

public abstract class FilterTree{

    protected final WebElement container;
    protected final WebDriver driver;

    FilterTree(WebElement element, WebDriver webDriver) {
        container = element;
        driver = webDriver;
    }

    public abstract List<WebElement> getFilterTypes();
    public abstract List<WebElement> getCurrentFiltersIncType();
    public abstract void expandAll();
    public abstract void collapseAll();

}
