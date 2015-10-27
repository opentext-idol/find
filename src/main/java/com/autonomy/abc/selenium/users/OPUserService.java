package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.login.OPAccount;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    public void login(User user) {
        getElementFactory().getLoginPage().loginWith(new OPAccount(user.getUsername(),user.getPassword()));
    }

    @Override
    public UsersPage createUser(User user) {
        goToUsers();
        getUsersPage().createNewUser(user.getUsername(),user.getPassword(),user.getAccessLevel().toString());
        return getUsersPage();
    }
}
