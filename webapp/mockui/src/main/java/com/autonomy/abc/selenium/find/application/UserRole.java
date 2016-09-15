package com.autonomy.abc.selenium.find.application;

public enum UserRole {
    BIFHI,
    FIND,
    ALL;

    public static UserRole fromString(String value) {
        for(UserRole userRole : UserRole.values()) {
            if (userRole.toString().toLowerCase().equals(value.toLowerCase())) {
                return userRole;
            }
        }

        return ALL;
    }
}
