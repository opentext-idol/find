package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.users.HSOUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.Status;
import com.autonomy.abc.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

abstract class HSOUserManagement extends UsersPage {
    protected HSOUserManagement(WebDriver driver) {
        super(driver);
    }

    public WebElement getUserRow(User user){
        return findElement(By.xpath("//*[contains(@class,'user-email') and text()='" + ((HSOUser) user).getEmail() + "']/.."));
    }

    public Status getStatusOf(User user) {
        return Status.fromString(getUserRow(user).findElement(By.className("account-status")).getText());
    }

    public Role getRoleOf(User user) {
        return Role.fromString(roleLinkFor(user).getText());
    }

    public WebElement roleLinkFor(User user){
        return getUserRow(user).findElement(By.cssSelector(".user-role .user-role-cell"));
    }


    protected WebElement getUserRowByUsername(String username){
        return findElement(By.xpath("//*[contains(@class,'user-name') and text()='" + username + "']/.."));
    }

    public WebElement editUsernameLink(User user) {
        return getUserRow(user).findElement(By.className("fa-pencil"));
    }

    public FormInput editUsernameInput(User user) {
        return new FormInput(getUserRow(user).findElement(By.name("new-value")), getDriver());
    }
}
