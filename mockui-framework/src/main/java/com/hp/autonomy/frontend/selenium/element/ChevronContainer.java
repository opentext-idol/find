package com.hp.autonomy.frontend.selenium.element;

import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ChevronContainer implements Collapsible {
    private WebElement container;
    private WebDriver driver;

    public ChevronContainer(WebElement element, WebDriver driver) {
        container = element;
        this.driver = driver;
    }

    @Override
    public void expand() {
        if (isCollapsed()) {
            DriverUtil.scrollIntoViewAndClick(driver, chevronIcon());
            Waits.loadOrFadeWait();
        }
    }

    @Override
    public void collapse() {
        if (!isCollapsed()) {
            DriverUtil.scrollIntoViewAndClick(driver, chevronIcon());
            Waits.loadOrFadeWait();
        }
    }

    @Override
    public boolean isCollapsed() {
        return ElementUtil.hasClass("collapsed", chevronIcon()) || !isInsideDisplayed();
    }

    /* some containers (ICMA) are not initialised with collapsed class,
     * this fallback shouldn't break anything... */
    private boolean isInsideDisplayed() {
        return container.findElement(By.className("collapse")).isDisplayed();
    }

    private WebElement chevronIcon() {
        return container.findElement(By.cssSelector("[data-toggle='collapse'], .rotating-chevron"));
    }
}
