package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import org.openqa.selenium.By;

public class OPUserService extends UserService {
    public OPUserService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    @Override
    public void goToUsers() {
        TopNavBar topNavBar = getBody().getTopNavBar();
        topNavBar.findElement(By.cssSelector(".fa-cog")).click();
        topNavBar.findElement(By.cssSelector("li[data-pagename='users'] a")).click();
        setUsersPage(getElementFactory().getUsersPage());
    }
}
