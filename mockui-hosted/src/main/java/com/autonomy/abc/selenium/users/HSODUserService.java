package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSODUserService extends UserService<HSODElementFactory> {
    private HSODUsersPage usersPage;

    public HSODUserService(SearchOptimizerApplication<? extends HSODElementFactory> application) {
        super(application);
    }

    @Override
    public HSODUsersPage goToUsers() {
        usersPage = getApplication().switchTo(HSODUsersPage.class);
        setUsersPage(usersPage);
        return usersPage;
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

    @Override
    public void deleteUser(User user){
        usersPage = goToUsers();
        usersPage.getUserRow(user).findElement(By.className("users-deleteUser")).click();
        Waits.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Deleted user"));
    }

    @Override
    public HSODUser changeRole(User user, Role newRole) {
        usersPage = goToUsers();

        WebElement roleLink = usersPage.roleLinkFor(user);

        if (user.getRole().equals(newRole)) {
            roleLink.click();
            roleLink.click();
            return (HSODUser) user;
        }

        roleLink.click();
        usersPage.setRoleValueFor(user, newRole);
        new WebDriverWait(getDriver(),5).until(ExpectedConditions.textToBePresentInElement(roleLink, newRole.toString()));
        user.setRole(newRole);
        return (HSODUser) user;
    }

    public void resetAuthentication(User user) {
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
        ((HSODUser) user).setUsername(newUsername);
        return user;
    }
}
