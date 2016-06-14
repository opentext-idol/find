package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HsodUserCreationModal extends UserCreationModal {
    public HsodUserCreationModal(final WebDriver driver) {
        super(driver);
    }

    public FormInput emailInput() {
        return new FormInput(findElement(By.className("create-user-email-input")), getDriver());
    }

    public void createUser() {
        createButton().click();
        new WebDriverWait(getDriver(), 15)
            .withMessage("creating user")
            .until(GritterNotice.notificationContaining("Created user"));
        new WebDriverWait(getDriver(), 5)
            .withMessage("clearing user input")
            .until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(final WebDriver driver) {
                    return usernameInput().getValue().isEmpty();
                }
            });
    }
}
