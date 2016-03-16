package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.util.Factory;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.WebDriver;

public class User {
    protected String username;
    private Role role;
    private final AuthProvider authProvider;
    public final static User NULL = NullUser.getInstance();

    public User(AuthProvider provider, String username, Role role) {
        this.authProvider = provider;
        this.username = username;
        this.role = role;
    }

    public User(AuthProvider provider, String username) {
        this(provider, username, Role.USER);
    }

    public Role getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public String toString() {
        return "User<" + authProvider + '|' + role + '>';
    }

    // changed in the app via UsersPage/UserService
    void setRole(Role role) {
        this.role = role;
    }

    public void authenticate(Factory<WebDriver> driver, GoesToAuthPage handler) {
        /* NOOP by default */
    }
}
