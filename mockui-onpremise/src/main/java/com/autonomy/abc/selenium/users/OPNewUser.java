package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.login.OPAccount;

public class OPNewUser implements NewUser {
    private final String username;
    private final String password;

    public OPNewUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public User withRole(Role role) {
        return new User(new OPAccount(username, password), username, role);
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        usersPage.passwordBoxFor(user).setValueAndWait(password);
        return new User(new OPAccount(user.getUsername(), password), user.getUsername(), user.getRole());
    }

    @Override
    public String toString() {
        return "NewUser<OP:" + username + ">";
    }
}
