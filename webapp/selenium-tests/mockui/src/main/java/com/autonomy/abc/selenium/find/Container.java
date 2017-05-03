package com.autonomy.abc.selenium.find;

import com.google.common.base.Function;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

//TODO: maybe rename?
@SuppressWarnings("Guava")
public enum Container {
    MIDDLE("middle"),
    RIGHT("right-side"),
    LEFT("left-side", driver -> {
        final WebElement containerElement = driver.findElements(By.cssSelector(".left-side-container")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Container must be visible"));

        return ElementUtil.hasClass("hide", containerElement.findElement(By.cssSelector(".parametric-fields-processing-indicator"))) &&
                ElementUtil.hasClass("hide", containerElement.findElement(By.cssSelector(".numeric-parametric-loading-indicator"))) &&
                ElementUtil.hasClass("hide", containerElement.findElement(By.cssSelector(".parametric-field-title-processing-indicator")));
    });

    private final String container;
    private final Function<? super WebDriver, Boolean> waitPredicate;

    Container(final String container) {
        this(container, ExpectedConditions.invisibilityOfElementLocated(By.cssSelector('.' + container + "-container .loading-spinner")));
    }

    Container(final String container, final Function<? super WebDriver, Boolean> waitPredicate) {
        this.container = container;
        this.waitPredicate = waitPredicate;
    }

    private String asCssClass() {
        return '.' + container + "-container";
    }

    public void waitForLoad(final WebDriver driver) {
        try {
            new WebDriverWait(driver, 2)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(asCssClass() + " .loading-spinner")));
        } catch (final RuntimeException ignored) {
            // The loading spinner wasn't visible for long enough
        }

        new WebDriverWait(driver, 15)
                .withMessage("Container " + this + " failed to load")
                .until(waitPredicate);
    }

    public WebElement findUsing(final SearchContext driver) {
        return currentTabContents(driver).findElement(By.cssSelector(asCssClass()));
    }

    public static WebElement currentTabContents(final SearchContext driver) {
        return driver.findElement(By.cssSelector(".query-service-view-container > :not(.hide):not(.search-tabs-container), div[data-pagename=search]"));
    }
}
