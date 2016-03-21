package com.hp.autonomy.frontend.selenium.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HPRemovable extends RemovableBase {
    private final static By CLOSE_LOCATOR = By.className("hp-close");
    private WebDriver driver;

    public HPRemovable(WebElement element, WebDriver driver) {
        super(element, CLOSE_LOCATOR);
        this.driver = driver;
    }

    @Override
    public void removeAndWait() {
        removeAndWait(5);
    }

    @Override
    public void removeAndWait(int timeout) {
        removeAsync();
        new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.stalenessOf(element()));
    }
}
