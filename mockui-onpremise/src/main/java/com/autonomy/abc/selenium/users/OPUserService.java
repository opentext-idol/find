package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.OPISOApplication;
import com.autonomy.abc.selenium.navigation.OPISOElementFactory;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class OPUserService extends UserService<OPISOElementFactory> {
    private OPUsersPage usersPage;

    public OPUserService(OPISOApplication application) {
        super(application);
    }

    public OPUsersPage goToUsers() {
        getElementFactory().getTopNavBar().findElement(By.cssSelector(".dropdown-toggle .hp-settings")).click();
        Waits.loadOrFadeWait();
        getElementFactory().getTopNavBar().findElement(By.cssSelector("li[data-pagename='users'] a")).click();
        Waits.loadOrFadeWait();
        setUsersPage(getElementFactory().getUsersPage());
        return (OPUsersPage) getUsersPage();
    }

    @Override
    public User createNewUser(NewUser newUser, Role role) {
        usersPage = goToUsers();
        usersPage.createUserButton().click();
        User user = usersPage.addNewUser(newUser, role);
        usersPage.closeModal();
        return user;
    }

    @Override
    public void deleteUser(User user){
        Waits.loadOrFadeWait();
        usersPage.deleteButton(user).click();
        Waits.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        Waits.loadOrFadeWait();
    }

    public User changeRole(User user, Role newRole) {
        usersPage.roleLinkFor(user).click();
        usersPage.setRoleValueFor(user, newRole);
        usersPage.submitPendingEditFor(user);
        user.setRole(newRole);
        return user;
    }

    @Override
    public void deleteOtherUsers() {
        usersPage = goToUsers();
        for (final WebElement deleteButton : usersPage.getTable().findElements(By.className("hp-trash"))) {
            if (!ElementUtil.hasClass("not-clickable", deleteButton)) {
                Waits.loadOrFadeWait();
                deleteButton.click();
                ModalView.getVisibleModalView(getDriver()).okButton().click();
            }
        }
    }
}
