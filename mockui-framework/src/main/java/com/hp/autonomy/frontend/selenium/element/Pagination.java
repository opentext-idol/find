package com.hp.autonomy.frontend.selenium.element;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public enum Pagination {
    FIRST("hp-previous-chapter"),
    PREVIOUS("hp-previous"),
    NEXT("hp-next"),
    LAST("hp-next-chapter");

    private final By locator;

    Pagination(String className) {
        locator = By.className(className);
    }

    public WebElement findInside(WebElement container) {
        return ElementUtil.ancestor(container.findElement(locator), 1);
    }
}
