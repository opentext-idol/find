package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.element.DocumentViewer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public abstract class QueryResult {
    private final WebElement result;
    private final WebDriver driver;

    protected QueryResult(final WebElement result, final WebDriver driver) {
        this.result = result;
        this.driver = driver;
    }

    public abstract WebElement title();
    public abstract WebElement icon();
    public abstract DocumentViewer openDocumentPreview();

    public String getTitleString() {
        return title().getText();
    }

    public String getDescription() {
        return findElement(By.className("result-summary")).getText();
    }

    public boolean isPromoted() {
        return !findElements(By.className("promoted-label")).isEmpty();
    }

    public WebElement star() {
        return findElement(By.className("hp-favorite"));
    }

    protected WebElement findElement(final By locator) {
        return result.findElement(locator);
    }

    protected List<WebElement> findElements(final By locator) {
        return result.findElements(locator);
    }

    protected WebDriver getDriver(){
        return driver;
    }
}
