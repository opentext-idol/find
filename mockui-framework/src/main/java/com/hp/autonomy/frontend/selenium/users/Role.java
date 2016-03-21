package com.hp.autonomy.frontend.selenium.users;

public enum Role {
    ADMIN("Admin"), USER("User"), NONE("None");

    private static final Role DEFAULT = USER;

    private String accessLevel;

    Role(String al){
        accessLevel = al;
    }

    @Override
    public String toString() {
        return accessLevel;
    }

    public static Role fromString(String name) {
        if (name == null) {
            return DEFAULT;
        }
        switch (name.toLowerCase()) {
            case "admin":
                return ADMIN;
            case "user":
            case "end user":
                return USER;
            case "none":
                return NONE;
            default:
                return DEFAULT;
        }
    }
}
