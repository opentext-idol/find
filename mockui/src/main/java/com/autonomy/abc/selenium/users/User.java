package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;

public class User {
    private final String username;
    private String email;
    private String password;
    private Role role;
    private AuthProvider authProvider;

    public User(String username, String password, String email, Role role){
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String username, String password, String email){
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = Role.USER;
    }

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

    public String getEmail() {
        return email;
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

}
