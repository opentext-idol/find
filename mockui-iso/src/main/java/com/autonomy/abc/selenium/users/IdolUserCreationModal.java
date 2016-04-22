package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IdolUserCreationModal extends UserCreationModal {
    public IdolUserCreationModal(WebDriver driver) {
        super(driver);
    }

    public FormInput passwordInput() {
        return new FormInput(findElement(By.id("create-users-password")), getDriver());
    }

    public FormInput passwordConfirmInput() {
        return new FormInput(findElement(By.id("create-users-passwordConfirm")), getDriver());
    }

    @Override
    public void createUser() {
        createButton().click();
        Waits.loadOrFadeWait();
    }
}
