package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.auth.IdolIsoNewUser;
import com.autonomy.abc.selenium.auth.IdolIsoReplacementAuth;
import com.autonomy.abc.selenium.users.table.IdolUserTable;
import com.autonomy.abc.selenium.users.table.IdolUserTableRow;
import com.hp.autonomy.frontend.selenium.element.PasswordBox;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.ReplacementAuth;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IdolUsersPage extends UsersPage<IdolUserTableRow> {
    private IdolUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    @Override
    public IdolUserCreationModal userCreationModal() {
        return new IdolUserCreationModal(getDriver());
    }

    @Override
    public User addNewUser(NewUser newUser, Role role) {
        if (newUser instanceof IdolIsoNewUser) {
            return addIdolNewUser((IdolIsoNewUser) newUser, role);
        }
        throw new IllegalStateException("Cannot create user " + newUser);
    }

    // TODO: move to IdolIsoSignupService
    private User addIdolNewUser(IdolIsoNewUser newUser, Role role) {
        IdolUserCreationModal modal = new IdolUserCreationModal(getDriver());
        modal.usernameInput().setValue(newUser.getUsername());
        modal.passwordInput().setValue(newUser.getPassword());
        modal.passwordConfirmInput().setValue(newUser.getPassword());
        modal.selectRole(role);
        modal.createUser();
        return newUser.createWithRole(role);
    }

    public User replaceAuthFor(User user, ReplacementAuth newAuth) {
        if (newAuth instanceof IdolIsoReplacementAuth) {
            PasswordBox passwordBox = getUserRow(user).passwordBox();
            ((IdolIsoReplacementAuth) newAuth).sendTo(passwordBox);
            passwordBox.waitForUpdate();
        }
        return newAuth.replaceAuth(user);
    }

    @Override
    public IdolUserTable getTable() {
        return new IdolUserTable(findElement(By.cssSelector("#users-current-admins")), getDriver());
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
