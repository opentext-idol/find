package com.autonomy.abc.selenium.find.preview;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class InlinePreview {
    private final WebElement container;

    private InlinePreview(WebElement container, WebDriver driver) {
        this.container = container;
    }

    public boolean loadingIndicatorExists() {
        return findElements(By.className("view-server-loading-indicator")).size()>0;
    }

    public WebElement loadingIndicator(){
        return findElement(By.className("view-server-loading-indicator"));
    }

    public String getContents() {
        return previewContents().getText();
    }

    public WebElement previewContents(){
        return findElement(By.className("preview-mode-contents"));
    }

    public void openDetailedPreview(){
        findElement(By.className("preview-mode-open-detail-button")).click();
    }

    private WebElement findElement(By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(By locator) {
        return container.findElements(locator);
    }

    public static InlinePreview make(WebDriver driver) {
        return new InlinePreview(driver.findElement(By.cssSelector(".preview-mode-wrapper:not(.hide)")), driver);
    }
}
