package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.OPISOApplication;
import com.autonomy.abc.selenium.navigation.OPISOElementFactory;
import org.openqa.selenium.By;

public class OPUserService extends UserService<OPISOElementFactory> {
    private OPUsersPage usersPage;

    public OPUserService(OPISOApplication application) {
        super(application);
    }

    public OPUsersPage goToUsers() {
        getElementFactory().getTopNavBar().findElement(By.cssSelector(".dropdown-toggle .hp-settings")).click();
        getElementFactory().getTopNavBar().findElement(By.cssSelector("li[data-pagename='users'] a")).click();
        setUsersPage(getElementFactory().getUsersPage());
        return (OPUsersPage) getUsersPage();
    }

    @Override
    public User createNewUser(NewUser newUser, Role role) {
        usersPage = goToUsers();
        usersPage.createButton().click();
        User user = usersPage.addNewUser(newUser, role);
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
