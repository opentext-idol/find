package com.hp.autonomy.frontend.selenium.element;

import org.openqa.selenium.WebElement;

public interface Editable {
    String getValue();

    // TODO: do all Editables have an editButton?
    WebElement editButton();

    void setValueAsync(String value);

    void setValueAndWait(String value);

    void waitForUpdate();

    WebElement getElement();
}