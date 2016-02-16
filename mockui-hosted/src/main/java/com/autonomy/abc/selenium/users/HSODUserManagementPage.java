package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

abstract class HSODUserManagementPage extends UsersPage {
    protected HSODUserManagementPage(WebDriver driver) {
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
}
