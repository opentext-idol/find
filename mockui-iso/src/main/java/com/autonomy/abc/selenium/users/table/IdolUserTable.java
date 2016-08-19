package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolUserTable extends UserTable<IdolUserTableRow> {
    public IdolUserTable(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    @Override
    public IdolUserTableRow rowFor(final User user) {
        final WebElement usernameEl = findElement(new Locator().containingText(user.getUsername()));
        return rowForElement(ElementUtil.ancestor(usernameEl, 2));
    }

    @Override
    protected IdolUserTableRow rowForElement(final WebElement element) {
        return new IdolUserTableRow(element, getDriver());
    }
}
