package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.config.HSOApplication;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
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
        getBody().getSideNavBar().switchPage(NavBarTabId.USERS);
        setUsersPage(elementFactory.getUsersPage());
        return elementFactory.getUsersPage();
    }

    @Override
    public HSOUser createNewUser(NewUser newUser, Role role) {
        usersPage = goToUsers();
        usersPage.createUserButton().click();
        try {
            return (HSOUser) newUser.signUpAs(role, usersPage);
        } finally {
            usersPage.closeModal();
        }
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
        usersPage = goToUsers();
        usersPage.getUserRow(user).findElement(By.className("users-deleteUser")).click();
        usersPage.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Deleted user"));
    }

    public HSOUser changeRole(User user, Role newRole) {
        usersPage = goToUsers();

        if(user.getRole().equals(newRole)){
            return (HSOUser) user;
        }

        WebElement roleLink = usersPage.roleLinkFor(user);
        roleLink.click();
        usersPage.setRoleValueFor(user, newRole);
        new WebDriverWait(getDriver(),5).until(ExpectedConditions.textToBePresentInElement(roleLink, newRole.toString()));
        user.setRole(newRole);
        return (HSOUser) user;
    }

    public void resetAuthentication(HSOUser user) {
        usersPage = goToUsers();
        usersPage.resetAuthenticationButton(user).click();
        usersPage.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Reset authentication for " + user.getUsername()));
    }

    public User editUsername(User user, String newUsername) {
        usersPage = goToUsers();
        WebElement pencil = usersPage.editUsernameLink(user);
        pencil.click();
        usersPage.editUsernameInput(user).setAndSubmit(newUsername);
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(pencil));
        usersPage.loadOrFadeWait();
        ((HSOUser) user).setUsername(newUsername);
        return user;
    }
}
