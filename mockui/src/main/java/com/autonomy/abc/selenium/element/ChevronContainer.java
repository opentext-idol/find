package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class ChevronContainer implements Collapsible {
    private WebElement container;

    public ChevronContainer(WebElement element) {
        container = element;
    }

    @Override
    public void expand() {
        if (isCollapsed()) {
            chevronIcon().click();
            loadOrFadeWait();
        }
    }

    @Override
    public void collapse() {
        if (!isCollapsed()) {
            chevronIcon().click();
            loadOrFadeWait();
        }
    }

    @Override
    public boolean isCollapsed() {
        return AppElement.hasClass("collapsed", chevronIcon());
    }

    private WebElement chevronIcon() {
        try {
            return container.findElement(By.cssSelector("[data-toggle='collapse']"));
        } catch (NoSuchElementException e) {
            return container.findElement(By.className("rotating-chevron"));
        }
    }

    private void loadOrFadeWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
