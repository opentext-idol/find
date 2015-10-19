package com.autonomy.abc.selenium.page.keywords;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OPCreateNewKeywordsPage extends CreateNewKeywordsPage {
    public OPCreateNewKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void selectLanguage(String language) {
            languagesSelectBox().click();
            loadOrFadeWait();
            final WebElement element = findElement(By.cssSelector("[data-step='type'] .dropdown-menu")).findElement(By.xpath(".//a[contains(text(), '" + language + "')]"));
            // IE doesn't want to click the dropdown elements
            final JavascriptExecutor executor = (JavascriptExecutor)getDriver();
            executor.executeScript("arguments[0].click();", element);
            loadOrFadeWait();
    }
}
