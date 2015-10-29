package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.login.OPAccount;

public class OPNewUser implements NewUser {
    private final String username;
    private final String password;

    public OPNewUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public User signUpAs(User.Role role, UsersPage usersPage) {
        usersPage.addUsername(username);
        usersPage.addAndConfirmPassword(password, password);
        usersPage.selectRole(role);
        usersPage.createButton().click();
        usersPage.loadOrFadeWait();
        OPAccount auth = new OPAccount(username, password);
        return new User(auth, username, role);
    }

    @Override
    public String toString() {
        return "NewUser<OP:" + username + ">";
    }
}
