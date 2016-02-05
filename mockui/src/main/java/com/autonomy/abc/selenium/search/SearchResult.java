package com.autonomy.abc.selenium.search;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public abstract class SearchResult {
    private WebElement result;

    SearchResult(WebElement result){
        this.result = result;
    }

    public abstract WebElement title();

    public String getTitleString(){
        return title().getText();
    }

    public String getDescription() {
        return findElement(By.className("result-summary")).getText();
    }

    public abstract WebElement getIcon();

    public boolean isPromoted(){
        return findElements(By.className("promoted-label")).size() > 0;
    }

    public WebElement star() {
        return findElement(By.className("fa-star"));
    }

    protected WebElement findElement(By locator) {
        return result.findElement(locator);
    }

    protected List<WebElement> findElements(By locator) {
        return result.findElements(locator);
    }
}
