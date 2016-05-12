package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class UserCreationModal extends ModalView {
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

    public WebElement roleDropdown() {
        return findElement(By.id("create-users-role"));
    }

    public void selectRole(Role role) {
        findElement(new Locator()
                .withTagName("option")
                .containingText(role.toString())
        ).click();
    }

    public abstract void createUser();

    @Override
    public void close() {
        findElement(By.cssSelector("[data-dismiss='modal']")).click();
        new WebDriverWait(getDriver(), 10)
                .withMessage("closing user creation modal")
                .until(ExpectedConditions.stalenessOf(this));
    }
}
