package com.autonomy.abc.selenium.util;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

public final class DriverUtil {
    private DriverUtil() {}

    public static void scrollToBottom(final WebDriver driver) {
        for(int i = 0; i < 10; i++){
            new Actions(driver).sendKeys(Keys.PAGE_DOWN).perform();
        }
    }


    public static void hover(final WebDriver driver, final WebElement element) {
        Actions builder = new Actions(driver);
        Dimension dimensions = element.getSize();
        builder.moveToElement(element, dimensions.getWidth() / 2, dimensions.getHeight() / 2);
        Action hover = builder.build();
        hover.perform();
    }

    public static void scrollIntoView(final WebDriver driver, final WebElement element) {
        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final int centre = element.getLocation().getY() + element.getSize().height / 2;
        executor.executeScript("window.scrollTo(0, " + centre + " - Math.floor(window.innerHeight/2));");
    }

    public static void scrollIntoViewAndClick(WebDriver driver, final WebElement element) {
        scrollIntoView(driver, element);
        element.click();
    }
}
