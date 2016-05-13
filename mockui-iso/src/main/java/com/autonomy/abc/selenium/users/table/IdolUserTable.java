package com.autonomy.abc.selenium.users.table;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolUserTable extends UserTable {
    public IdolUserTable(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    @Override
    protected UserTableRow rowForElement(WebElement element) {
        return new IdolUserTableRow(element, getDriver());
    }
}
