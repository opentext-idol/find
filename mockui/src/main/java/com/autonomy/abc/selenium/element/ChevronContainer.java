package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
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
            Waits.loadOrFadeWait();
        }
    }

    @Override
    public void collapse() {
        if (!isCollapsed()) {
            chevronIcon().click();
            Waits.loadOrFadeWait();
        }
    }

    @Override
    public boolean isCollapsed() {
        return ElementUtil.hasClass("collapsed", chevronIcon());
    }

    private WebElement chevronIcon() {
        try {
            return container.findElement(By.cssSelector("[data-toggle='collapse']"));
        } catch (NoSuchElementException e) {
            return container.findElement(By.className("rotating-chevron"));
        }
    }
}
