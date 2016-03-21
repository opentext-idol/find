package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.ReplacementAuth;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;

public class OPNewUser implements NewUser, ReplacementAuth {
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
    public User createWithRole(Role role) {
        return new User(new OPAccount(username, password), username, role);
    }

    @Override
    public User replaceAuth(User toReplace) {
        return new User(new OPAccount(toReplace.getUsername(), password), toReplace.getUsername(), toReplace.getRole());
    }

    @Override
    public String toString() {
        return "NewUser<OP:" + username + ">";
    }
}
