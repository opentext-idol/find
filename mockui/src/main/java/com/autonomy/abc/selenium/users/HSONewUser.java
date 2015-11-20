package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;

// TODO: CSA-1663
public class HSONewUser implements NewUser {

    private final String username;
    private final String email;

    public HSONewUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @Override
    public HSOUser signUpAs(Role role, UsersPage usersPage) {
        HSOUsersPage hsoUsersPage = (HSOUsersPage) usersPage;

        hsoUsersPage.addUsername(username);
        hsoUsersPage.addEmail(email);
        hsoUsersPage.selectRole(role);
        hsoUsersPage.createButton().click();
        hsoUsersPage.loadOrFadeWait();
        return new HSOUser(username,email,role);
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        return null;
    }
}
