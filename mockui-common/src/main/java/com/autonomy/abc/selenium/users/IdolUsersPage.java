package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.ReplacementAuth;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolUsersPage extends UsersPage {
    private IdolUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    @Override
    public User addNewUser(NewUser newUser, Role role) {
        if (newUser instanceof IdolIsoNewUser) {
            return addIdolNewUser((IdolIsoNewUser) newUser, role);
        }
        throw new IllegalStateException("Cannot create user " + newUser);
    }

    private User addIdolNewUser(IdolIsoNewUser newUser, Role role) {
        addUsername(newUser.getUsername());
        addAndConfirmPassword(newUser.getPassword(), newUser.getPassword());
        selectRole(role);
        createButton().click();
        Waits.loadOrFadeWait();
        return newUser.createWithRole(role);
    }

    public User replaceAuthFor(User user, ReplacementAuth newAuth) {
        if (newAuth instanceof IdolIsoReplacementAuth) {
            ((IdolIsoReplacementAuth) newAuth).sendTo(passwordBoxFor(user));
            passwordBoxFor(user).waitForUpdate();
        }
        return newAuth.replaceAuth(user);
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
        userCreationModal().passwordInput().clear();
        userCreationModal().passwordConfirmInput().clear();
    }

    public static class Factory extends SOPageFactory<IdolUsersPage> {
        public Factory() {
            super(IdolUsersPage.class);
        }

        public IdolUsersPage create(WebDriver context) {
            return new IdolUsersPage(context);
        }
    }
}
