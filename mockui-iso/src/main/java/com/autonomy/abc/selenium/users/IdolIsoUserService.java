package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;
import com.autonomy.abc.selenium.users.table.UserTable;
import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IdolIsoUserService extends UserService<IdolIsoElementFactory> {
    private IdolUsersPage usersPage;

    public IdolIsoUserService(IsoApplication<? extends IdolIsoElementFactory> application) {
        super(application);
    }

    public IdolUsersPage goToUsers() {
        return getApplication().switchTo(IdolUsersPage.class);
    }

    @Override
    public void deleteUser(User user){
        Waits.loadOrFadeWait();
        getUsersPage().deleteButton(user).click();
        Waits.loadOrFadeWait();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        Waits.loadOrFadeWait();
    }

    public User changeRole(User user, Role newRole) {
        goToUsers();
        getUsersPage().getUserRow(user).changeRoleTo(newRole);
        user.setRole(newRole);
        return user;
    }

    @Override
    public void deleteOtherUsers() {
        goToUsers();
        for (final UserTableRow row : getUsersPage().getTable()) {
            WebElement deleteButton = row.deleteButton();
            if (!ElementUtil.hasClass("not-clickable", deleteButton)) {
                deleteButton.click();
                ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
                deleteModal.okButton().click();
                new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(deleteModal));
            }
        }
    }

    private IdolUsersPage getUsersPage() {
        if (usersPage == null) {
            usersPage = getElementFactory().loadPage(IdolUsersPage.class);
        }
        return usersPage;
    }
}
