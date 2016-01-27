package com.autonomy.abc.selenium.search;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class SearchResult {
    protected WebDriver driver;

    protected WebElement title;
    protected String description;
    protected WebElement icon;

    public WebElement title() {
        return title;
    }

    public String getTitleString(){
        return title.getText();
    }

    public String getDescription() {
        return description;
    }

    public WebElement getIcon() {
        return icon;
    }
}
