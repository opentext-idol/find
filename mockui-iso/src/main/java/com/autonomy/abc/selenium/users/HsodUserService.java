package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.auth.HsodUser;
import com.autonomy.abc.selenium.auth.HsodUserBuilder;
import com.autonomy.abc.selenium.hsod.IsoHsodElementFactory;
import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HsodUserService extends UserService<IsoHsodElementFactory> {

    public HsodUserService(final IsoApplication<? extends IsoHsodElementFactory> application) {
        super(application);
    }

    @Override
    public HsodUsersPage goToUsers() {
        final HsodUsersPage usersPage = getApplication().switchTo(HsodUsersPage.class);
        return usersPage;
    }

    @Override
    protected void deleteUserInRow(final UserTableRow row) {
        row.deleteButton().click();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Deleted user"));
    }

    @Override
    public HsodUser changeRole(final User user, final Role newRole) {
        goToUsers().getUserRow(user).changeRoleTo(newRole);
        return new HsodUserBuilder(user)
                .setRole(newRole)
                .build();
    }

    public void resetAuthentication(final User user) {
        goToUsers().getUserRow(user).openResetAuthModal();
        ModalView.getVisibleModalView(getDriver()).okButton().click();
        new WebDriverWait(getDriver(),10).until(GritterNotice.notificationContaining("Reset authentication for " + user.getUsername()));
    }

    public User editUsername(final User user, final String newUsername) {
        goToUsers().getUserRow(user).changeUsernameTo(newUsername);
        return new HsodUserBuilder(user)
                .setUsername(newUsername)
                .build();
    }
}
