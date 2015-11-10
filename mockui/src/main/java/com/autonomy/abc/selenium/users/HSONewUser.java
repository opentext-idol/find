package com.autonomy.abc.selenium.users;

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
    public User signUpAs(Role role, UsersPage usersPage) {
        usersPage.addUsername(username);
        usersPage.addEmail(email);
        usersPage.selectRole(role);
        usersPage.createButton().click();
        usersPage.loadOrFadeWait();
        return new User(null,username,role);
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        return null;
    }
}
