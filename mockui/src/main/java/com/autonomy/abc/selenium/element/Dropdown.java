package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Dropdown {
    private WebElement button;
    private WebElement menu;

    public Dropdown(WebElement element) {
        button = element.findElement(By.className("dropdown-toggle"));
        menu = element.findElement(By.className("dropdown-menu"));
    }

    public void toggle() {
        button.click();
    }

    public boolean isOpen() {
        return menu.isDisplayed();
    }

    public void open() {
        if (!isOpen()) {
            toggle();
        }
    }

    public void close() {
        if (isOpen()) {
            toggle();
        }
    }

    // will only work if menu is already open
    public List<WebElement> getItems() {
        return menu.findElements(By.tagName("li"));
    }

}
