package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.users.HSOUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSOUsersPage extends HSOUserManagementPage {
    public HSOUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    public WebElement getUserRow(User user){
        return findElement(By.xpath("//*[contains(@class,'user-email') and text()='" + ((HSOUser) user).getEmail() + "']/.."));
    }

    public FormInput getUsernameInput(){
        return new FormInput(getDriver().findElement(By.id("create-users-username")), getDriver());
    }

    public FormInput getEmailInput(){
        return new FormInput(getDriver().findElement(By.className("create-user-email-input")), getDriver());
    }

    public WebElement userLevelDropdown(){
        return getDriver().findElement(By.id("create-users-role"));
    }

    public void addEmail(String email) {
        getEmailInput().setValue(email);
    }

    public void setRoleValueFor(User user, Role newRole) {
        getUserRow(user).findElement(By.xpath(".//a[contains(text(),'"+newRole+"')]")).click();
    }

    private By trashCan(){
        return By.className("users-deleteUser");
    }

    @Override
    public void deleteUser(String username){
        getUserRowByUsername(username).findElement(trashCan()).click();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),20).until(GritterNotice.notificationContaining("Deleted user " + username));
    }

    public String getEmailOf(User user) {
        return getUserRow(user).findElement(By.className("user-email")).getText();
    }

    public WebElement resetAuthenticationButton(User user) {
        return getUserRow(user).findElement(By.className("reset-authentication"));
    }

    public void clearEmail() {
        getEmailInput().clear();
    }

    @Override
    protected WebElement getUserRowByUsername(String username){
        return findElement(By.xpath("//*[contains(@class,'user-name') and text()='" + username + "']/.."));
    }
}
