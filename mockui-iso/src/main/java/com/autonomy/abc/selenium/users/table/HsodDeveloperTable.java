package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HsodDeveloperTable extends UserTable<HsodDeveloperTableRow> {
    public HsodDeveloperTable(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    @Override
    public HsodDeveloperTableRow rowFor(final User user) {
        final WebElement usernameEl = findElement(new Locator().havingClass("user-username").containingText(user.getUsername()));
        return rowForElement(ElementUtil.ancestor(usernameEl, 1));
    }

    @Override
    protected HsodDeveloperTableRow rowForElement(final WebElement element) {
        return new HsodDeveloperTableRow(element, getDriver());
    }
}
