package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
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
