package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PasswordBox implements Editable {
    private final AppElement element;

    public PasswordBox(AppElement element) {
        this.element = element;
    }

    public PasswordBox(WebElement element, WebDriver driver) {
        this(new AppElement(element, driver));
    }

    private WebElement passwordInput() {
        return element.findElement(By.cssSelector("input[type='password']"));
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public WebElement editButton() {
        return element.findElement(By.cssSelector(".editable-click"));
    }

    @Override
    public void setValueAsync(String value) {
        try {
            editButton().click();
        } catch (ElementNotVisibleException e) {
            // already clicked
        }
        WebElement input = passwordInput();
        input.clear();
        input.sendKeys(value);
        element.findElement(By.cssSelector(".editable-submit")).click();
    }

    @Override
    public void setValueAndWait(String value) {
        setValueAsync(value);
        waitForUpdate();
    }

    @Override
    public void waitForUpdate() {
        new WebDriverWait(element.getDriver(), 10).until(ExpectedConditions.visibilityOf(editButton()));
    }

    @Override
    public WebElement getElement() {
        return element;
    }
}
