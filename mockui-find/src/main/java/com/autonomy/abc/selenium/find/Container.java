package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public enum Container {
    LEFT("left-side"),
    MIDDLE("middle"),
    RIGHT("right-side");

    private final String container;

    Container(final String container) {
        this.container = container;
    }

    private String asCssClass() {
        return '.' + container + "-container";
    }

    //TODO: BIG PROBLEM: THIS IS USED BY THE RELATED CONCEPTS PANEL AND THE FILTER PANEL
    //WILL HAVE BROKEN THOSE
    public void waitForLoad(final WebDriver driver) {
        new WebDriverWait(driver,50).until(new LoadedCondition());
    }

    private static class LoadedCondition implements ExpectedCondition<Boolean> {
        @Override
        public Boolean apply(final WebDriver input) {
            if (resultsLoaded(input)) {
                return true;
            }
            return false;
        }

        private boolean resultsLoaded(final WebDriver driver) {
            return !driver.findElements(By.cssSelector(".error.well:not(.hide)")).isEmpty()
                    || !driver.findElements(By.xpath("//div[contains(@class,'result-message') and contains(text(),'No results found')]")).isEmpty()
                    || !driver.findElements(By.cssSelector(".results-contents")).isEmpty();
        }
    }
    public WebElement findUsing(final WebDriver driver) {
        return currentTabContents(driver).findElement(By.cssSelector(asCssClass()));
    }

    public static WebElement currentTabContents(final WebDriver driver) {
        return driver.findElement(By.cssSelector(".query-service-view-container > :not(.hide):not(.search-tabs-container), div[data-pagename=search]"));
    }
}
