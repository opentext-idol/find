package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FormInput {
    private AppElement element;

    public FormInput(AppElement element) {
        this.element = element;
    }

    public FormInput(WebElement element, WebDriver driver) {
        this(new AppElement(element, driver));
    }

    public void clear() {
        element.clear();
        // artificially trigger key up event
        element.sendKeys("a" + Keys.BACK_SPACE);
    }

    public void submit() {
        element.submit();
    }

    public String getValue() {
        return element.getAttribute("value");
    }

    public void setValue(String value) {
        element.clear();
        element.sendKeys(value);
    }

    public AppElement getElement() {
        return element;
    }

    public void setAndSubmit(String value) {
        setValue(value);
        element.submit();
    }

    public WebElement formGroup() {
        return ElementUtil.ancestor(element, 2);
    }

    public WebElement errorMessage() {
        return formGroup().findElement(By.cssSelector(".help-block:not(.ng-hide)"));
    }
}
