package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.util.Factory;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSOUserService extends UserService {
    private HSOUsersPage usersPage;
    private HSOElementFactory elementFactory;

    public HSOUserService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
        this.elementFactory = (HSOElementFactory) elementFactory;
    }

    @Override
    public HSOUsersPage goToUsers() {
        getBody().getSideNavBar().switchPage(NavBarTabId.USER_MGMT);
        setUsersPage(elementFactory.getUsersPage());
        return elementFactory.getUsersPage();
    }

    @Override
    public HSOUser createNewUser(NewUser newUser, Role role, Factory<WebDriver> webDriverFactory) {
        UsersPage usersPage = goToUsers();
        usersPage.createUserButton().click();
        HSOUser user = (HSOUser) newUser.signUpAs(role, usersPage, webDriverFactory);
        usersPage.closeModal();
        return user;
    }

    @Override
    public void deleteOtherUsers(){
        usersPage = goToUsers();
        for(WebElement trashCan : usersPage.findElements(By.className("users-deleteUser"))){
            trashCan.click();
            ModalView.getVisibleModalView(getDriver()).okButton().click();
            new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Deleted user"));
            usersPage.loadOrFadeWait();
        }
    }

    public void deleteUser(User user){
        usersPage.getUserRow(user).findElement(By.className("users-deleteUser")).click();
        usersPage.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Deleted user"));
    }

    public void changeRole(User user, Role newRole) {
        WebElement roleLink = usersPage.roleLinkFor(user);
        roleLink.click();
        usersPage.setRoleValueFor(user, newRole);
        usersPage.submitPendingEditFor(user);
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(roleLink));
    }

    public void resetAuthentication(HSOUser user) {
        usersPage.resetAuthenticationButton(user).click();
        usersPage.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Reset authentication for " + user.getUsername()));
    }

    public User editUsername(User user, String newUsername) {
        usersPage.editUsernameLink(user).click();
        usersPage.editUsernameInput(user).setAndSubmit(newUsername);
        user.setUsername(newUsername);
        return user;
    }
}
