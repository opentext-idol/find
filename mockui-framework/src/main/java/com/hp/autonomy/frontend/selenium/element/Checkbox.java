package com.hp.autonomy.frontend.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class Checkbox extends AppElement {
    protected WebElement box;

    public Checkbox(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public abstract String getName();
    public abstract boolean isChecked();

    public void toggle(){
        box.click();
    }

    public void check(){
        if (!isChecked()) {
            toggle();
        }
    }

    public void uncheck() {
        if (isChecked()) {
            toggle();
        }
    }

    // TODO: this does not belong in Checkbox
    protected int getResultCount(By by){
        String spanResultCount = findElement(by).getText().split("\\(")[1];
        return Integer.parseInt(spanResultCount.substring(0, spanResultCount.length() - 1));
    }
}
