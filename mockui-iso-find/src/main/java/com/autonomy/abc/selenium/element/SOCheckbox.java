package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.element.Checkbox;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SOCheckbox extends Checkbox {
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

    public int getResultsCount() {
        return getResultCount(By.tagName("span"));
    }
}
