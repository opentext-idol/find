package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

abstract class HsodUserManagementBase<T extends UserTableRow> extends UsersPage<T> {
    protected HsodUserManagementBase(WebDriver driver) {
        super(driver);
    }

}
