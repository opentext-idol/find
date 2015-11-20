package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;

public class HSOUserService extends UserService {
    private HSOUsersPage usersPage;
    private HSOElementFactory elementFactory;

    public HSOUserService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
        this.elementFactory = (HSOElementFactory) elementFactory;
    }

    @Override
    public HSOUsersPage goToUsers() {
        getBody().getSideNavBar().switchPage(NavBarTabId.USER_MGMT);
        setUsersPage(elementFactory.getUsersPage());
        return elementFactory.getUsersPage();
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
