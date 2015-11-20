package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.users.HSOUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.Status;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSOUsersPage extends UsersPage {
    public HSOUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    public FormInput getUsernameInput(){
        return new FormInput(getDriver().findElement(By.id("create-users-username")), getDriver());
    }

    public FormInput getEmailInput(){
        return new FormInput(getDriver().findElement(By.className("create-user-email-input")), getDriver());
    }

    public WebElement getUserLevelDropdown(){
        return getDriver().findElement(By.id("create-users-role"));
    }

    public void addEmail(String email) {
        getEmailInput().setValue(email);
    }

    public WebElement refreshButton() {
        return findElement(By.id("refresh-users"));
    }

    public WebElement getUserRow(HSOUser user){
        return findElement(By.xpath("//*[contains(@class,'user-email') and text()='" + user.getEmail() + "']/.."));
    }

    public Status getUserStatus(HSOUser user) {
        return Status.fromString(getUserRow(user).findElement(By.className("account-status")).toString());
    }

    public Role getUserRole(HSOUser user) {
        return Role.fromString(getUserRow(user).findElement(By.className("user-role")).toString());
    }
}
