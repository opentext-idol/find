package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;
import com.autonomy.abc.selenium.users.table.IdolUserTableRow;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
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
        goToUsers().getUserRow(user).changeRoleTo(newRole);
        return new User(user.getAuthProvider(), user.getUsername(), newRole);
    }

    @Override
    public void deleteOtherUsers() {
        goToUsers();
        for (final IdolUserTableRow row : getUsersPage().getTable()) {
            if (row.canDeleteUser()) {
                row.deleteButton().click();
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
