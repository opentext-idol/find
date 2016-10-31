package com.autonomy.abc.selenium.find.application;

public enum UserRole {
    BIFHI,
    FIND,
    BOTH;

    public static UserRole fromString(String value) {
        if (value == null){
            return null;
        }

        switch(value.toLowerCase()){
            case "find" :
                return FIND;
            case "bifhi":
            default:
                return BIFHI;
        }
    }
}
