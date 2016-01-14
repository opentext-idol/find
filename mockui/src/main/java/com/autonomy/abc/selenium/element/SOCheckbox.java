package com.autonomy.abc.selenium.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SOCheckbox extends Checkbox {
    private WebElement box;

    public SOCheckbox(WebElement element, WebDriver driver) {
        super(element, driver);
        box = findElement(By.className("icheckbox_square-green"));
    }

    public String getName() {
        return findElement(By.tagName("label")).getText();
    }

    public boolean isChecked() {
        return box.getAttribute("class").contains("checked");
    }
}
