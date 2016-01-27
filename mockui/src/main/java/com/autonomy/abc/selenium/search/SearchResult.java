package com.autonomy.abc.selenium.search;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class SearchResult {
    protected WebDriver driver;
    protected WebElement result;

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

    public WebElement getElement(){
        return result;
    }

    public boolean isPromoted(){
        try {
            return result.findElement(By.className("promoted-label")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public WebElement star() {
        return result.findElement(By.className("fa-star"));
    }
}
