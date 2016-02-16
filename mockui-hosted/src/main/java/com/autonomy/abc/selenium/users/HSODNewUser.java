package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;

public class HSODNewUser implements NewUser {

    private final String username;
    private final String email;
    private AuthProvider provider;

    public HSODNewUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public HSODNewUser(String username, String email, AuthProvider provider){
        this(username, email);
        this.provider = provider;
    }

    public HSODUser withRole(Role role) {
        return new HSODUser(username, email, role, provider);
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        return null;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "NewUser<" + username + ":" + email + "|" + provider + ">";
    }
}
