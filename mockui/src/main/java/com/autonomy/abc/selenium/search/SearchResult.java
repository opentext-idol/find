package com.autonomy.abc.selenium.search;

import org.openqa.selenium.WebElement;

public class SearchResult {
    protected WebElement title;
    protected String description;
    protected WebElement icon;

    public WebElement getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public WebElement getIcon() {
        return icon;
    }
}
