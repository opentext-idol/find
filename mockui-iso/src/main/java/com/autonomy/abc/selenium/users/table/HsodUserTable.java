package com.autonomy.abc.selenium.users.table;

import com.autonomy.abc.selenium.auth.HsodUser;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HsodUserTable extends UserTable<HsodUserTableRow> {
    public HsodUserTable(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public HsodUserTableRow rowFor(User user) {
        WebElement usernameEl = findElement(new Locator().havingClass("user-email").containingText(((HsodUser) user).getEmail()));
        return rowForElement(ElementUtil.ancestor(usernameEl, 1));
    }

    @Override
    protected HsodUserTableRow rowForElement(WebElement element) {
        return new HsodUserTableRow(element, getDriver());
    }
}
