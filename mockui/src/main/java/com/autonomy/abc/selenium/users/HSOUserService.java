package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.HSOApplication;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.navigation.HSODElementFactory;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSOUserService extends UserService<HSODElementFactory> {
    private HSOUsersPage usersPage;

    public HSOUserService(HSOApplication application) {
        super(application);
    }

    @Override
    public HSOUsersPage goToUsers() {
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.USERS);
        setUsersPage(getElementFactory().getUsersPage());
        return getElementFactory().getUsersPage();
    }

    @Override
    public HSOUser createNewUser(NewUser newUser, Role role) {
        usersPage = goToUsers();
        usersPage.createUserButton().click();
        try {
            return usersPage.addNewUser(newUser, role);
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
            Waits.loadOrFadeWait();
        }
    }

    public void deleteUser(User user){
        usersPage = goToUsers();
        usersPage.getUserRow(user).findElement(By.className("users-deleteUser")).click();
        Waits.loadOrFadeWait();
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
        Waits.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Reset authentication for " + user.getUsername()));
    }

    public User editUsername(User user, String newUsername) {
        usersPage = goToUsers();
        WebElement pencil = usersPage.editUsernameLink(user);
        pencil.click();
        usersPage.editUsernameInput(user).setAndSubmit(newUsername);
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(pencil));
        Waits.loadOrFadeWait();
        ((HSOUser) user).setUsername(newUsername);
        return user;
    }
}
