package com.hp.autonomy.frontend.selenium.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class RemovableBase implements Removable {
    private WebElement element;
    private By closeLocator;

    protected RemovableBase(WebElement element, By closeLocator) {
        this.element = element;
        this.closeLocator = closeLocator;
    }

    @Override
    public boolean isRemovable() {
        return element.findElements(closeLocator).size() > 0;
    }

    @Override
    public void removeAsync() {
        element.findElement(closeLocator).click();
    }

    @Override
    public void click() {
        element.click();
    }

    @Override
    public String getText() {
        return element.getText();
    }

    protected WebElement element() {
        return element;
    }
}
