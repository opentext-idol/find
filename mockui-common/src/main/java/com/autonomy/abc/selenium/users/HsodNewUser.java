package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;

public class HsodNewUser implements NewUser {

    private final String username;
    private final String email;
    private AuthProvider provider;

    public HsodNewUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public HsodNewUser(String username, String email, AuthProvider provider){
        this(username, email);
        this.provider = provider;
    }

    public HsodUser createWithRole(Role role) {
        return new HsodUserBuilder(username)
                .setEmail(email)
                .setRole(role)
                .setAuthProvider(provider)
                .build();
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
