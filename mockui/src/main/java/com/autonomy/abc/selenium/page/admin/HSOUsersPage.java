package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.users.HSOUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.Status;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
        return getUserRow(user).findElement(By.cssSelector(".user-role a"));
    }

    public void setRoleValueFor(User user, Role newRole) {
        getUserRow(user).findElement(By.xpath(".//option[contains(text(),'"+newRole+"')]")).click();
    }

    public void submitPendingEditFor(User user) {
        getUserRow(user).findElement(By.cssSelector(".editable-submit")).click();
    }

    private WebElement getUserRowByUsername(String username){
        return findElement(By.xpath("//*[contains(@class,'user-name') and text()='" + username + "']/.."));
    }

    private By trashCan(){
        return By.className("users-deleteUser");
    }

    @Override
    public void deleteUser(String username){
        getUserRowByUsername(username).findElement(trashCan()).click();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
    }

    public String getEmailOf(User user) {
        return getUserRow(user).findElement(By.className("user-email")).getText();
    }
}
