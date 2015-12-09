package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
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
        }
    }

    @Override
    public void collapse() {
        if (!isCollapsed()) {
            chevronIcon().click();
        }
    }

    @Override
    public boolean isCollapsed() {
        return AppElement.hasClass("collapsed", chevronIcon());
    }

    private WebElement chevronIcon() {
        return container.findElement(By.cssSelector("[data-toggle='collapse']"));
    }
}
