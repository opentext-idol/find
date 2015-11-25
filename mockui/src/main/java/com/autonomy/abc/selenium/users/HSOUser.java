package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;

public class HSOUser extends User {
    private String email;

    public HSOUser(String username, String email, Role role) {
        this(username, email, role, null);
    }

    public HSOUser(String username, String email, Role role, AuthProvider authProvider){
        super(authProvider, username, role);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
