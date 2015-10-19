package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;

public class HSOUserService extends  UserService {
    public HSOUserService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    @Override
    public void goToUsers() {
        getBody().getSideNavBar().switchPage(NavBarTabId.USER_MGMT);
        setUsersPage(getElementFactory().getUsersPage());
    }
}
