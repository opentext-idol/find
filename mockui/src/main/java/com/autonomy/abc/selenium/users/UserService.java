package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import org.openqa.selenium.WebDriver;

public abstract class UserService {

    private final Application application;
    private final ElementFactory elementFactory;
    private UsersPage usersPage;

    public UserService(Application application, ElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    public abstract UsersPage goToUsers();

    public abstract UsersPage createUser(User user);

    public void login(User user) {
        getElementFactory().getLoginPage().loginWith(user.getAuthProvider());
    }

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
}
