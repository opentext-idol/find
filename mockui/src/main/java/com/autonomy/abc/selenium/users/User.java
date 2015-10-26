package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;

public class User<T extends AuthProvider> {
    private final String username;
    private final String email;
    private String password;
    private AccessLevel accessLevel;
    private T authProvider;

    public User(String username, String password, String email, AccessLevel accessLevel){
        this.username = username;
        this.email = email;
        this.password = password;
        this.accessLevel = accessLevel;
    }

    public User(String username, String password, String email){
        this.username = username;
        this.email = email;
        this.password = password;
        this.accessLevel = AccessLevel.USER;
    }

    public User(T provider, String email, AccessLevel accessLevel) {
        this.authProvider = provider;
        this.username = null;
        this.email = email;
        this.accessLevel = accessLevel;
    }

    public User(T provider, String email) {
        this(provider, email, AccessLevel.USER);
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public String getEmail() {
        return email;
    }

    public T getAuthProvider() {
        return authProvider;
    }

    public enum AccessLevel {
        ADMIN("Admin"), USER("User"), NONE("None");

        private String accessLevel;

        AccessLevel(String al){
            accessLevel = al;
        }

        @Override
        public String toString() {
            return accessLevel;
        }

        public static AccessLevel fromString(String name) {
            switch (name.toLowerCase()) {
                case "admin":
                    return ADMIN;
                case "user":
                    return USER;
                default:
                    return NONE;
            }
        }
    }

    public String toString() {
        return "User<" + username + ">";
    }

}
