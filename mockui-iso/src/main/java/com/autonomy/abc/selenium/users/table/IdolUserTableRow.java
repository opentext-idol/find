package com.autonomy.abc.selenium.users.table;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolUserTableRow extends UserTableRow {
    public IdolUserTableRow(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public WebElement roleLink() {
        return findElement(By.cssSelector(".role"));
    }

    public void submitPendingEdit() {
        findElement(By.cssSelector(".editable-submit")).click();
    }
}
