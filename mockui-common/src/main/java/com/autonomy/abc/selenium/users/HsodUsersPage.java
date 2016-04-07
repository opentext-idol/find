package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HsodUsersPage extends HsodUserManagementBase {
    private HsodUsersPage(WebDriver driver) {
        super(driver);
        waitForLoad();
    }

    @Override
    public HsodUser addNewUser(NewUser newUser, Role role) {
        if (newUser instanceof HsodNewUser) {
            return addHSONewUser((HsodNewUser) newUser, role);
        }
        throw new IllegalStateException("Cannot create new user " + newUser);
    }

    private HsodUser addHSONewUser(HsodNewUser newUser, Role role) {
        addUsername(newUser.getUsername());
        addEmail(newUser.getEmail());
        selectRole(role);
        createButton().click();

        try {
            new WebDriverWait(getDriver(), 15)
                    .withMessage("User hasn't been created")
                    .until(GritterNotice.notificationContaining("Created user"));
            new WebDriverWait(getDriver(), 5)
                    .withMessage("User input hasn't cleared")
                    .until(new ExpectedCondition<Boolean>() {
                        @Override
                        public Boolean apply(WebDriver driver) {
                            return getUsernameInput().getValue().isEmpty();
                        }
                    });
        } catch (TimeoutException e) {
            throw new UserNotCreatedException(newUser);
        }

        return newUser.createWithRole(role);
    }

    public WebElement getUserRow(User user){
        return findElement(By.xpath("//*[contains(@class,'user-email') and text()='" + ((HsodUser) user).getEmail() + "']/.."));
    }

    public FormInput getUsernameInput(){
        return new FormInput(getDriver().findElement(By.id("create-users-username")), getDriver());
    }

    public FormInput getEmailInput(){
        return new FormInput(getDriver().findElement(By.className("create-user-email-input")), getDriver());
    }

    public WebElement userLevelDropdown(){
        return getDriver().findElement(By.id("create-users-role"));
    }

    public void addEmail(String email) {
        getEmailInput().setValue(email);
    }

    public void setRoleValueFor(User user, Role newRole) {
        getUserRow(user).findElement(By.xpath(".//a[contains(text(),'"+newRole+"')]")).click();
    }

    public String getEmailOf(User user) {
        return getUserRow(user).findElement(By.className("user-email")).getText();
    }

    public WebElement resetAuthenticationButton(User user) {
        return getUserRow(user).findElement(By.className("reset-authentication"));
    }

    public void clearEmail() {
        getEmailInput().clear();
    }

    public static class Factory extends SOPageFactory<HsodUsersPage> {
        public Factory() {
            super(HsodUsersPage.class);
        }

        public HsodUsersPage create(WebDriver context) {
            return new HsodUsersPage(context);
        }
    }
}
