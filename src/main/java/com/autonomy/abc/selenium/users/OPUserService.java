package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import org.openqa.selenium.By;

public class OPUserService extends UserService {
    public OPUserService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    @Override
    public UsersPage goToUsers() {
        getBody().getTopNavBar().findElement(By.cssSelector(".dropdown-toggle .hp-settings")).click();
        getBody().getTopNavBar().findElement(By.cssSelector("li[data-pagename='users'] a")).click();
        setUsersPage(getElementFactory().getUsersPage());
        return getUsersPage();
    }

    @Override
    public User createNewUser(NewUser newUser, Role role) {
        UsersPage usersPage = goToUsers();
        usersPage.createButton().click();
        User user = newUser.signUpAs(role, usersPage);
        usersPage.closeModal();
        return user;
    }
}
