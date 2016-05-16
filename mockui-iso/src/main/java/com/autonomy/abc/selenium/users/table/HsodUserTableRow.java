package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.users.Role;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;

public class HsodUserTableRow extends UserTableRow {
    public HsodUserTableRow(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public String getEmail() {
        return findElement(By.className("user-email")).getText();
    }

    @Override
    public void changeRoleTo(Role newRole) {
        Dropdown dropdown = roleDropdown();
        if (newRole.equals(getRole())) {
            dropdown.open();
            dropdown.close();
        } else {
            dropdown.select(newRole.toString());
            waitForRoleToUpdate();
        }
    }

    private Dropdown roleDropdown() {
        return new Dropdown(findElement(By.className("user-roles-dropdown")), getDriver());
    }

    private void waitForRoleToUpdate() {
        new WebDriverWait(getDriver(), 10)
                .withMessage("updating role")
                .until(invisibilityOfElementLocated(By.cssSelector(".user-roles-dropdown .fa-spin")));
    }

    public WebElement resetAuthenticationButton() {
        return findElement(By.className("reset-authentication"));
    }

    @Override
    public boolean canDeleteUser() {
        return true;
    }
}
