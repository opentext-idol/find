package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.element.PasswordBox;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolUserTableRow extends UserTableRow {
    public IdolUserTableRow(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    @Override
    public void changeRoleTo(Role newRole) {
        roleLink().click();
        setRoleValue(newRole);
        submitPendingEdit();
        Waits.loadOrFadeWait();
    }

    private WebElement roleLink() {
        return findElement(By.cssSelector(".role"));
    }

    private void setRoleValue(Role newRole) {
        findElement(By.cssSelector(".input-admin")).findElement(By.xpath(".//*[text() = '" + newRole + "']")).click();
    }

    private void submitPendingEdit() {
        findElement(By.cssSelector(".editable-submit")).click();
    }

    public PasswordBox passwordBox() {
        return new PasswordBox(findElement(By.cssSelector("td:nth-child(2)")), getDriver());
    }

    @Override
    public boolean canDeleteUser() {
        return !ElementUtil.hasClass("not-clickable", deleteButton());
    }
}
