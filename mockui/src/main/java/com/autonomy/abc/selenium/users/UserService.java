package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Factory;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class UserService {

    private final Application application;
    private final ElementFactory elementFactory;
    protected UsersPage usersPage;

    public UserService(Application application, ElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    public abstract void deleteUser(User user);

    public abstract UsersPage goToUsers();

    public abstract User createNewUser(NewUser newUser, Role role);

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
            if (!ElementUtil.isAttributePresent(deleteButton, "disabled")) {
                Waits.loadOrFadeWait();
                deleteButton.click();
                Waits.loadOrFadeWait();
                usersPage.findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
            }
        }
    }

    public abstract User changeRole(User user, Role newRole);
}
