package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.page.search.DocumentViewer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public abstract class SearchResult {
    private WebElement result;
    private WebDriver driver;

    protected SearchResult(WebElement result, WebDriver driver) {
        this.result = result;
        this.driver = driver;
    }

    public abstract WebElement title();
    public abstract WebElement getIcon();
    public abstract DocumentViewer openDocumentPreview();

    public String getTitleString() {
        return title().getText();
    }

    public String getDescription() {
        return findElement(By.className("result-summary")).getText();
    }

    public boolean isPromoted() {
        return findElements(By.className("promoted-label")).size() > 0;
    }

    public WebElement star() {
        return findElement(By.className("hp-favorite"));
    }

    protected WebElement findElement(By locator) {
        return result.findElement(locator);
    }

    protected List<WebElement> findElements(By locator) {
        return result.findElements(locator);
    }

    protected WebDriver getDriver(){
        return driver;
    }
}
