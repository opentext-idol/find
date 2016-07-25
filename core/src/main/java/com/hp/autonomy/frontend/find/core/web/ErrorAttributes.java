/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

public enum ErrorAttributes {
    MAIN_MESSAGE("mainMessage"),
    SUB_MESSAGE("subMessage"),
    BASE_URL("baseUrl"),
    STATUS_CODE("statusCode"),
    CONTACT_SUPPORT("contactSupport"),
    BUTTON_HREF("buttonHref"),
    BUTTON_MESSAGE("buttonMessage"),
    AUTH_ERROR("isAuthError");

    private final String value;

    ErrorAttributes(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
