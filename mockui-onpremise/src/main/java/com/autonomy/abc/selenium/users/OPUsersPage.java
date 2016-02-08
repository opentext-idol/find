package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.UsersPage;
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

    public WebElement roleLinkFor(User user) {
        return getUserRow(user).findElement(By.cssSelector(".role"));
    }

    public void submitPendingEditFor(User user) {
        getUserRow(user).findElement(By.cssSelector(".editable-submit")).click();
    }

    @Override
    public WebElement getUserRow(User user) {
        return findElement(By.xpath(".//span[contains(text(), '" + user.getUsername() + "')]/../.."));
    }

    public void setRoleValueFor(User user, Role newRole) {
        selectTableUserType(user, newRole.toString());
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
