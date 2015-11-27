package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
    public User createNewUser(NewUser newUser, Role role, Factory<WebDriver> webDriverFactory) {
        UsersPage usersPage = goToUsers();
        usersPage.createButton().click();
        User user = newUser.signUpAs(role, usersPage, null);
        usersPage.closeModal();
        return user;
    }

    @Override
    public void deleteUser(User user){
        getUsersPage().deleteUser(user.getUsername());
    }
}
