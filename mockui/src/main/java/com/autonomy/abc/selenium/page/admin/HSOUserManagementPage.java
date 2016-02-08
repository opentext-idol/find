package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.Status;
import com.autonomy.abc.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

abstract class HSOUserManagementPage extends UsersPage {
    protected HSOUserManagementPage(WebDriver driver) {
        super(driver);
    }

    public Status getStatusOf(User user) {
        return Status.fromString(getUserRow(user).findElement(By.className("account-status")).getText());
    }

    public WebElement roleLinkFor(User user){
        return getUserRow(user).findElement(By.cssSelector(".user-role .user-role-cell"));
    }

    public WebElement editUsernameLink(User user) {
        return getUserRow(user).findElement(By.className("fa-pencil"));
    }

    public FormInput editUsernameInput(User user) {
        return new FormInput(getUserRow(user).findElement(By.name("new-value")), getDriver());
    }

    protected abstract WebElement getUserRowByUsername(String username);
}
