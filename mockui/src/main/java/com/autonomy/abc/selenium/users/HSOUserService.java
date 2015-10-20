package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.login.OPAccount;
import com.hp.autonomy.frontend.selenium.sso.ApiKey;

public class HSOUserService extends  UserService {
    public HSOUserService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    @Override
    public UsersPage goToUsers() {
        getBody().getSideNavBar().switchPage(NavBarTabId.USER_MGMT);
        setUsersPage(getElementFactory().getUsersPage());
        return getUsersPage();
    }

    @Override
    public void login(User user) {
        getElementFactory().getLoginPage().loginWith(getApplication().createCredentials());
    }

    @Override
    public UsersPage createUser(User user) {
        return null;
    }
}
