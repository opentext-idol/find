package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolUserTable extends UserTable<IdolUserTableRow> {
    public IdolUserTable(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public IdolUserTableRow rowFor(User user) {
        WebElement usernameEl = findElement(new Locator().containingText(user.getUsername()));
        return rowForElement(ElementUtil.ancestor(usernameEl, 2));
    }

    @Override
    protected IdolUserTableRow rowForElement(WebElement element) {
        return new IdolUserTableRow(element, getDriver());
    }
}
