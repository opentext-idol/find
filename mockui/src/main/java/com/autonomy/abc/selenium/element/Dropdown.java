package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class Dropdown {
    private AppElement element;
    private WebElement button;
    private WebElement menu;

    public Dropdown(AppElement element) {
        this.element = element;
        button = element.findElement(By.className("dropdown-toggle"));
        menu = element.findElement(By.className("dropdown-menu"));
    }

    public Dropdown(WebElement element, WebDriver driver) {
        this(new AppElement(element, driver));
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
            new WebDriverWait(element.getDriver(), 5).until(ExpectedConditions.visibilityOf(menu));
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
