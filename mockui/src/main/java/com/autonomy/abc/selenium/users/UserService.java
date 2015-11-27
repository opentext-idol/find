package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class UserService {

    private final Application application;
    private final ElementFactory elementFactory;
    private UsersPage usersPage;

    public UserService(Application application, ElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    public abstract void deleteUser(User user);

    public abstract UsersPage goToUsers();

    public abstract User createNewUser(NewUser newUser, Role role, Factory<WebDriver> webDriverFactory);

    protected WebDriver getDriver() {
        return getElementFactory().getDriver();
    }

    protected ElementFactory getElementFactory() {
        return elementFactory;
    }

    protected AppBody getBody() {
        return application.createAppBody(getDriver());
    }

    protected Application getApplication() {
        return application;
    }

    public UsersPage getUsersPage() {
        return usersPage;
    }

    public void setUsersPage(UsersPage usersPage) {
        this.usersPage = usersPage;
    }

    public void deleteOtherUsers() {
        usersPage = goToUsers();
        for (final WebElement deleteButton : usersPage.getTable().findElements(By.cssSelector("button"))) {
            if (!usersPage.isAttributePresent(deleteButton, "disabled")) {
                usersPage.loadOrFadeWait();
                deleteButton.click();
                usersPage.loadOrFadeWait();
                usersPage.findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
            }
        }
    }

    public void changeRole(User user, Role newRole) {
        usersPage.roleLinkFor(user).click();
        usersPage.setRoleValueFor(user, newRole);
        usersPage.submitPendingEditFor(user);
    }
}
