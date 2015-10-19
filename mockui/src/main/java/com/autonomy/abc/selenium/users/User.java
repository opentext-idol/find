package com.autonomy.abc.selenium.users;

public class User {
    private final String username;
    private final String email;
    private String password;
    private AccessLevel accessLevel;

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
        accessLevel = AccessLevel.USER;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
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
    }
}
