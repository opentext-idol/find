package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.auth.HsodNewUser;
import com.autonomy.abc.selenium.auth.HsodUser;
import com.autonomy.abc.selenium.users.table.HsodUserTable;
import com.autonomy.abc.selenium.users.table.HsodUserTableRow;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

public class HsodUsersPage extends UsersPage<HsodUserTableRow> {
    private HsodUsersPage(final WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    @Override
    public HsodUserCreationModal userCreationModal() {
        return new HsodUserCreationModal(getDriver());
    }

    @Override
    public HsodUser addNewUser(final NewUser newUser, final Role role) {
        if (newUser instanceof HsodNewUser) {
            return addHsodNewUser((HsodNewUser) newUser, role);
        }
        throw new IllegalStateException("Cannot create new user " + newUser);
    }

    private HsodUser addHsodNewUser(final HsodNewUser newUser, final Role role) {
        final HsodUserCreationModal modal = userCreationModal();
        modal.usernameInput().setValue(newUser.getUsername());
        modal.emailInput().setValue(newUser.getEmail());
        modal.selectRole(role);
        try {
            modal.createUser();
        } catch (final TimeoutException e) {
            throw new UserNotCreatedException(newUser);
        }
        return newUser.createWithRole(role);
    }

    @Override
    public HsodUserTable getTable() {
        return new HsodUserTable(findElement(By.cssSelector("#users-current-admins")), getDriver());
    }

    public static class Factory extends SOPageBase.SOPageFactory<HsodUsersPage> {
        public Factory() {
            super(HsodUsersPage.class);
        }

        @Override
        public HsodUsersPage create(final WebDriver context) {
            return new HsodUsersPage(context);
        }
    }
}
