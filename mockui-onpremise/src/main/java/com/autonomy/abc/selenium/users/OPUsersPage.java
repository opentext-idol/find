package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.login.OPAccount;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OPUsersPage extends UsersPage {
    private OPUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    @Override
    public User addNewUser(NewUser newUser, Role role) {
        if (newUser instanceof OPNewUser) {
            return addOPNewUser((OPNewUser) newUser, role);
        }
        throw new IllegalStateException("Cannot create user " + newUser);
    }

    private User addOPNewUser(OPNewUser newUser, Role role) {
        addUsername(newUser.getUsername());
        addAndConfirmPassword(newUser.getPassword(), newUser.getPassword());
        selectRole(role);
        createButton().click();
        Waits.loadOrFadeWait();
        return newUser.withRole(role);
    }

    public void deleteUser(final String userName) {
        Waits.loadOrFadeWait();
        deleteButton(userName).click();
        Waits.loadOrFadeWait();
        findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
        Waits.loadOrFadeWait();
    }

    public Role getRoleOf(User user) {
        return Role.fromString(getTableUserTypeLink(user.getUsername()).getText());
    }

    public WebElement roleLinkFor(User user) {
        return getTableUserTypeLink(user.getUsername());
    }

    public void submitPendingEditFor(User user) {
        getUserRow(user.getUsername()).findElement(By.cssSelector(".editable-submit")).click();
    }

    @Override
    public WebElement getUserRow(User user) {
        return getUserRow(user.getUsername());
    }

    public void setRoleValueFor(User user, Role newRole) {
        selectTableUserType(user.getUsername(), newRole.toString());
    }

    public void clearPasswords() {
        ModalView.getVisibleModalView(getDriver()).findElement(By.id("create-users-password")).clear();
        ModalView.getVisibleModalView(getDriver()).findElement(By.id("create-users-passwordConfirm")).clear();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, OPUsersPage> {
        public OPUsersPage create(WebDriver context) {
            return new OPUsersPage(context);
        }
    }
}
