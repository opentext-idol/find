package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.page.SOElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class UserService<T extends SOElementFactory> extends ServiceBase<T> {

    protected UsersPage usersPage;

    public UserService(SearchOptimizerApplication<? extends T> application) {
        super(application);
    }

    public abstract void deleteUser(User user);

    public abstract UsersPage goToUsers();

    public abstract User createNewUser(NewUser newUser, Role role);

    public UsersPage getUsersPage() {
        return usersPage;
    }

    public void setUsersPage(UsersPage usersPage) {
        this.usersPage = usersPage;
    }

    public void deleteOtherUsers() {
        usersPage = goToUsers();
        for (final WebElement deleteButton : usersPage.getTable().findElements(By.cssSelector("button"))) {
            if (!ElementUtil.isAttributePresent(deleteButton, "disabled")) {
                Waits.loadOrFadeWait();
                deleteButton.click();
                Waits.loadOrFadeWait();
                usersPage.findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
            }
        }
    }

    public abstract User changeRole(User user, Role newRole);
}
