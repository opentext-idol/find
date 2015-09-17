package com.autonomy.abc.selenium.element;

import org.openqa.selenium.WebElement;

public interface Editable {
    String getValue();

    void setValueAsync(String value);

    void setValueAndWait(String value);

    void waitForUpdate();

    WebElement getElement();
}