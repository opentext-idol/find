package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OPUsersPage extends UsersPage {
    public OPUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    public void deleteUser(final String userName) {
        loadOrFadeWait();
        deleteButton(userName).click();
        loadOrFadeWait();
        findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
        loadOrFadeWait();
    }

    public Role getRoleOf(User user) {
        return Role.fromString(getTableUserTypeLink(user.getUsername()).getText());
    }

    public WebElement roleLinkFor(User user) {
        return getTableUserTypeLink(user.getUsername());
    }

    public void submitPendingEditFor(User user) {
        getUserRow(user.getUsername()).findElement(By.cssSelector(".editable-submit")).click();
    }
}
