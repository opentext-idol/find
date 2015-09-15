package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FormInput extends AppElement {

    public FormInput(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public void setAndSubmit(String value) {
        clear();
        sendKeys(value);
        submit();
    }
}
