package com.autonomy.abc.selenium.find.application;

public enum UserRole {
    BIFHI,
    FIND,
    BOTH;

    public static UserRole fromString(String value) {
        if (value == null){
            return null;
        }
        for(UserRole userRole : UserRole.values()) {
            if (userRole.toString().toLowerCase().equals(value.toLowerCase())) {
                return userRole;
            }
        }
        return BIFHI;
    }
}
