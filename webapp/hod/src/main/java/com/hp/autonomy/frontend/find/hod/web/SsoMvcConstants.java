/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

enum SsoMvcConstants {
    AUTHENTICATE_PATH("authenticatePath"),
    ERROR_PAGE("errorPage"),
    LOGOUT_ENDPOINT("endpoint"),
    LOGOUT_REDIRECT_URL("redirectUrl"),
    PATCH_REQUEST("patchRequest"),
    SSO_PATCH_TOKEN_API("ssoPatchTokenApi"),
    SSO_PAGE_GET_URL("ssoPageGetUrl"),
    SSO_PAGE_POST_URL("ssoPagePostUrl"),
    SSO_ENTRY_PAGE("ssoEntryPage");

    private final String value;

    SsoMvcConstants(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
