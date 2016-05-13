package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class UserTableRow extends AppElement {
    public UserTableRow(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public String getUsername() {
        return findElement(By.className("user-username")).getText().trim();
    }

    public WebElement deleteButton() {
        return findElement(By.className("users-deleteUser"));
    }

    public Role getRole() {
        return Role.fromString(findElement(By.className("user-role")).getText());
    }
}
