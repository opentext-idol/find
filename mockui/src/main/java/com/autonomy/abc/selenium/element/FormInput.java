package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

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
        clear();
        element.sendKeys(value);
    }

    public AppElement getElement() {
        return element;
    }

    public void setAndSubmit(String value) {
        setValue(value);
        element.submit();
    }

    // TODO/WARN: this works on MOST inputs, but not all
    public WebElement formGroup() {
        return ElementUtil.ancestor(element, 2);
    }

    public String getErrorMessage() {
        List<WebElement> errorMessages = formGroup().findElements(By.cssSelector(".help-block:not(.ng-hide)"));
        if (errorMessages.isEmpty()) {
            /* NB: no error message != empty error message
            * use isEmptyOrNullString() if they should be treated the same */
            return null;
        }
        return StringUtils.join(ElementUtil.getTexts(errorMessages), "\n");
    }
}
