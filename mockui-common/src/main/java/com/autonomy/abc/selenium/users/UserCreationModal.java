package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class UserCreationModal extends ModalView {
    UserCreationModal(WebDriver driver) {
        super(ModalView.getVisibleModalView(driver), driver);
    }

    public WebElement createButton() {
        return findElement(new Locator()
                .withTagName("button")
                .containingText("Create")
        );
    }

    public FormInput usernameInput() {
        return new FormInput(findElement(By.name("create-users-username")), getDriver());
    }

    public FormInput passwordInput() {
        return new FormInput(findElement(By.id("create-users-password")), getDriver());
    }

    public FormInput passwordConfirmInput() {
        return new FormInput(findElement(By.id("create-users-passwordConfirm")), getDriver());
    }

    public void selectRole(Role role) {
        findElement(new Locator()
                .withTagName("option")
                .containingText(role.toString())
        ).click();
    }

    @Override
    public void close() {
        findElement(By.cssSelector("[data-dismiss='modal']")).click();
        Waits.loadOrFadeWait();
    }
}
