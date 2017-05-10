package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

//TODO: maybe rename?
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

    //TODO: waiting too long
    public void waitForLoad(final WebDriver driver) {
         try {
             new WebDriverWait(driver, 5)
                     .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(asCssClass() + " .loading-spinner")));
         }
         catch (final Exception ignored) {
             //Noop
         }
        new WebDriverWait(driver, 80)
                .withMessage("Container " + this + " failed to load")
                .until((ExpectedCondition<Boolean>) driver1 -> driver1.findElements(By.cssSelector(asCssClass() + " .parametric-fields-processing-indicator:not(.hide)")).isEmpty() &&
                        driver1.findElements(By.cssSelector(asCssClass() + " .numeric-parametric-loading-indicator:not(.hide)")).isEmpty() &&
                        driver1.findElements(By.cssSelector(asCssClass() + " .parametric-field-title-processing-indicator:not(.hide)")).isEmpty());
    }

    public WebElement findUsing(final SearchContext driver) {
        return currentTabContents(driver).findElement(By.cssSelector(asCssClass()));
    }

    public static WebElement currentTabContents(final SearchContext driver) {
        return driver.findElement(By.cssSelector(".query-service-view-container > :not(.hide):not(.search-tabs-container), div[data-pagename=search]"));
    }
}
