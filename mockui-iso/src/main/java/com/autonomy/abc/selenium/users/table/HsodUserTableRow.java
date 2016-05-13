package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.users.Role;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HsodUserTableRow extends UserTableRow {
    public HsodUserTableRow(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public String getEmail() {
        return findElement(By.className("user-email")).getText();
    }

    public void setRoleValue(Role newRole) {
        findElement(By.partialLinkText(newRole.toString())).click();
    }

    public WebElement resetAuthenticationButton() {
        return findElement(By.className("reset-authentication"));
    }
}
