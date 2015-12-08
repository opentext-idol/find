package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;
import org.openqa.selenium.By;

public class OPUserService extends UserService {
    private OPUsersPage usersPage;

    public OPUserService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    public OPUsersPage goToUsers() {
        getBody().getTopNavBar().findElement(By.cssSelector(".dropdown-toggle .hp-settings")).click();
        getBody().getTopNavBar().findElement(By.cssSelector("li[data-pagename='users'] a")).click();
        setUsersPage(getElementFactory().getUsersPage());
        return (OPUsersPage) getUsersPage();
    }

    @Override
    public User createNewUser(NewUser newUser, Role role) {
        usersPage = goToUsers();
        usersPage.createButton().click();
        User user = newUser.signUpAs(role, usersPage);
        usersPage.closeModal();
        return user;
    }

    @Override
    public void deleteUser(User user){
        getUsersPage().deleteUser(user.getUsername());
    }

    public User changeRole(User user, Role newRole) {
        usersPage.roleLinkFor(user).click();
        usersPage.setRoleValueFor(user, newRole);
        usersPage.submitPendingEditFor(user);
        user.setRole(newRole);
        return user;
    }
}
