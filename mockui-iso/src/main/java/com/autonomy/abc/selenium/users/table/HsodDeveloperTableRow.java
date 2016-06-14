package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.users.Role;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HsodDeveloperTableRow extends UserTableRow {
    public HsodDeveloperTableRow(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    @Override
    public void changeRoleTo(final Role newRole) {
        throw new UnsupportedOperationException("Cannot change role for HSOD developer users");
    }

    @Override
    public boolean canDeleteUser() {
        return false;
    }
}
