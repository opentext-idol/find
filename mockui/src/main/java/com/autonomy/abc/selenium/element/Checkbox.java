package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Checkbox extends AppElement {
    private WebElement box;

    public Checkbox(WebElement element, WebDriver driver) {
        super(element, driver);
        box = findElement(By.className("icheckbox_square-green"));
    }

    public String getName() {
        return findElement(By.tagName("label")).getText();
    }

    public boolean isChecked() {
        return box.getAttribute("class").contains("checked");
    }

    public void toggle() {
        box.click();
    }

    public void check() {
        if (!isChecked()) {
            toggle();
        }
    }

    public void uncheck() {
        if (isChecked()) {
            toggle();
        }
    }
}
