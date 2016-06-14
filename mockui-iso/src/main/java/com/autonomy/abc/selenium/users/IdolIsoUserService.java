package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;
import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IdolIsoUserService extends UserService<IdolIsoElementFactory> {
    public IdolIsoUserService(final IsoApplication<? extends IdolIsoElementFactory> application) {
        super(application);
    }

    public IdolUsersPage goToUsers() {
        return getApplication().switchTo(IdolUsersPage.class);
    }

    public User changeRole(final User user, final Role newRole) {
        goToUsers().getUserRow(user).changeRoleTo(newRole);
        return new User(user.getAuthProvider(), user.getUsername(), newRole);
    }

    @Override
    protected void deleteUserInRow(final UserTableRow row) {
        row.deleteButton().click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.okButton().click();
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(deleteModal));
    }
}
