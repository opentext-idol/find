package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;

public class HSOUser extends User {
    public HSOUser(String username, String email, Role role) {
        super(null, username, role);
    }
}
