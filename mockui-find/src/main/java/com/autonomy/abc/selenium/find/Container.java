package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public enum Container {
    LEFT("left-side"),
    MIDDLE("middle"),
    RIGHT("right-side");

    private final String container;

    Container(String container) {
        this.container = container;
    }

    private String asCssClass() {
        return "." + container + "-container";
    }

    public void waitForLoad(WebDriver driver) {
        try {
            new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(asCssClass() + " .loading-spinner")));
        } catch (Exception e) {
            //Noop
        }

        new WebDriverWait(driver, 60)
                .withMessage("Container " + this + " failed to load")
                .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(asCssClass() + " .fa-spinner")));
    }

    public WebElement findUsing(WebDriver driver) {
        return driver.findElement(By.cssSelector(".full-height-viewport:not(.hide) " + asCssClass()));
    }
}
