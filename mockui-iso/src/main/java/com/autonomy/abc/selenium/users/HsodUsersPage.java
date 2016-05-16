package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.auth.HsodNewUser;
import com.autonomy.abc.selenium.auth.HsodUser;
import com.autonomy.abc.selenium.users.table.HsodUserTable;
import com.autonomy.abc.selenium.users.table.HsodUserTableRow;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HsodUsersPage extends HsodUserManagementBase<HsodUserTableRow> {
    private HsodUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    @Override
    public HsodUserCreationModal userCreationModal() {
        return new HsodUserCreationModal(getDriver());
    }

    @Override
    public HsodUser addNewUser(NewUser newUser, Role role) {
        if (newUser instanceof HsodNewUser) {
            return addHsodNewUser((HsodNewUser) newUser, role);
        }
        throw new IllegalStateException("Cannot create new user " + newUser);
    }

    private HsodUser addHsodNewUser(HsodNewUser newUser, Role role) {
        HsodUserCreationModal modal = userCreationModal();
        modal.usernameInput().setValue(newUser.getUsername());
        modal.emailInput().setValue(newUser.getEmail());
        modal.selectRole(role);
        try {
            modal.createUser();
        } catch (TimeoutException e) {
            throw new UserNotCreatedException(newUser);
        }
        return newUser.createWithRole(role);
    }

    @Override
    public HsodUserTable getTable() {
        return new HsodUserTable(findElement(By.cssSelector("#users-current-admins")), getDriver());
    }

    public WebElement resetAuthenticationButton(User user) {
        return getUserRow(user).resetAuthenticationButton();
    }

    public static class Factory extends SOPageFactory<HsodUsersPage> {
        public Factory() {
            super(HsodUsersPage.class);
        }

        public HsodUsersPage create(WebDriver context) {
            return new HsodUsersPage(context);
        }
    }
}
