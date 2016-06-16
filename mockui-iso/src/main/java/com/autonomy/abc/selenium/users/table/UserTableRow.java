package com.autonomy.abc.selenium.users.table;

import com.autonomy.abc.selenium.users.Status;
import com.hp.autonomy.frontend.selenium.element.Editable;
import com.hp.autonomy.frontend.selenium.element.InlineEdit;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class UserTableRow extends AppElement {
    public UserTableRow(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    public String getUsername() {
        return editableUsername().getValue().trim();
    }

    public void changeUsernameTo(final String newUsername) {
        editableUsername().setValueAndWait(newUsername);
    }

    public WebElement usernameEditBox() {
        return findElement(By.className("form-group"));
    }

    private Editable editableUsername() {
        return new InlineEdit(findElement(By.className("user-username")), getDriver());
    }

    public WebElement deleteButton() {
        return findElement(By.className("users-deleteUser"));
    }

    public Role getRole() {
        return Role.fromString(findElement(By.className("user-role")).getText());
    }

    public boolean isConfirmed() {
        final String statusString = findElement(By.className("account-status")).getText();
        return Status.fromString(statusString).equals(Status.CONFIRMED);
    }

    public abstract void changeRoleTo(Role role);

    public abstract boolean canDeleteUser();
}
