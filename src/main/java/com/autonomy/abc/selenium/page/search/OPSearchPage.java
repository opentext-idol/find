package com.autonomy.abc.selenium.page.search;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OPSearchPage extends SearchPage {
    public OPSearchPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void selectLanguage(String language) {
        findElement(By.cssSelector(".search-language .current-language-selection")).click();

        final WebElement element = findElement(By.cssSelector(".search-language")).findElement(By.xpath(".//a[text()='" + language + "']"));
        // IE doesn't like clicking dropdown elements
        final JavascriptExecutor executor = (JavascriptExecutor)getDriver();
        executor.executeScript("arguments[0].click();", element);

        loadOrFadeWait();
        waitForSearchLoadIndicatorToDisappear();
    }
}
