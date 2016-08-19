package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.users.table.HsodDeveloperTable;
import com.autonomy.abc.selenium.users.table.HsodDeveloperTableRow;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HsodDevelopersPage extends UsersPage<HsodDeveloperTableRow> {
    private HsodDevelopersPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    public UserCreationModal userCreationModal() {
        throw new UnsupportedOperationException("Cannot add new developers to a tenancy");
    }

    @Override
    public User addNewUser(final NewUser newUser, final Role role) {
        throw new UnsupportedOperationException("Cannot add new developers to a tenancy");
    }

    @Override
    public HsodDeveloperTable getTable() {
        return new HsodDeveloperTable(findElement(By.cssSelector("#users-current-admins")), getDriver());
    }

    public static class Factory extends SOPageBase.SOPageFactory<HsodDevelopersPage> {
        public Factory() {
            super(HsodDevelopersPage.class);
        }

        @Override
        public HsodDevelopersPage create(final WebDriver context) {
            return new HsodDevelopersPage(context);
        }
    }
}
