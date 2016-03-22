package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;

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

    public HSODUser createWithRole(Role role) {
        return new HSODUser(username, email, role, provider);
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
