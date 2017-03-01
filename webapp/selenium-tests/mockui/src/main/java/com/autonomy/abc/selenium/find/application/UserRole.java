/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.application;

public enum UserRole {
    BIFHI,
    FIND,
    BOTH;

    public static UserRole fromString(final String value) {
        if(value == null) {
            return null;
        }

        switch(value.toLowerCase()) {
            case "find":
                return FIND;
            case "bifhi":
            default:
                return BIFHI;
        }
    }
}
