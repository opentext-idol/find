package com.autonomy.abc.selenium.users;

public class User {
    private final String name;
    private final String email;
    private String password;
    private AccessLevel accessLevel;

    public User(String name, String password, String email, AccessLevel accessLevel){
        this.name = name;
        this.email = email;
        this.password = password;
        this.accessLevel = accessLevel;
    }

    public User(String name, String password, String email){
        this.name = name;
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

    public String getName() {
        return name;
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
