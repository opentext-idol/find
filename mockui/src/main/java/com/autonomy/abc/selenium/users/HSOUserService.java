package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    public HSOUser createNewUser(NewUser newUser, Role role) {
        UsersPage usersPage = goToUsers();
        usersPage.createUserButton().click();
        HSOUser user = (HSOUser) newUser.signUpAs(role, usersPage);
        usersPage.closeModal();
        return user;
    }

    @Override
    public void deleteOtherUsers(){
        usersPage = goToUsers();
        for(WebElement trashCan : usersPage.findElements(By.className("users-deleteUser"))){
            trashCan.click();
            ModalView.getVisibleModalView(getDriver()).okButton().click();
            new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Deleted user"));
            usersPage.loadOrFadeWait();
        }
    }

    public void deleteUser(HSOUser user){
        usersPage.getUserRow(user).findElement(By.className("users-deleteUser")).click();
        usersPage.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        usersPage.loadOrFadeWait();
    }
}
