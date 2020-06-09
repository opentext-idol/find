/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
