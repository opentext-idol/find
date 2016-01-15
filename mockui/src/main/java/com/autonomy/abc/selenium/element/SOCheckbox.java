package com.autonomy.abc.selenium.element;

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
        String spanResultCount = findElement(By.tagName("span")).getText().split(" ")[1];
        return Integer.parseInt(spanResultCount.substring(1, spanResultCount.length() - 1));
    }
}
